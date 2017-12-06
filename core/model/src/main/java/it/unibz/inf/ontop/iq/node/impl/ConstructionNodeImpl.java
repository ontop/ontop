package it.unibz.inf.ontop.iq.node.impl;


import com.google.common.collect.ImmutableList;
import com.google.common.collect.ImmutableMap;
import com.google.common.collect.ImmutableSet;
import com.google.inject.assistedinject.Assisted;
import com.google.inject.assistedinject.AssistedInject;
import it.unibz.inf.ontop.evaluator.TermNullabilityEvaluator;
import it.unibz.inf.ontop.exception.MinorOntopInternalBugException;
import it.unibz.inf.ontop.injection.IntermediateQueryFactory;
import it.unibz.inf.ontop.iq.IQTree;
import it.unibz.inf.ontop.iq.IntermediateQuery;
import it.unibz.inf.ontop.iq.UnaryIQTree;
import it.unibz.inf.ontop.iq.exception.InvalidIntermediateQueryException;
import it.unibz.inf.ontop.iq.exception.InvalidQueryNodeException;
import it.unibz.inf.ontop.iq.exception.QueryNodeSubstitutionException;
import it.unibz.inf.ontop.iq.exception.QueryNodeTransformationException;
import it.unibz.inf.ontop.iq.impl.DefaultSubstitutionResults;
import it.unibz.inf.ontop.iq.node.*;
import it.unibz.inf.ontop.iq.node.impl.ConstructionNodeTools.NewSubstitutionPair;
import it.unibz.inf.ontop.iq.transform.node.HeterogeneousQueryNodeTransformer;
import it.unibz.inf.ontop.iq.transform.node.HomogeneousQueryNodeTransformer;
import it.unibz.inf.ontop.substitution.SubstitutionFactory;
import it.unibz.inf.ontop.substitution.VariableOrGroundTermSubstitution;
import it.unibz.inf.ontop.substitution.impl.ImmutableSubstitutionTools;
import it.unibz.inf.ontop.substitution.impl.ImmutableUnificationTools;
import it.unibz.inf.ontop.model.term.functionsymbol.BNodePredicate;
import it.unibz.inf.ontop.model.term.functionsymbol.ExpressionOperation;
import it.unibz.inf.ontop.model.term.functionsymbol.Predicate;
import it.unibz.inf.ontop.model.term.functionsymbol.URITemplatePredicate;
import it.unibz.inf.ontop.model.term.*;
import it.unibz.inf.ontop.substitution.ImmutableSubstitution;
import it.unibz.inf.ontop.utils.ImmutableCollectors;
import it.unibz.inf.ontop.utils.VariableGenerator;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.AbstractMap;
import java.util.Map;
import java.util.Optional;
import java.util.stream.Stream;

@SuppressWarnings({"OptionalUsedAsFieldOrParameterType", "BindingAnnotationWithoutInject"})
public class ConstructionNodeImpl extends QueryNodeImpl implements ConstructionNode {

    private static Logger LOGGER = LoggerFactory.getLogger(ConstructionNodeImpl.class);
    @SuppressWarnings("FieldCanBeLocal")
    private static int CONVERGENCE_BOUND = 5;

    private final Optional<ImmutableQueryModifiers> optionalModifiers;
    private final TermNullabilityEvaluator nullabilityEvaluator;
    private final ImmutableSet<Variable> projectedVariables;
    private final ImmutableSubstitution<ImmutableTerm> substitution;

    private final ImmutableUnificationTools unificationTools;
    private final ConstructionNodeTools constructionNodeTools;
    private final ImmutableSubstitutionTools substitutionTools;
    private final SubstitutionFactory substitutionFactory;
    private final IntermediateQueryFactory iqFactory;

    private static final String CONSTRUCTION_NODE_STR = "CONSTRUCT";
    private final TermFactory termFactory;
    private final ValueConstant nullValue;

