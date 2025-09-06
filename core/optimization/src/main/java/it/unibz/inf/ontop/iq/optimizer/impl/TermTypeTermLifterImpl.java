package it.unibz.inf.ontop.iq.optimizer.impl;

import com.google.common.collect.*;
import com.google.inject.Inject;
import com.google.inject.Singleton;
import it.unibz.inf.ontop.exception.MinorOntopInternalBugException;
import it.unibz.inf.ontop.exception.OntopInternalBugException;
import it.unibz.inf.ontop.injection.CoreSingletons;
import it.unibz.inf.ontop.injection.IntermediateQueryFactory;
import it.unibz.inf.ontop.iq.BinaryNonCommutativeIQTree;
import it.unibz.inf.ontop.iq.IQTree;
import it.unibz.inf.ontop.iq.NaryIQTree;
import it.unibz.inf.ontop.iq.UnaryIQTree;
import it.unibz.inf.ontop.iq.impl.IQTreeTools;
import it.unibz.inf.ontop.iq.impl.NaryIQTreeTools;
import it.unibz.inf.ontop.iq.node.*;
import it.unibz.inf.ontop.iq.optimizer.TermTypeTermLifter;
import it.unibz.inf.ontop.iq.tools.TypeConstantDictionary;
import it.unibz.inf.ontop.iq.transform.IQTreeVariableGeneratorTransformer;
import it.unibz.inf.ontop.iq.transform.impl.DelegatingIQTreeVariableGeneratorTransformer;
import it.unibz.inf.ontop.iq.visit.impl.DefaultRecursiveIQTreeVisitingTransformerWithVariableGenerator;
import it.unibz.inf.ontop.model.term.*;
import it.unibz.inf.ontop.model.term.functionsymbol.FunctionSymbol;
import it.unibz.inf.ontop.model.term.functionsymbol.FunctionSymbolFactory;
import it.unibz.inf.ontop.model.term.functionsymbol.RDFTermTypeFunctionSymbol;
import it.unibz.inf.ontop.substitution.InjectiveSubstitution;
import it.unibz.inf.ontop.substitution.SubstitutionFactory;
import it.unibz.inf.ontop.utils.ImmutableCollectors;
import it.unibz.inf.ontop.utils.VariableGenerator;

import java.util.Collection;
import java.util.Map;
import java.util.Optional;
import java.util.stream.Stream;

import static it.unibz.inf.ontop.iq.impl.IQTreeTools.UnaryIQTreeDecomposition;

@Singleton
public class TermTypeTermLifterImpl extends DelegatingIQTreeVariableGeneratorTransformer implements TermTypeTermLifter {

    private final IntermediateQueryFactory iqFactory;
    private final TermFactory termFactory;
    private final IQTreeTools iqTreeTools;
    private final TypeConstantDictionary dictionary;
    private final SubstitutionFactory substitutionFactory;
    private final FunctionSymbolFactory functionSymbolFactory;

    private final IQTreeVariableGeneratorTransformer transformer;

    @Inject
    private TermTypeTermLifterImpl(CoreSingletons coreSingletons,
                                   TypeConstantDictionary typeConstantDictionary) {
        this.iqFactory = coreSingletons.getIQFactory();
        this.termFactory = coreSingletons.getTermFactory();
        this.iqTreeTools = coreSingletons.getIQTreeTools();
        this.substitutionFactory = coreSingletons.getSubstitutionFactory();
        this.functionSymbolFactory = coreSingletons.getFunctionSymbolFactory();
        this.dictionary = typeConstantDictionary;

        this.transformer = IQTreeVariableGeneratorTransformer.of(
                // Makes sure the tree is already normalized before transforming it
                IQTree::normalizeForOptimization,
                IQTreeVariableGeneratorTransformer.of(TermTypeTermLifter::new),
                (t,  vg) -> makeRDFTermTypeFunctionSymbolsSimplifiable(t),
                IQTree::normalizeForOptimization);
    }

