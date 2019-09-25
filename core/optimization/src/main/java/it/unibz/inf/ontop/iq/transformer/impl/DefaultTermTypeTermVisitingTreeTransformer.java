package it.unibz.inf.ontop.iq.transformer.impl;

import com.google.common.collect.*;
import com.google.inject.Inject;
import com.google.inject.assistedinject.Assisted;
import it.unibz.inf.ontop.exception.MinorOntopInternalBugException;
import it.unibz.inf.ontop.exception.OntopInternalBugException;
import it.unibz.inf.ontop.injection.IntermediateQueryFactory;

import it.unibz.inf.ontop.iq.IQTree;
import it.unibz.inf.ontop.iq.LeafIQTree;
import it.unibz.inf.ontop.iq.NaryIQTree;
import it.unibz.inf.ontop.iq.UnaryIQTree;
import it.unibz.inf.ontop.iq.node.*;
import it.unibz.inf.ontop.iq.tools.TypeConstantDictionary;
import it.unibz.inf.ontop.iq.transform.impl.DefaultRecursiveIQTreeVisitingTransformer;
import it.unibz.inf.ontop.iq.transformer.TermTypeTermLiftTransformer;
import it.unibz.inf.ontop.model.term.*;
import it.unibz.inf.ontop.model.term.functionsymbol.RDFTermTypeFunctionSymbol;
import it.unibz.inf.ontop.substitution.ImmutableSubstitution;
import it.unibz.inf.ontop.substitution.InjectiveVar2VarSubstitution;
import it.unibz.inf.ontop.substitution.SubstitutionFactory;
import it.unibz.inf.ontop.utils.ImmutableCollectors;
import it.unibz.inf.ontop.utils.VariableGenerator;

import java.util.Map;
import java.util.Optional;
import java.util.stream.Stream;


/**
 * Meta term type definitions are blocked by unions
 *
 * TODO: explain it further
 */