    @AssistedInject
    private ConstructionNodeImpl(@Assisted ImmutableSet<Variable> projectedVariables,
                                 @Assisted ImmutableSubstitution<ImmutableTerm> substitution,
                                 @Assisted Optional<ImmutableQueryModifiers> optionalQueryModifiers,
                                 TermNullabilityEvaluator nullabilityEvaluator,
                                 ImmutableUnificationTools unificationTools, ConstructionNodeTools constructionNodeTools,
                                 ImmutableSubstitutionTools substitutionTools, SubstitutionFactory substitutionFactory,
                                 TermFactory termFactory, IntermediateQueryFactory iqFactory) {
        this.projectedVariables = projectedVariables;
        this.substitution = substitution;
        this.optionalModifiers = optionalQueryModifiers;
        this.nullabilityEvaluator = nullabilityEvaluator;
        this.unificationTools = unificationTools;
        this.constructionNodeTools = constructionNodeTools;
        this.substitutionTools = substitutionTools;
        this.substitutionFactory = substitutionFactory;
        this.termFactory = termFactory;
        this.nullValue = termFactory.getNullConstant();
        this.iqFactory = iqFactory;

        validate();
    }

    private void validate() {
        // The substitution domain must be a subset of the projectedVariables
        if (!projectedVariables.containsAll(substitution.getDomain())) {
            throw new InvalidQueryNodeException("ConstructionNode: all the domain variables " +
                    "of the substitution must be projected.\n" + toString());
        }

        // Substitution to non-projected variables is incorrect
        if (substitution.getImmutableMap().values().stream()
                .filter(v -> v instanceof Variable)
                .map(v -> (Variable) v)
                .anyMatch(v -> !projectedVariables.contains(v))) {
            throw new InvalidQueryNodeException(
                    "ConstructionNode: substituting a variable " +
                            "by a non-projected variable is incorrect.\n"
                + toString());
        }
    }

    /**
     * Without modifiers nor substitution.
     */
    @AssistedInject
    private ConstructionNodeImpl(@Assisted ImmutableSet<Variable> projectedVariables,
                                 TermNullabilityEvaluator nullabilityEvaluator,
                                 ImmutableUnificationTools unificationTools,
                                 ConstructionNodeTools constructionNodeTools,
                                 ImmutableSubstitutionTools substitutionTools, SubstitutionFactory substitutionFactory,
                                 TermFactory termFactory, IntermediateQueryFactory iqFactory) {
        this.projectedVariables = projectedVariables;
        this.nullabilityEvaluator = nullabilityEvaluator;
        this.unificationTools = unificationTools;
        this.substitutionTools = substitutionTools;
        this.substitution = substitutionFactory.getSubstitution();
        this.termFactory = termFactory;
        this.iqFactory = iqFactory;
        this.optionalModifiers = Optional.empty();
        this.constructionNodeTools = constructionNodeTools;
        this.substitutionFactory = substitutionFactory;
        this.nullValue = termFactory.getNullConstant();

        validate();
    }

    @AssistedInject
    private ConstructionNodeImpl(@Assisted ImmutableSet<Variable> projectedVariables,
                                 @Assisted ImmutableSubstitution<ImmutableTerm> substitution,
                                 TermNullabilityEvaluator nullabilityEvaluator,
                                 ImmutableUnificationTools unificationTools, ConstructionNodeTools constructionNodeTools,
                                 ImmutableSubstitutionTools substitutionTools, SubstitutionFactory substitutionFactory,
                                 TermFactory termFactory, IntermediateQueryFactory iqFactory) {
        this.projectedVariables = projectedVariables;
        this.substitution = substitution;
        this.nullabilityEvaluator = nullabilityEvaluator;
        this.unificationTools = unificationTools;
        this.constructionNodeTools = constructionNodeTools;
        this.substitutionTools = substitutionTools;
        this.substitutionFactory = substitutionFactory;
        this.termFactory = termFactory;
        this.iqFactory = iqFactory;
        this.optionalModifiers = Optional.empty();
        this.nullValue = termFactory.getNullConstant();

        validate();
    }

    @Override
    public ImmutableSet<Variable> getVariables() {
        return projectedVariables;
    }