    @Override
    protected IQTreeVariableGeneratorTransformer getTransformer() {
        return transformer;
    }

    /**
     * Lifts meta term type definitions blocked by unions.
     *
     * Also makes sure that type terms are lifted above AggregationNode-s and DistinctNode-s.
     *
     */
    private class TermTypeTermLifter extends DefaultRecursiveIQTreeVisitingTransformerWithVariableGenerator {

        TermTypeTermLifter(VariableGenerator variableGenerator) {
            super(TermTypeTermLifterImpl.this.iqFactory, variableGenerator);
        }

        @Override
        public IQTree transformUnion(NaryIQTree tree, UnionNode rootNode, ImmutableList<IQTree> children) {
            // Recursive (children are normalized separately)
            // RDF type constants are replaced by functional terms
            ImmutableList<IQTree> normalizedChildren = NaryIQTreeTools.transformChildren(children,
                    c -> replaceTypeTermConstants(transformChild(c)));

            ImmutableSet<Variable> metaTermTypeVariables = rootNode.getVariables().stream()
                    .filter(v -> normalizedChildren.stream()
                            .anyMatch(c -> extractDefinition(v, c)
                                    // NB: we only expect NULLs or functional terms at this stage
                                    .filter(d -> d instanceof ImmutableFunctionalTerm)
                                    .map(d -> (ImmutableFunctionalTerm) d)
                                    .filter(d -> d.getFunctionSymbol() instanceof RDFTermTypeFunctionSymbol)
                                    .isPresent()))
                    .collect(ImmutableCollectors.toSet());

            if (metaTermTypeVariables.isEmpty())
                return iqFactory.createNaryIQTree(rootNode, normalizedChildren)
                        .normalizeForOptimization(variableGenerator);

            ImmutableMultimap<Variable, RDFTermTypeConstant> possibleConstantMultimap =
                    metaTermTypeVariables.stream()
                            .flatMap(v -> normalizedChildren.stream()
                                    .flatMap(child -> extractPossibleTermTypeConstants(v, child))
                                    .map(c -> Maps.immutableEntry(v, c)))
                            .distinct()
                            .collect(ImmutableCollectors.toMultimap());

            ImmutableMap<Variable, RDFTermTypeFunctionSymbol> typeFunctionSymbolMap = possibleConstantMultimap.asMap().entrySet().stream()
                    .collect(ImmutableCollectors.toMap(
                            Map.Entry::getKey,
                            // Non-simplifiable so as to lifted
                            e -> functionSymbolFactory.getRDFTermTypeFunctionSymbol(dictionary, ImmutableSet.copyOf(e.getValue()), false)));

            ImmutableList<IQTree> newChildren = NaryIQTreeTools.transformChildren(normalizedChildren,
                    c -> enforceUsageOfCommonTypeFunctionSymbol(c, typeFunctionSymbolMap));

            return iqFactory.createNaryIQTree(rootNode, newChildren)
                    // Lifts the RDFTermTypeFunctionSymbols at the root of child definitions
                    .normalizeForOptimization(variableGenerator);
        }

        private IQTree replaceTypeTermConstants(IQTree child) {
            var construction = UnaryIQTreeDecomposition.of(child, ConstructionNode.class);
            return iqTreeTools.unaryIQTreeBuilder()
                    .append(construction.getOptionalNode()
                            .map(cn -> iqTreeTools.replaceSubstitution(cn,
                                    s -> s.transform(this::replaceTypeTermConstants))))
                    .build(construction.getTail());
        }

