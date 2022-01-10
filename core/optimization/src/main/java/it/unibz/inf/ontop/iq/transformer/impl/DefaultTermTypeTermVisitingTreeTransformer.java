package it.unibz.inf.ontop.iq.transformer.impl;

import com.google.common.collect.*;
import com.google.inject.Inject;
import com.google.inject.assistedinject.Assisted;
import it.unibz.inf.ontop.exception.MinorOntopInternalBugException;
import it.unibz.inf.ontop.exception.OntopInternalBugException;
import it.unibz.inf.ontop.injection.IntermediateQueryFactory;

import it.unibz.inf.ontop.iq.IQTree;
import it.unibz.inf.ontop.iq.LeafIQTree;
import it.unibz.inf.ontop.iq.UnaryIQTree;
import it.unibz.inf.ontop.iq.node.*;
import it.unibz.inf.ontop.iq.tools.TypeConstantDictionary;
import it.unibz.inf.ontop.iq.transform.impl.DefaultRecursiveIQTreeVisitingTransformer;
import it.unibz.inf.ontop.iq.transformer.TermTypeTermLiftTransformer;
import it.unibz.inf.ontop.model.term.*;
import it.unibz.inf.ontop.model.term.functionsymbol.FunctionSymbolFactory;
import it.unibz.inf.ontop.model.term.functionsymbol.RDFTermTypeFunctionSymbol;
import it.unibz.inf.ontop.substitution.ImmutableSubstitution;
import it.unibz.inf.ontop.substitution.SubstitutionFactory;
import it.unibz.inf.ontop.utils.ImmutableCollectors;
import it.unibz.inf.ontop.utils.VariableGenerator;

import java.util.Collection;
import java.util.Map;
import java.util.Optional;
import java.util.stream.Stream;


/**
 * Lifts meta term type definitions are blocked by unions.
 *
 * Also makes sure that type terms are lifted above AggregationNode-s and DistinctNode-s.
 *
 */