    @Override
    public ImmutableSubstitution<ImmutableTerm> getSubstitution() {
        return substitution;
    }

    @Override
    public Optional<ImmutableQueryModifiers> getOptionalModifiers() {
        return optionalModifiers;
    }

    /**
     * Immutable fields, can be shared.
     */
    @Override
    public ConstructionNode clone() {
        return new ConstructionNodeImpl(projectedVariables, substitution, optionalModifiers, nullabilityEvaluator,
                unificationTools, constructionNodeTools, substitutionTools, substitutionFactory, termFactory,
                iqFactory);
    }

    @Override
    public ConstructionNode acceptNodeTransformer(HomogeneousQueryNodeTransformer transformer)
            throws QueryNodeTransformationException {
        return transformer.transform(this);
    }

    @Override
    public ImmutableSet<Variable> getChildVariables() {
        ImmutableSet<Variable> variableDefinedByBindings = substitution.getDomain();

        Stream<Variable> variablesRequiredByBindings = substitution.getImmutableMap().values().stream()
                .flatMap(ImmutableTerm::getVariableStream);

        //return only the variables that are also used in the bindings for the child of the construction node
        return Stream.concat(projectedVariables.stream(), variablesRequiredByBindings)
                .filter(v -> !variableDefinedByBindings.contains(v))
                .collect(ImmutableCollectors.toSet());
    }

    @Override
    public NodeTransformationProposal acceptNodeTransformer(HeterogeneousQueryNodeTransformer transformer) {
        return transformer.transform(this);
    }

    @Override
    public ImmutableSet<Variable> getLocalVariables() {
        ImmutableSet.Builder<Variable> collectedVariableBuilder = ImmutableSet.builder();

        collectedVariableBuilder.addAll(projectedVariables);

        ImmutableMap<Variable, ImmutableTerm> substitutionMap = substitution.getImmutableMap();

        collectedVariableBuilder.addAll(substitutionMap.keySet());
        for (ImmutableTerm term : substitutionMap.values()) {
            if (term instanceof Variable) {
                collectedVariableBuilder.add((Variable)term);
            }
            else if (term instanceof ImmutableFunctionalTerm) {
                collectedVariableBuilder.addAll(((ImmutableFunctionalTerm)term).getVariables());
            }
        }

        return collectedVariableBuilder.build();
    }

    @Override
    public ImmutableSubstitution<ImmutableTerm> getDirectBindingSubstitution() {
        if (substitution.isEmpty())
            return substitution;

        // Non-final
        ImmutableSubstitution<ImmutableTerm> previousSubstitution;
        // Non-final
        ImmutableSubstitution<ImmutableTerm> newSubstitution = substitution;

        int i = 0;
        do {
            previousSubstitution = newSubstitution;
            newSubstitution = newSubstitution.composeWith(substitution);
            i++;
        } while ((i < CONVERGENCE_BOUND) && (!previousSubstitution.equals(newSubstitution)));

        if (i == CONVERGENCE_BOUND) {
            LOGGER.warn(substitution + " has not converged after " + CONVERGENCE_BOUND + " recursions over itself");
        }

        return newSubstitution;

    }

    /**
     * Creates a new ConstructionNode with a new substitution.
     *
     * Stops the propagation.
     *
     */
    @Override
    public SubstitutionResults<ConstructionNode> applyAscendingSubstitution(
            ImmutableSubstitution<? extends ImmutableTerm> substitutionToApply,
            QueryNode childNode, IntermediateQuery query) {

        ImmutableSubstitution<ImmutableTerm> newSubstitution = mergeWithAscendingSubstitution(substitutionToApply);

        ConstructionNode newConstructionNode = new ConstructionNodeImpl(projectedVariables,
                newSubstitution, getOptionalModifiers(), nullabilityEvaluator, unificationTools, constructionNodeTools,
                substitutionTools, substitutionFactory, termFactory, iqFactory);

        /*
         * Stops to propagate the substitution
         */
        return DefaultSubstitutionResults.newNode(newConstructionNode);
    }