public class DefaultTermTypeTermVisitingTreeTransformer
        extends DefaultRecursiveIQTreeVisitingTransformer implements TermTypeTermLiftTransformer {

    private final VariableGenerator variableGenerator;
    private final TypeConstantDictionary dictionary;
    private final TermFactory termFactory;
    private final Constant nullValue;
    private final SubstitutionFactory substitutionFactory;

    @Inject
    private DefaultTermTypeTermVisitingTreeTransformer(@Assisted VariableGenerator variableGenerator,
                                                       TermFactory termFactory,
                                                       IntermediateQueryFactory iqFactory,
                                                       TypeConstantDictionary typeConstantDictionary,
                                                       SubstitutionFactory substitutionFactory) {
        super(iqFactory);
        this.variableGenerator = variableGenerator;
        this.dictionary = typeConstantDictionary;
        this.termFactory = termFactory;
        this.nullValue = termFactory.getNullConstant();
        this.substitutionFactory = substitutionFactory;
    }

    @Override
    public IQTree transform(IQTree tree) {
        // Makes sure the tree is already normalized before transforming it
        return tree.normalizeForOptimization(variableGenerator)
                .acceptTransformer(this);
    }


    @Override
    public IQTree transformUnion(IQTree tree, UnionNode rootNode, ImmutableList<IQTree> children) {
        // Recursive
        ImmutableList<IQTree> liftedChildren = children.stream()
                .map(c -> c.acceptTransformer(this))
                .collect(ImmutableCollectors.toList());

        ImmutableSet<Variable> metaTermTypeVariables = rootNode.getVariables().stream()
                .filter(v -> liftedChildren.stream()
                        .anyMatch(c -> isRDFTermTypeVariable(v, c)
                                .filter(b -> b)
                                .isPresent()))
                .collect(ImmutableCollectors.toSet());

        if (metaTermTypeVariables.isEmpty())
            return iqFactory.createNaryIQTree(rootNode, liftedChildren)
                    .normalizeForOptimization(variableGenerator);

        ImmutableMultimap<Variable, RDFTermTypeConstant> possibleConstantMultimap =
                metaTermTypeVariables.stream()
                        .flatMap(v -> liftedChildren.stream()
                            .flatMap(child -> extractPossibleTermTypeConstants(v, child))
                            .map(c -> Maps.immutableEntry(v, c)))
                        .distinct()
                        .collect(ImmutableCollectors.toMultimap());

        // Meta RDF term type variable -> new integer variable
        ImmutableMap<Variable, Variable> integerVariableMap = metaTermTypeVariables.stream()
                .collect(ImmutableCollectors.toMap(v -> v,
                        variableGenerator::generateNewVariableFromVar));

        ImmutableSet<Variable> newUnionVariables = rootNode.getVariables().stream()
                .map(v -> Optional.ofNullable(integerVariableMap.get(v))
                        .orElse(v))
                .collect(ImmutableCollectors.toSet());

        ImmutableList<IQTree> newChildren = liftedChildren.stream()
                .map(c -> transformUnionChild(c, integerVariableMap, newUnionVariables))
                .collect(ImmutableCollectors.toList());

        NaryIQTree newUnionTree = iqFactory.createNaryIQTree(
                iqFactory.createUnionNode(newUnionVariables),
                newChildren);

        ImmutableSubstitution<ImmutableTerm> newSubstitution = createNewUnionSubstitution(possibleConstantMultimap,
                integerVariableMap);

        return iqFactory.createUnaryIQTree(
                iqFactory.createConstructionNode(rootNode.getVariables(), newSubstitution),
                newUnionTree);
    }

    private Optional<Boolean> isRDFTermTypeVariable(Variable variable, IQTree unionChild) {
        return extractDefinition(variable, unionChild)
                .map(definition -> {
                    if (definition instanceof RDFTermTypeConstant)
                        return Optional.of(true);
                    else if (definition.equals(nullValue))
                        return Optional.<Boolean>empty();
                    else if (definition instanceof ImmutableFunctionalTerm)
                        return Optional.of(((ImmutableFunctionalTerm) definition).getFunctionSymbol() instanceof RDFTermTypeFunctionSymbol);
                    else
                        return Optional.of(false);
                })
                // RDF term type variables are expected to be blocked by the Union
                .orElseGet(() -> Optional.of(false));
    }

    private Optional<ImmutableTerm> extractDefinition(Variable variable, IQTree unionChild) {
        return Optional.of(unionChild.getRootNode())
                .filter(n -> n instanceof ConstructionNode)
                .map(n -> (ConstructionNode) n)
                .flatMap(c -> Optional.ofNullable(c.getSubstitution().get(variable)));
    }

    private Stream<RDFTermTypeConstant> extractPossibleTermTypeConstants(Variable variable, IQTree child) {
        ImmutableTerm definition = extractDefinition(variable, child)
                .orElseThrow(() ->  new UnexpectedlyFormattedIQTreeException(
                        "Was expecting the child to define the blocked definition of the RDF term type variable"));

        if (definition instanceof RDFTermTypeConstant)
            return Stream.of((RDFTermTypeConstant) definition);
        else if (definition.equals(nullValue))
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

    private IQTree transformUnionChild(IQTree child, ImmutableMap<Variable, Variable> integerVariableMap,
                                       ImmutableSet<Variable> newProjectedVariables) {
        ConstructionNode initialConstructionNode = Optional.of(child.getRootNode())
                .filter(n -> n instanceof ConstructionNode)
                .map(n -> (ConstructionNode) n)
                .orElseThrow(() -> new UnexpectedlyFormattedIQTreeException(
                        "Was expecting the child to start with a ConstructionNode"));

        /*
         * All the definitions.
         *
         * Some of them may be propagated down (var-to-var when the second variable is not projected)
         *
         */
        ImmutableSubstitution<ImmutableTerm> newDefinitions = substitutionFactory.getSubstitution(
                initialConstructionNode.getSubstitution().getImmutableMap().entrySet().stream()
                        .collect(ImmutableCollectors.toMap(
                                e -> Optional.ofNullable(integerVariableMap.get(e.getKey())).orElseGet(e::getKey),
                                e -> integerVariableMap.containsKey(e.getKey())
                                        ? transformIntoIntegerDefinition(e.getValue())
                                        : e.getValue())));

        Optional<InjectiveVar2VarSubstitution> optionalSubstitutionToPropagateDown = Optional.of(
                newDefinitions.getImmutableMap().entrySet().stream()
                        .filter(e -> (e.getValue() instanceof Variable) && !newProjectedVariables.contains(e.getValue()))
                        // Inverse the entry
                        .map(e -> Maps.immutableEntry((Variable) e.getValue(), e.getKey()))
                        .collect(ImmutableCollectors.toMap()))
                .filter(m -> !m.isEmpty())
                .map(substitutionFactory::getInjectiveVar2VarSubstitution);

        IQTree grandChild = ((UnaryIQTree) child).getChild();
        IQTree newGrandChild = optionalSubstitutionToPropagateDown
                .map(s -> grandChild.applyDescendingSubstitution(s, Optional.empty()))
                .orElse(grandChild);

        ImmutableSubstitution<ImmutableTerm> newChildSubstitution = optionalSubstitutionToPropagateDown
                .map(prop -> prop.applyRenaming(newDefinitions))
                .orElse(newDefinitions);

        return iqFactory.createUnaryIQTree(
                iqFactory.createConstructionNode(newProjectedVariables, newChildSubstitution),
                newGrandChild);
    }

    private ImmutableTerm transformIntoIntegerDefinition(ImmutableTerm term) {
        if (term instanceof RDFTermTypeConstant)
            return dictionary.convert((RDFTermTypeConstant) term);

        else if ((term instanceof ImmutableFunctionalTerm)
                && (((ImmutableFunctionalTerm) term).getFunctionSymbol() instanceof RDFTermTypeFunctionSymbol)) {
            return ((ImmutableFunctionalTerm) term).getTerm(0);
        }
        else if ((term instanceof Constant) && term.isNull())
            return term;
        else
            throw new MinorOntopInternalBugException("Unexpected definition for RDFTermType term");
    }

    private ImmutableSubstitution<ImmutableTerm> createNewUnionSubstitution(
            ImmutableMultimap<Variable, RDFTermTypeConstant> possibleConstantMultimap,
            ImmutableMap<Variable, Variable> integerVariableMap) {
        return substitutionFactory.getSubstitution(
                possibleConstantMultimap.asMap().entrySet().stream()
                    .collect(ImmutableCollectors.toMap(
                            Map.Entry::getKey,
                            e -> termFactory.getRDFTermTypeFunctionalTerm(
                                    integerVariableMap.get(e.getKey()),
                                    dictionary, ImmutableSet.copyOf(e.getValue())))));
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