        /**
         * Recursive
         * <p>
         * NB: a function symbol accepting RDF type terms as arguments and returning a RDF type term
         * is expected to lift RDFTermTypeFunctionSymbol-s. Concerns for instance COALESCE, CASE and some type inference
         * functions.
         */
        private ImmutableTerm replaceTypeTermConstants(ImmutableTerm term) {
            if (term instanceof RDFTermTypeConstant) {
                RDFTermTypeConstant typeConstant = (RDFTermTypeConstant) term;
                DBConstant intConstant = dictionary.convert(typeConstant);
                // Non simplifiable as a simplification would reverse the transformation
                return termFactory.getRDFTermTypeFunctionalTerm(intConstant, dictionary, ImmutableSet.of(typeConstant), false);
            }
            else if (term instanceof ImmutableFunctionalTerm) {
                ImmutableList<ImmutableTerm> subTerms = ((ImmutableFunctionalTerm) term).getTerms().stream()
                        .map(this::replaceTypeTermConstants)
                        .collect(ImmutableCollectors.toList());

                return termFactory.getImmutableFunctionalTerm(((ImmutableFunctionalTerm) term).getFunctionSymbol(), subTerms)
                        // May lift RDFTermTypeFunctionSymbol-s
                        .simplify();
            }
            else
                return term;
        }

        private Optional<ImmutableTerm> extractDefinition(Variable variable, IQTree child) {
            return UnaryIQTreeDecomposition.of(child, ConstructionNode.class)
                    .getOptionalNode()
                    .map(ConstructionNode::getSubstitution)
                    .flatMap(s -> Optional.ofNullable(s.get(variable)));
        }

        private Stream<RDFTermTypeConstant> extractPossibleTermTypeConstants(Variable variable, IQTree child) {
            ImmutableTerm definition = extractDefinition(variable, child)
                    .orElseThrow(() -> new UnexpectedlyFormattedIQTreeException(
                            "Was expecting the child to define the blocked definition of the RDF term type variable"));

            if (definition.isNull())
                return Stream.of();
            else if (definition instanceof ImmutableFunctionalTerm)
                return extractPossibleTermTypeConstants((ImmutableFunctionalTerm) definition);
            else
                throw new UnexpectedlyFormattedIQTreeException("Was not expecting a Variable or a different kind of Constant");
        }

        /**
         * We expect to receive only one particular type of function symbol
         * TODO: define it
         */
        private Stream<RDFTermTypeConstant> extractPossibleTermTypeConstants(ImmutableFunctionalTerm definition) {
            RDFTermTypeFunctionSymbol functionSymbol = Optional.of(definition.getFunctionSymbol())
                    .filter(f -> f instanceof RDFTermTypeFunctionSymbol)
                    .map(f -> (RDFTermTypeFunctionSymbol) f)
                    .orElseThrow(() -> new UnexpectedlyFormattedIQTreeException("Was expecting the definition to be a " +
                            "RDFTermTypeFunctionSymbol"));
            return functionSymbol.getConversionMap().values().stream();
        }

        private IQTree enforceUsageOfCommonTypeFunctionSymbol(IQTree tree,
                                                              ImmutableMap<Variable, RDFTermTypeFunctionSymbol> typeFunctionSymbolMap) {

            var construction = UnaryIQTreeDecomposition.of(tree, ConstructionNode.class);
            if (!construction.isPresent())
                throw new UnexpectedlyFormattedIQTreeException("Was expecting the child to start with a ConstructionNode");

            return iqFactory.createUnaryIQTree(
                    iqTreeTools.replaceSubstitution(construction.getNode(),
                            s -> s.builder()
                                    .transformOrRetain(typeFunctionSymbolMap::get, this::enforceUsageOfCommonTypeFunctionSymbol)
                                    .build()),
                    construction.getChild());
        }

        private ImmutableTerm enforceUsageOfCommonTypeFunctionSymbol(ImmutableTerm definition, RDFTermTypeFunctionSymbol functionSymbol) {
            if (definition.isNull())
                return termFactory.getImmutableFunctionalTerm(functionSymbol, definition);
            else if ((definition instanceof ImmutableFunctionalTerm)
                    && ((ImmutableFunctionalTerm) definition).getFunctionSymbol() instanceof RDFTermTypeFunctionSymbol) {
                return termFactory.getImmutableFunctionalTerm(functionSymbol,
                        ((ImmutableFunctionalTerm) definition).getTerms());
            }
            throw new MinorOntopInternalBugException(
                    String.format("Was expecting a functional term with a RDFTermTypeFunctionSymbol, not %s.\n" +
                            "Some simplifications might be missing", definition));
        }