    /**
     * Merges the current substitution with the ascending one
     *
     * This substitution is obtained by composition and then cleaned (only defines the projected variables)
     *
     * Note that expects that the substitution does not rename a projected variable
     * into a non-projected one (this would produce an invalid construction node).
     * That is the responsibility of the SubstitutionPropagationExecutor
     * to prevent such bindings from appearing.
     */
    private ImmutableSubstitution<ImmutableTerm> mergeWithAscendingSubstitution(
            ImmutableSubstitution<? extends ImmutableTerm> substitutionToApply) {
        ImmutableSubstitution<ImmutableTerm> localSubstitution = getSubstitution();
        ImmutableSet<Variable> boundVariables = localSubstitution.getImmutableMap().keySet();

        if (substitutionToApply.getImmutableMap().keySet().stream().anyMatch(boundVariables::contains)) {
            throw new IllegalArgumentException("An ascending substitution MUST NOT include variables bound by " +
                    "the substitution of the current construction node");
        }

        ImmutableSubstitution<ImmutableTerm> compositeSubstitution = substitutionToApply.composeWith(localSubstitution);

        /*
         * Cleans the composite substitution by removing non-projected variables
         */

        ImmutableMap.Builder<Variable, ImmutableTerm> newSubstitutionMapBuilder = ImmutableMap.builder();
        compositeSubstitution.getImmutableMap().entrySet().stream()
                .map(this::applyNullNormalization)
                .filter(e -> projectedVariables.contains(e.getKey()))
                .forEach(newSubstitutionMapBuilder::put);

         return substitutionFactory.getSubstitution(newSubstitutionMapBuilder.build());

    }

    /**
     * Most functional terms do not accept NULL as arguments. If this happens, they become NULL.
     */
    private Map.Entry<Variable, ImmutableTerm> applyNullNormalization(
            Map.Entry<Variable, ImmutableTerm> substitutionEntry) {
        ImmutableTerm value = substitutionEntry.getValue();
        if (value instanceof ImmutableFunctionalTerm) {
            ImmutableTerm newValue = normalizeFunctionalTerm((ImmutableFunctionalTerm) value);
            return newValue.equals(value)
                    ? substitutionEntry
                    : new AbstractMap.SimpleEntry<>(substitutionEntry.getKey(), newValue);
        }
        return substitutionEntry;
    }

    private ImmutableTerm normalizeFunctionalTerm(ImmutableFunctionalTerm functionalTerm) {
        if (isSupportingNullArguments(functionalTerm)) {
            return functionalTerm;
        }

        ImmutableList<ImmutableTerm> newArguments = functionalTerm.getArguments().stream()
                .map(arg -> (arg instanceof ImmutableFunctionalTerm)
                        ? normalizeFunctionalTerm((ImmutableFunctionalTerm) arg)
                        : arg)
                .collect(ImmutableCollectors.toList());
        if (newArguments.stream()
                .anyMatch(arg -> arg.equals(nullValue))) {
            return nullValue;
        }

        return termFactory.getImmutableFunctionalTerm(functionalTerm.getFunctionSymbol(), newArguments);
    }

    /**
     * TODO: move it elsewhere
     */
    private static boolean isSupportingNullArguments(ImmutableFunctionalTerm functionalTerm) {
        Predicate functionSymbol = functionalTerm.getFunctionSymbol();
        if (functionSymbol instanceof ExpressionOperation) {
            switch((ExpressionOperation)functionSymbol) {
                case IS_NOT_NULL:
                case IS_NULL:
                    // TODO: add COALESCE, EXISTS, NOT EXISTS
                    return true;
                default:
                    return false;
            }
        }
        else if ((functionSymbol instanceof URITemplatePredicate)
                || (functionSymbol instanceof BNodePredicate)) {
            return false;
        }
        return true;
    }


    /**
     * TODO: explain
     */
    @Override
    public SubstitutionResults<ConstructionNode> applyDescendingSubstitution(
            ImmutableSubstitution<? extends ImmutableTerm> descendingSubstitution, IntermediateQuery query) {
        return applyDescendingSubstitution(descendingSubstitution);
    }