public class DefaultTermTypeTermVisitingTreeTransformer
        extends DefaultRecursiveIQTreeVisitingTransformer implements TermTypeTermLiftTransformer {

    private final VariableGenerator variableGenerator;
    private final TypeConstantDictionary dictionary;
    private final TermFactory termFactory;
    private final Constant nullValue;
    private final SubstitutionFactory substitutionFactory;
    private final FunctionSymbolFactory functionSymbolFactory;

    @Inject
    protected DefaultTermTypeTermVisitingTreeTransformer(@Assisted VariableGenerator variableGenerator,
                                                       TermFactory termFactory,
                                                       IntermediateQueryFactory iqFactory,
                                                       TypeConstantDictionary typeConstantDictionary,
                                                       SubstitutionFactory substitutionFactory,
                                                       FunctionSymbolFactory functionSymbolFactory) {
        super(iqFactory);
        this.variableGenerator = variableGenerator;
        this.dictionary = typeConstantDictionary;
        this.termFactory = termFactory;
        this.nullValue = termFactory.getNullConstant();
        this.substitutionFactory = substitutionFactory;
        this.functionSymbolFactory = functionSymbolFactory;
    }

    @Override
    public IQTree transform(IQTree tree) {
        // Makes sure the tree is already normalized before transforming it
        return tree.normalizeForOptimization(variableGenerator)
                .acceptTransformer(this);
    }


    @Override
    public IQTree transformUnion(IQTree tree, UnionNode rootNode, ImmutableList<IQTree> children) {
        // Recursive (children are normalized separately)
        ImmutableList<IQTree> normalizedChildren = children.stream()
                .map(c -> c.acceptTransformer(this))
                // RDF type constants are replaced by functional terms
                .map(this::replaceTypeTermConstants)
                .collect(ImmutableCollectors.toList());

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

        ImmutableList<IQTree> newChildren = normalizedChildren.stream()
                .map(c -> enforceUsageOfCommonTypeFunctionSymbol(c, typeFunctionSymbolMap))
                .collect(ImmutableCollectors.toList());

        return iqFactory.createNaryIQTree(rootNode, newChildren)
                // Lifts the RDFTermTypeFunctionSymbols at the root of child definitions
                .normalizeForOptimization(variableGenerator);
    }

    private IQTree replaceTypeTermConstants(IQTree child) {
        return Optional.of(child.getRootNode())
                .filter(n -> n instanceof ConstructionNode)
                .map(n -> (ConstructionNode)n)
                .map(ConstructionNode::getSubstitution)
                .filter(s -> !s.isEmpty())
                .map(s -> s.transform(this::replaceTypeTermConstants))
                .map(s -> iqFactory.createConstructionNode(child.getVariables(), s))
                .filter(n -> !n.equals(child.getRootNode()))
                .map(n -> (IQTree) iqFactory.createUnaryIQTree(n, ((UnaryIQTree)child).getChild()))
                .orElse(child);
    }

    /**
     * Recursive
     *
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
        return Optional.of(child.getRootNode())
                .filter(n -> n instanceof ConstructionNode)
                .map(n -> (ConstructionNode) n)
                .flatMap(c -> Optional.ofNullable(c.getSubstitution().get(variable)));
    }

    private Stream<RDFTermTypeConstant> extractPossibleTermTypeConstants(Variable variable, IQTree child) {
        ImmutableTerm definition = extractDefinition(variable, child)
                .orElseThrow(() ->  new UnexpectedlyFormattedIQTreeException(
                        "Was expecting the child to define the blocked definition of the RDF term type variable"));

        if (definition.equals(nullValue))
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
                .map(f -> (RDFTermTypeFunctionSymbol)f)
                .orElseThrow(() -> new UnexpectedlyFormattedIQTreeException("Was expecting the definition to be a " +
                        "RDFTermTypeFunctionSymbol"));
        return functionSymbol.getConversionMap().values().stream();
    }

    private IQTree enforceUsageOfCommonTypeFunctionSymbol(IQTree tree,
                                                          ImmutableMap<Variable, RDFTermTypeFunctionSymbol> typeFunctionSymbolMap) {
        ConstructionNode initialConstructionNode = Optional.of(tree.getRootNode())
                .filter(n -> n instanceof ConstructionNode)
                .map(n -> (ConstructionNode) n)
                .orElseThrow(() -> new UnexpectedlyFormattedIQTreeException(
                        "Was expecting the child to start with a ConstructionNode"));

        ImmutableSubstitution<ImmutableTerm> newSubstitution = initialConstructionNode.getSubstitution().transform(
                (k, v) -> Optional.ofNullable(typeFunctionSymbolMap.get(k))
                        // RDF type definition
                        .map(functionSymbol -> enforceUsageOfCommonTypeFunctionSymbol(v, functionSymbol))
                        // Regular definition
                        .orElse(v));

        ConstructionNode newConstructionNode = iqFactory.createConstructionNode(tree.getVariables(),newSubstitution);

        return iqFactory.createUnaryIQTree(newConstructionNode, ((UnaryIQTree)tree).getChild());
    }

    private ImmutableTerm enforceUsageOfCommonTypeFunctionSymbol(ImmutableTerm definition, RDFTermTypeFunctionSymbol functionSymbol) {
        if (definition.isNull())
            return termFactory.getImmutableFunctionalTerm(functionSymbol, definition);
        else if ((definition instanceof ImmutableFunctionalTerm)
                && ((ImmutableFunctionalTerm) definition).getFunctionSymbol() instanceof RDFTermTypeFunctionSymbol){
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
        ImmutableSet<Variable> metaTermTypeVariables = getMetaTermTypeVariables(valuesNode);

        if (metaTermTypeVariables.isEmpty())
                return transformLeaf(valuesNode);

        ImmutableSet<RDFTermTypeConstant> possibleConstants = getPossibleConstants(valuesNode);

        return createConstructionValuesTree(valuesNode, metaTermTypeVariables, possibleConstants);
    }

    private ImmutableSet<Variable> getMetaTermTypeVariables(ValuesNode valuesNode) {
        return valuesNode.getOrderedVariables().stream()
                .filter(variable -> valuesNode.getValueStream(variable)
                                            .anyMatch(constant -> constant instanceof RDFTermTypeConstant))
                .collect(ImmutableCollectors.toSet());
    }

    private ImmutableSet<RDFTermTypeConstant> getPossibleConstants(ValuesNode valuesNode) {
        ImmutableList<ImmutableList<Constant>>  values = valuesNode.getValues();
        return values.stream()
                .map(tuple -> tuple.stream()
                        .filter(constant -> constant instanceof RDFTermTypeConstant)
                        .map(constant -> (RDFTermTypeConstant) constant)
                        .collect(ImmutableCollectors.toSet()))
                .flatMap(Collection::stream)
                .collect(ImmutableCollectors.toSet());
    }

    IQTree createConstructionValuesTree(ValuesNode valuesNode,
                                        ImmutableSet<Variable> metaTermTypeVariables,
                                        ImmutableSet<RDFTermTypeConstant> possibleConstants) {

        ImmutableList<Variable> orderedVariables = valuesNode.getOrderedVariables();
        ImmutableList<ImmutableList<Constant>> values = valuesNode.getValues();

        ImmutableMap<Variable, Variable> generatedVariableNamesMap = orderedVariables.stream().collect(ImmutableCollectors.toMap(
                key -> key,
                key -> variableGenerator.generateNewVariable()));

        ValuesNode newValuesNode = iqFactory.createValuesNode(
                orderedVariables.stream()
                        .map(variable -> metaTermTypeVariables.contains(variable)
                                            ? generatedVariableNamesMap.get(variable)
                                            : variable)
                        .collect(ImmutableCollectors.toList()),
                values.stream()
                        .map(tuple -> tuple.stream()
                                .map(this::replaceTypeTermConstantWithFunctionalTerm)
                                .collect(ImmutableCollectors.toList()))
                        .collect(ImmutableCollectors.toList()));

        ConstructionNode newConstructionNode = iqFactory.createConstructionNode(
                ImmutableSet.copyOf(orderedVariables),
                substitutionFactory.getSubstitution(
                        metaTermTypeVariables.stream()
                                .collect(ImmutableCollectors.toMap(
                                        variable -> variable,
                                        variable -> termFactory.getRDFTermTypeFunctionalTerm(
                                                        generatedVariableNamesMap.get(variable),
                                                        dictionary,
                                                        possibleConstants,
                                                        false)))));

        return iqFactory.createUnaryIQTree(newConstructionNode, newValuesNode);
    }


    private Constant replaceTypeTermConstantWithFunctionalTerm(Constant constant) {
        if (constant instanceof RDFTermTypeConstant) {
            RDFTermTypeConstant typeConstant = (RDFTermTypeConstant) constant;
            return dictionary.convert(typeConstant);
        }
        else
            return constant;
    }






    @Override
    public IQTree transformDistinct(IQTree tree, DistinctNode rootNode, IQTree child) {
        return transformNodeBlockingNonInjectiveBindings(rootNode, child);
    }

    @Override
    public IQTree transformAggregation(IQTree tree, AggregationNode rootNode, IQTree child) {
        return transformNodeBlockingNonInjectiveBindings(rootNode, child);
    }

    /**
     * Makes sure the child does not project a type term anymore.
     *
     * Useful for dealing with COALESCE and CASEs
     *
     */
    private IQTree transformNodeBlockingNonInjectiveBindings(UnaryOperatorNode rootNode, IQTree child) {
        IQTree normalizedChild = replaceTypeTermConstants(
                child.acceptTransformer(this));

        return iqFactory.createUnaryIQTree(rootNode, normalizedChild)
                .normalizeForOptimization(variableGenerator);
    }

    protected IQTree transformLeaf(LeafIQTree leaf){
        return leaf.normalizeForOptimization(variableGenerator);
    }

    protected IQTree transformUnaryNode(IQTree tree, UnaryOperatorNode rootNode, IQTree child) {
        return super.transformUnaryNode(tree, rootNode, child)
                .normalizeForOptimization(variableGenerator);
    }

    protected IQTree transformNaryCommutativeNode(IQTree tree, NaryOperatorNode rootNode, ImmutableList<IQTree> children) {
        return super.transformNaryCommutativeNode(tree, rootNode, children)
                .normalizeForOptimization(variableGenerator);
    }

    protected IQTree transformBinaryNonCommutativeNode(IQTree tree, BinaryNonCommutativeOperatorNode rootNode, IQTree leftChild, IQTree rightChild) {
        return super.transformBinaryNonCommutativeNode(tree, rootNode, leftChild, rightChild)
                .normalizeForOptimization(variableGenerator);
    }


    private static class UnexpectedlyFormattedIQTreeException extends OntopInternalBugException {

        protected UnexpectedlyFormattedIQTreeException(String message) {
            super(message);
        }
    }

}