        /**
         * Used to change out any RDFTermTypeConstants appearing in ValuesNodes to DBConstants.
         */
        @Override
        public IQTree transformValues(ValuesNode valuesNode) {
            // TODO: Currently we walk through all values three times in calling the below three methods. Is there a better way?
            ImmutableSet<Variable> metaTermTypeVariables = valuesNode.getOrderedVariables().stream()
                    .filter(v -> valuesNode.getValueStream(v)
                            .anyMatch(c1 -> c1 instanceof RDFTermTypeConstant))
                    .collect(ImmutableCollectors.toSet());

            if (metaTermTypeVariables.isEmpty())
                return transformLeaf(valuesNode);

            ImmutableSet<RDFTermTypeConstant> possibleConstants = valuesNode.getValues().stream()
                    .map(tuple -> tuple.stream()
                            .filter(c -> c instanceof RDFTermTypeConstant)
                            .map(c -> (RDFTermTypeConstant) c)
                            .collect(ImmutableCollectors.toSet()))
                    .flatMap(Collection::stream)
                    .collect(ImmutableCollectors.toSet());

            InjectiveSubstitution<Variable> renaming = metaTermTypeVariables.stream()
                    .collect(substitutionFactory.toFreshRenamingSubstitution(variableGenerator));

            ValuesNode newValuesNode = iqFactory.createValuesNode(
                    substitutionFactory.apply(renaming, valuesNode.getOrderedVariables()),
                    valuesNode.getValues().stream()
                            .map(tuple -> tuple.stream()
                                    .map(this::replaceTypeTermConstantWithFunctionalTerm)
                                    .collect(ImmutableCollectors.toList()))
                            .collect(ImmutableCollectors.toList()));

            ConstructionNode newConstructionNode = iqFactory.createConstructionNode(
                    ImmutableSet.copyOf(valuesNode.getOrderedVariables()),
                    renaming.transform(t -> termFactory.getRDFTermTypeFunctionalTerm(t, dictionary, possibleConstants, false)));

            return iqFactory.createUnaryIQTree(newConstructionNode, newValuesNode);
        }


        private Constant replaceTypeTermConstantWithFunctionalTerm(Constant constant) {
            if (constant instanceof RDFTermTypeConstant) {
                RDFTermTypeConstant typeConstant = (RDFTermTypeConstant) constant;
                return dictionary.convert(typeConstant);
            }
            return constant;
        }


        @Override
        public IQTree transformDistinct(UnaryIQTree tree, DistinctNode rootNode, IQTree child) {
            return transformNodeBlockingNonInjectiveBindings(rootNode, child);
        }

        @Override
        public IQTree transformAggregation(UnaryIQTree tree, AggregationNode rootNode, IQTree child) {
            return transformNodeBlockingNonInjectiveBindings(rootNode, child);
        }

        /**
         * Makes sure the child does not project a type term anymore.
         * <p>
         * Useful for dealing with COALESCE and CASEs
         */
        private IQTree transformNodeBlockingNonInjectiveBindings(UnaryOperatorNode rootNode, IQTree child) {
            IQTree normalizedChild = replaceTypeTermConstants(transformChild(child));
            return iqFactory.createUnaryIQTree(rootNode, normalizedChild)
                    .normalizeForOptimization(variableGenerator);
        }

        @Override
        public IQTree transformTrue(TrueNode leaf) {
            return leaf.normalizeForOptimization(variableGenerator);
        }

        @Override
        public IQTree transformExtensionalData(ExtensionalDataNode leaf) {
            return leaf.normalizeForOptimization(variableGenerator);
        }

        @Override
        public IQTree transformIntensionalData(IntensionalDataNode leaf) {
            return leaf.normalizeForOptimization(variableGenerator);
        }