    private SubstitutionResults<ConstructionNode> applyDescendingSubstitution(
            ImmutableSubstitution<? extends ImmutableTerm> descendingSubstitution) {

        ImmutableSubstitution<ImmutableTerm> relevantSubstitution = constructionNodeTools.extractRelevantDescendingSubstitution(
                descendingSubstitution, projectedVariables);

        ImmutableSet<Variable> newProjectedVariables = constructionNodeTools.computeNewProjectedVariables(relevantSubstitution,
                getVariables());

        /*
         * TODO: avoid using an exception
         */
        NewSubstitutionPair newSubstitutions;
        try {
            newSubstitutions = constructionNodeTools.traverseConstructionNode(relevantSubstitution, substitution, projectedVariables,
                    newProjectedVariables);
        } catch (QueryNodeSubstitutionException e) {
            return DefaultSubstitutionResults.declareAsEmpty();
        }

        ImmutableSubstitution<? extends ImmutableTerm> substitutionToPropagate = newSubstitutions.propagatedSubstitution;

        Optional<ImmutableQueryModifiers> newOptionalModifiers = updateOptionalModifiers(optionalModifiers,
                descendingSubstitution, substitutionToPropagate);

        /*
         * The construction node is not needed anymore
         *
         * Currently, the root construction node is still required.
         */
        if (newSubstitutions.bindings.isEmpty() && !newOptionalModifiers.isPresent()) {
            return DefaultSubstitutionResults.replaceByUniqueChild(substitutionToPropagate);
        }

        /*
         * New construction node
         */
        else {
            ConstructionNode newConstructionNode = new ConstructionNodeImpl(newProjectedVariables,
                    newSubstitutions.bindings, newOptionalModifiers, nullabilityEvaluator, unificationTools, constructionNodeTools,
                    substitutionTools, substitutionFactory, termFactory, iqFactory);

            return DefaultSubstitutionResults.newNode(newConstructionNode, substitutionToPropagate);
        }
    }

    @Override
    public boolean isVariableNullable(IntermediateQuery query, Variable variable) {
        if (getChildVariables().contains(variable))
            return isChildVariableNullable(query, variable);

        return Optional.ofNullable(substitution.get(variable))
                .map(t -> isTermNullable(query, t))
                .orElseThrow(() -> new IllegalArgumentException("The variable " + variable + " is not projected by " + this));
    }

    private boolean isChildVariableNullable(IntermediateQuery query, Variable variable) {
        return query.getFirstChild(this)
                .map(c -> c.isVariableNullable(query, variable))
                .orElseThrow(() -> new InvalidIntermediateQueryException(
                        "A construction node with child variables must have a child"));
    }

    private boolean isTermNullable(IntermediateQuery query, ImmutableTerm substitutionValue) {
        if (substitutionValue instanceof ImmutableFunctionalTerm) {
            ImmutableSet<Variable> nullableVariables = substitutionValue.getVariableStream()
                    .filter(v -> isChildVariableNullable(query, v))
                    .collect(ImmutableCollectors.toSet());
            return nullabilityEvaluator.isNullable(substitutionValue, nullableVariables);

        }
        else if (substitutionValue instanceof Constant) {
            return substitutionValue.equals(nullValue);
        }
        else if (substitutionValue instanceof Variable) {
            return isChildVariableNullable(query, (Variable)substitutionValue);
        }
        else {
            throw new IllegalStateException("Unexpected immutable term");
        }
    }

    @Override
    public boolean isSyntacticallyEquivalentTo(QueryNode node) {
        return Optional.of(node)
                .filter(n -> n instanceof ConstructionNode)
                .map(n -> (ConstructionNode) n)
                .filter(n -> n.getVariables().equals(projectedVariables))
                .filter(n -> n.getSubstitution().equals(substitution))
                .filter(n -> n.getOptionalModifiers().equals(optionalModifiers))
                .isPresent();
    }

    @Override
    public NodeTransformationProposal reactToEmptyChild(IntermediateQuery query, EmptyNode emptyChild) {
        /*
         * A construction node has only one child
         */
        return new NodeTransformationProposalImpl(NodeTransformationProposedState.DECLARE_AS_EMPTY, projectedVariables);
    }

    @Override
    public NodeTransformationProposal reactToTrueChildRemovalProposal(IntermediateQuery query, TrueNode trueNode) {
        if (this.getVariables().isEmpty() && !this.equals(query.getRootNode())){
           return new NodeTransformationProposalImpl(NodeTransformationProposedState.DECLARE_AS_TRUE, ImmutableSet.of());
        }
       return new NodeTransformationProposalImpl(NodeTransformationProposedState.NO_LOCAL_CHANGE, ImmutableSet.of());
    }

    @Override
    public ImmutableSet<Variable> getLocallyRequiredVariables() {
        return getChildVariables();
    }

    @Override
    public ImmutableSet<Variable> getRequiredVariables(IntermediateQuery query) {
        return getLocallyRequiredVariables();
    }

    @Override
    public ImmutableSet<Variable> getLocallyDefinedVariables() {
        return substitution.getDomain();
    }

    @Override
    public void acceptVisitor(QueryNodeVisitor visitor) {
        visitor.visit(this);
    }

    @Override
    public String toString() {
        // TODO: display the query modifiers
        return CONSTRUCTION_NODE_STR + " " + projectedVariables + " " + "[" + substitution + "]" ;
    }

    @Override
    public IQTree liftBinding(IQTree childIQTree, VariableGenerator variableGenerator) {
        IQTree liftedChildIQTree = childIQTree.liftBinding(variableGenerator);
        QueryNode liftedChildRoot = liftedChildIQTree.getRootNode();
        if (liftedChildRoot instanceof ConstructionNode)
            return liftBinding((ConstructionNode) liftedChildRoot, (UnaryIQTree) liftedChildIQTree);
        else if (liftedChildIQTree.isDeclaredAsEmpty()) {
            return iqFactory.createEmptyNode(projectedVariables);
        }
        else
            return iqFactory.createUnaryIQTree(this, liftedChildIQTree, true);
    }

    /**
     * TODO: refactor
     *
     * TODO: handle the constraint
     *
     */
    @Override
    public IQTree applyDescendingSubstitution(
            VariableOrGroundTermSubstitution<? extends VariableOrGroundTerm> descendingSubstitution,
            Optional<ImmutableExpression> constraint, IQTree child) {
        SubstitutionResults<ConstructionNode> results = applyDescendingSubstitution(descendingSubstitution);

        IQTree updatedChild = results.getSubstitutionToPropagate()
                .map(s -> (ImmutableSubstitution<? extends VariableOrGroundTerm>) (ImmutableSubstitution<?>) s)
                .map(this::convertVariableOrGroundTermSubstitution)
                .map(s -> child.applyDescendingSubstitution(s, constraint))
                .orElse(child);

        switch (results.getLocalAction()) {
            case NO_CHANGE:
                return iqFactory.createUnaryIQTree(this, updatedChild);
            case NEW_NODE:
                return iqFactory.createUnaryIQTree(results.getOptionalNewNode().get(), updatedChild);
            case REPLACE_BY_CHILD:
                return updatedChild;
            case DECLARE_AS_TRUE:
                return iqFactory.createTrueNode();
            case DECLARE_AS_EMPTY:
                return iqFactory.createEmptyNode(
                        projectedVariables.stream()
                                .flatMap(v -> descendingSubstitution.apply(v).getVariableStream())
                                .collect(ImmutableCollectors.toSet()));
            default:
                throw new MinorOntopInternalBugException("Unexpected local action: " + results.getLocalAction());
        }
    }