        @Override
        public IQTree transformEmpty(EmptyNode leaf) {
            return leaf.normalizeForOptimization(variableGenerator);
        }

        @Override
        public IQTree transformOrderBy(UnaryIQTree tree, OrderByNode rootNode, IQTree child) {
            return super.transformOrderBy(tree, rootNode, child)
                    .normalizeForOptimization(variableGenerator);
        }

        @Override
        public IQTree transformSlice(UnaryIQTree tree, SliceNode rootNode, IQTree child) {
            return super.transformSlice(tree, rootNode, child)
                    .normalizeForOptimization(variableGenerator);
        }

        @Override
        public IQTree transformFilter(UnaryIQTree tree, FilterNode rootNode, IQTree child) {
            return super.transformFilter(tree, rootNode, child)
                    .normalizeForOptimization(variableGenerator);
        }

        @Override
        public IQTree transformFlatten(UnaryIQTree tree, FlattenNode rootNode, IQTree child) {
            return super.transformFlatten(tree, rootNode, child)
                    .normalizeForOptimization(variableGenerator);
        }

        @Override
        public IQTree transformConstruction(UnaryIQTree tree, ConstructionNode rootNode, IQTree child) {
            return super.transformConstruction(tree, rootNode, child)
                    .normalizeForOptimization(variableGenerator);
        }

        @Override
        public IQTree transformInnerJoin(NaryIQTree tree, InnerJoinNode rootNode, ImmutableList<IQTree> children) {
            return super.transformInnerJoin(tree, rootNode, children)
                    .normalizeForOptimization(variableGenerator);
        }

        @Override
        public IQTree transformLeftJoin(BinaryNonCommutativeIQTree tree, LeftJoinNode rootNode, IQTree leftChild, IQTree rightChild) {
            return super.transformLeftJoin(tree, rootNode, leftChild, rightChild)
                    .normalizeForOptimization(variableGenerator);
        }
    }

    private static class UnexpectedlyFormattedIQTreeException extends OntopInternalBugException {

        protected UnexpectedlyFormattedIQTreeException(String message) {
            super(message);
        }
    }

    /**
     * ONLY in the root construction node (if there is such a node)
     *
     * Makes sure all the RDFTermTypeFunctionSymbol-s are simplifiable and therefore post-processable.
     *
     * Note that such function symbols are only expected at this stage in the root
     * (they cannot be processed by the DB engine).
     *
     */
    private IQTree makeRDFTermTypeFunctionSymbolsSimplifiable(IQTree tree) {
        var construction = UnaryIQTreeDecomposition.of(tree, ConstructionNode.class);
        return iqTreeTools.unaryIQTreeBuilder()
                .append(construction.getOptionalNode()
                        .map(cn -> iqTreeTools.replaceSubstitution(
                                cn,
                                s -> s.transform(this::makeRDFTermTypeFunctionSymbolsSimplifiable))))
                .build(construction.getTail());
    }

    /**
     * Recursive
     */
    private ImmutableTerm makeRDFTermTypeFunctionSymbolsSimplifiable(ImmutableTerm immutableTerm) {
        if (immutableTerm instanceof ImmutableFunctionalTerm) {
            ImmutableFunctionalTerm functionalTerm = (ImmutableFunctionalTerm) immutableTerm;
            ImmutableList<ImmutableTerm> newTerms = functionalTerm.getTerms().stream()
                    //Recursive
                    .map(this::makeRDFTermTypeFunctionSymbolsSimplifiable)
                    .collect(ImmutableCollectors.toList());

            FunctionSymbol functionSymbol = functionalTerm.getFunctionSymbol();
            FunctionSymbol newFunctionSymbol = (functionSymbol instanceof RDFTermTypeFunctionSymbol)
                    ? ((RDFTermTypeFunctionSymbol) functionSymbol).getSimplifiableVariant()
                    : functionSymbol;

            return termFactory.getImmutableFunctionalTerm(newFunctionSymbol, newTerms);
        }
        else
            return immutableTerm;
    }
}