    private <T extends VariableOrGroundTerm>
    VariableOrGroundTermSubstitution<T> convertVariableOrGroundTermSubstitution(
            ImmutableSubstitution<T> substitution) {
        if (substitution instanceof VariableOrGroundTermSubstitution)
            return (VariableOrGroundTermSubstitution<T>) substitution;

        return substitutionFactory.getVariableOrGroundTermSubstitution(substitution.getImmutableMap());
    }

    private IQTree liftBinding(ConstructionNode childConstructionNode, UnaryIQTree childIQ) {
        IQTree grandChildIQTree = childIQ.getChild();

        ImmutableSubstitution<ImmutableTerm> newSubstitution = mergeWithAscendingSubstitution(
                childConstructionNode.getSubstitution());

        /*
         * TODO: what about updating the query modifiers? Clarify
         */
        Optional<ImmutableQueryModifiers> formerModifiers = getOptionalModifiers();
        if (formerModifiers.isPresent()) {
            Optional<ImmutableQueryModifiers> topModifiers = formerModifiers
                    .flatMap(m1 -> childConstructionNode.getOptionalModifiers()
                            .flatMap(m2 -> ImmutableQueryModifiersImpl.merge(m1, m2)));
            if (topModifiers.isPresent()) {
                ConstructionNode newConstructionNode = new ConstructionNodeImpl(projectedVariables,
                        newSubstitution, topModifiers, nullabilityEvaluator, unificationTools, constructionNodeTools,
                        substitutionTools, substitutionFactory, termFactory, iqFactory);

                return iqFactory.createUnaryIQTree(newConstructionNode, grandChildIQTree, true);
            }
            /*
             * Not mergeable query modifiers --> keeps two nodes
             */
            else {
                ConstructionNode newTopConstructionNode = new ConstructionNodeImpl(projectedVariables,
                        newSubstitution, formerModifiers, nullabilityEvaluator, unificationTools, constructionNodeTools,
                        substitutionTools, substitutionFactory, termFactory, iqFactory);

                ConstructionNode newChildConstructionNode = new ConstructionNodeImpl(grandChildIQTree.getVariables(),
                        substitutionFactory.getSubstitution(), childConstructionNode.getOptionalModifiers(),
                        nullabilityEvaluator, unificationTools, constructionNodeTools,
                        substitutionTools, substitutionFactory, termFactory, iqFactory);

                UnaryIQTree newChildIQ = iqFactory.createUnaryIQTree(newChildConstructionNode, grandChildIQTree, true);
                return iqFactory.createUnaryIQTree(newTopConstructionNode, newChildIQ, true);
            }
        }
        /*
         * No top query modifier
         */
        else {
            ConstructionNode newConstructionNode = new ConstructionNodeImpl(projectedVariables,
                    newSubstitution, childConstructionNode.getOptionalModifiers(), nullabilityEvaluator,
                    unificationTools, constructionNodeTools,
                    substitutionTools, substitutionFactory, termFactory, iqFactory);

            return iqFactory.createUnaryIQTree(newConstructionNode, grandChildIQTree, true);
        }
    }

    /**
     * TODO: explain
     */
    private static Optional<ImmutableQueryModifiers> updateOptionalModifiers(
            Optional<ImmutableQueryModifiers> optionalModifiers,
            ImmutableSubstitution<? extends ImmutableTerm> substitution1,
            ImmutableSubstitution<? extends ImmutableTerm> substitution2) {
        if (!optionalModifiers.isPresent()) {
            return Optional.empty();
        }
        ImmutableQueryModifiers previousModifiers = optionalModifiers.get();
        ImmutableList.Builder<OrderCondition> conditionBuilder = ImmutableList.builder();

        for (OrderCondition condition : previousModifiers.getSortConditions()) {
            ImmutableTerm newTerm = substitution2.apply(substitution1.apply(condition.getVariable()));
            /**
             * If after applying the substitution the term is still a variable,
             * "updates" the OrderCondition.
             *
             * Otherwise, forgets it.
             */
            if (newTerm instanceof Variable) {
                conditionBuilder.add(condition.newVariable((Variable) newTerm));
            }
        }
        return previousModifiers.newSortConditions(conditionBuilder.build());
    }

}
