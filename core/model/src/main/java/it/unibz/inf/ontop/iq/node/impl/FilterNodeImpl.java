package it.unibz.inf.ontop.iq.node.impl;


import com.google.common.collect.ImmutableList;
import com.google.common.collect.ImmutableSet;
import com.google.inject.assistedinject.Assisted;
import com.google.inject.assistedinject.AssistedInject;
import it.unibz.inf.ontop.datalog.impl.DatalogTools;
import it.unibz.inf.ontop.evaluator.ExpressionEvaluator;
import it.unibz.inf.ontop.evaluator.TermNullabilityEvaluator;
import it.unibz.inf.ontop.exception.MinorOntopInternalBugException;
import it.unibz.inf.ontop.injection.IntermediateQueryFactory;
import it.unibz.inf.ontop.iq.exception.QueryNodeTransformationException;
import it.unibz.inf.ontop.iq.node.*;
import it.unibz.inf.ontop.iq.node.impl.ConditionSimplifier.ExpressionAndSubstitution;
import it.unibz.inf.ontop.iq.transform.IQTransformer;
import it.unibz.inf.ontop.model.term.*;
import it.unibz.inf.ontop.model.term.impl.ImmutabilityTools;
import it.unibz.inf.ontop.model.type.TypeFactory;
import it.unibz.inf.ontop.substitution.ImmutableSubstitution;
import it.unibz.inf.ontop.iq.*;
import it.unibz.inf.ontop.iq.transform.node.HeterogeneousQueryNodeTransformer;
import it.unibz.inf.ontop.iq.transform.node.HomogeneousQueryNodeTransformer;
import it.unibz.inf.ontop.iq.exception.InvalidIntermediateQueryException;
import it.unibz.inf.ontop.substitution.SubstitutionFactory;
import it.unibz.inf.ontop.substitution.impl.ImmutableSubstitutionTools;
import it.unibz.inf.ontop.substitution.impl.ImmutableUnificationTools;
import it.unibz.inf.ontop.utils.ImmutableCollectors;
import it.unibz.inf.ontop.utils.VariableGenerator;

import java.util.Optional;

import static it.unibz.inf.ontop.model.term.functionsymbol.ExpressionOperation.AND;


public class FilterNodeImpl extends JoinOrFilterNodeImpl implements FilterNode {

    private static final String FILTER_NODE_STR = "FILTER";
    private static final int MAX_NORMALIZATION_ITERATIONS = 10000;
    private final ConstructionNodeTools constructionNodeTools;
    private final ConditionSimplifier conditionSimplifier;

    @AssistedInject
    private FilterNodeImpl(@Assisted ImmutableExpression filterCondition, TermNullabilityEvaluator nullabilityEvaluator,
                           TermFactory termFactory, TypeFactory typeFactory, DatalogTools datalogTools,
                           ImmutabilityTools immutabilityTools, SubstitutionFactory substitutionFactory,
                           ImmutableUnificationTools unificationTools, ImmutableSubstitutionTools substitutionTools,
                           ExpressionEvaluator defaultExpressionEvaluator, IntermediateQueryFactory iqFactory,
                           ConstructionNodeTools constructionNodeTools, ConditionSimplifier conditionSimplifier) {
        super(Optional.of(filterCondition), nullabilityEvaluator, termFactory, iqFactory, typeFactory, datalogTools,
                immutabilityTools, substitutionFactory, unificationTools, substitutionTools, defaultExpressionEvaluator);
        this.constructionNodeTools = constructionNodeTools;
        this.conditionSimplifier = conditionSimplifier;
    }

    @Override
    public void acceptVisitor(QueryNodeVisitor visitor) {
        visitor.visit(this);
    }

    @Override
    public FilterNode clone() {
        return iqFactory.createFilterNode(getFilterCondition());
    }

    @Override
    public FilterNode acceptNodeTransformer(HomogeneousQueryNodeTransformer transformer) throws QueryNodeTransformationException {
        return transformer.transform(this);
    }

    @Override
    public NodeTransformationProposal acceptNodeTransformer(HeterogeneousQueryNodeTransformer transformer) {
        return transformer.transform(this);
    }

    @Override
    public ImmutableExpression getFilterCondition() {
        return getOptionalFilterCondition().get();
    }

    @Override
    public FilterNode changeFilterCondition(ImmutableExpression newFilterCondition) {
        return iqFactory.createFilterNode(newFilterCondition);
    }

    @Override
    public boolean isVariableNullable(IntermediateQuery query, Variable variable) {
        if (isFilteringNullValue(variable))
            return false;

        return query.getFirstChild(this)
                .map(c -> c.isVariableNullable(query, variable))
                .orElseThrow(() -> new InvalidIntermediateQueryException("A filter node must have a child"));
    }

    @Override
    public ImmutableSet<Variable> getNullableVariables(IQTree child) {
        return child.getNullableVariables().stream()
                .filter(v -> !isFilteringNullValue(v))
                .collect(ImmutableCollectors.toSet());
    }


    @Override
    public IQTree liftIncompatibleDefinitions(Variable variable, IQTree child) {
        IQTree newChild = child.liftIncompatibleDefinitions(variable);
        QueryNode newChildRoot = newChild.getRootNode();

        /*
         * Lift the union above the filter
         */
        if ((newChildRoot instanceof UnionNode)
                && ((UnionNode) newChildRoot).hasAChildWithLiftableDefinition(variable, newChild.getChildren())) {
            UnionNode unionNode = (UnionNode) newChildRoot;
            ImmutableList<IQTree> grandChildren = newChild.getChildren();

            ImmutableList<IQTree> newChildren = grandChildren.stream()
                    .map(c -> (IQTree) iqFactory.createUnaryIQTree(this, c))
                    .collect(ImmutableCollectors.toList());

            return iqFactory.createNaryIQTree(unionNode, newChildren);
        }
        return iqFactory.createUnaryIQTree(this, newChild);
    }

    @Override
    public IQTree propagateDownConstraint(ImmutableExpression constraint, IQTree child) {
        return propagateDownCondition(child, Optional.of(constraint));
    }

    private IQTree propagateDownCondition(IQTree child, Optional<ImmutableExpression> initialConstraint) {
        try {
            // TODO: also consider the constraint for simplifying the condition
            ExpressionAndSubstitution conditionSimplificationResults = conditionSimplifier
                    .simplifyCondition(getFilterCondition());

            Optional<ImmutableExpression> downConstraint = computeDownConstraint(initialConstraint,
                    conditionSimplificationResults);

            IQTree newChild = Optional.of(conditionSimplificationResults.substitution)
                    .filter(s -> !s.isEmpty())
                    .map(s -> child.applyDescendingSubstitution(s, downConstraint))
                    .orElseGet(() -> downConstraint
                            .map(child::propagateDownConstraint)
                            .orElse(child));

            IQTree filterLevelTree = conditionSimplificationResults.optionalExpression
                    .map(e -> e.equals(getFilterCondition()) ? this : iqFactory.createFilterNode(e))
                    .map(filterNode -> (IQTree) iqFactory.createUnaryIQTree(filterNode, newChild))
                    .orElse(newChild);

            return Optional.of(conditionSimplificationResults.substitution)
                    .filter(s -> !s.isEmpty())
                    .map(s -> (ImmutableSubstitution<ImmutableTerm>)(ImmutableSubstitution<?>)s)
                    .map(s -> iqFactory.createConstructionNode(child.getVariables(), s))
                    .map(c -> (IQTree) iqFactory.createUnaryIQTree(c, filterLevelTree))
                    .orElse(filterLevelTree);


        } catch (UnsatisfiableConditionException e) {
            return iqFactory.createEmptyNode(child.getVariables());
        }

    }

    @Override
    public IQTree acceptTransformer(IQTree tree, IQTransformer transformer, IQTree child) {
        return transformer.transformFilter(tree,this, child);
    }

    @Override
    public void validateNode(IQTree child) throws InvalidIntermediateQueryException {
        checkExpression(getFilterCondition(), ImmutableList.of(child));
    }

    @Override
    public boolean isConstructed(Variable variable, IQTree child) {
        return child.isConstructed(variable);
    }

    @Override
    public boolean isSyntacticallyEquivalentTo(QueryNode node) {
        return (node instanceof FilterNode)
                && ((FilterNode) node).getFilterCondition().equals(this.getFilterCondition());
    }

    @Override
    public ImmutableSet<Variable> getRequiredVariables(IntermediateQuery query) {
        return getLocallyRequiredVariables();
    }

    @Override
    public boolean isEquivalentTo(QueryNode queryNode) {
        return (queryNode instanceof FilterNode)
                && getFilterCondition().equals(((FilterNode) queryNode).getFilterCondition());
    }

    @Override
    public String toString() {
        return FILTER_NODE_STR + getOptionalFilterString();
    }

    /**
     * TODO: Optimization: lift direct construction and filter nodes before normalizing them
     *  (so as to reduce the recursive pressure)
     */
    @Override
    public IQTree normalizeForOptimization(IQTree initialChild, VariableGenerator variableGenerator, IQProperties currentIQProperties) {
        //Non-final
        FilterNormalizationState state = new FilterNormalizationState(this, initialChild)
                .normalizeChild(variableGenerator);

        for(int i=0; i < MAX_NORMALIZATION_ITERATIONS; i++) {
            FilterNormalizationState stateBeforeSimplification = state.liftBindingsAndDistinct()
                    .mergeWithChild();

            FilterNormalizationState newState = stateBeforeSimplification.simplifyAndPropagateDownConstraint()
                    .normalizeChild(variableGenerator);

            // Convergence
            if (newState.child.equals(state.child))
                return newState.createNormalizedTree(variableGenerator, currentIQProperties);

            state = newState;
        }

        throw new MinorOntopInternalBugException("Bug: FilterNode.normalizeForOptimization() did not converge after "
                + MAX_NORMALIZATION_ITERATIONS + " iterations");
    }

    @Override
    public IQTree applyDescendingSubstitution(
            ImmutableSubstitution<? extends VariableOrGroundTerm> descendingSubstitution,
            Optional<ImmutableExpression> constraint, IQTree child) {

        ImmutableExpression unoptimizedExpression = descendingSubstitution.applyToBooleanExpression(getFilterCondition());

        ImmutableSet<Variable> newlyProjectedVariables = constructionNodeTools
                .computeNewProjectedVariables(descendingSubstitution, child.getVariables());

        try {
            ExpressionAndSubstitution expressionAndSubstitution = conditionSimplifier.simplifyCondition(unoptimizedExpression);

            Optional<ImmutableExpression> downConstraint = computeDownConstraint(constraint,
                    expressionAndSubstitution);

            ImmutableSubstitution<? extends VariableOrGroundTerm> downSubstitution =
                    ((ImmutableSubstitution<VariableOrGroundTerm>)descendingSubstitution)
                            .composeWith2(expressionAndSubstitution.substitution);

            IQTree newChild = child.applyDescendingSubstitution(downSubstitution, downConstraint);
            IQTree filterLevelTree = expressionAndSubstitution.optionalExpression
                    .map(iqFactory::createFilterNode)
                    .map(n -> (IQTree) iqFactory.createUnaryIQTree(n, newChild))
                    .orElse(newChild);
            return expressionAndSubstitution.substitution.isEmpty()
                    ? filterLevelTree
                    : iqFactory.createUnaryIQTree(
                            iqFactory.createConstructionNode(newlyProjectedVariables,
                                    (ImmutableSubstitution<ImmutableTerm>)(ImmutableSubstitution<?>)
                                            expressionAndSubstitution.substitution),
                            filterLevelTree);
        } catch (UnsatisfiableConditionException e) {
            return iqFactory.createEmptyNode(newlyProjectedVariables);
        }

    }


    /**
     * Immutable
     *
     * Normalization operations are directly done on this structure.
     *
     */
    @SuppressWarnings("OptionalUsedAsFieldOrParameterType")
    private class FilterNormalizationState {
        private final ImmutableSet<Variable> projectedVariables;
        // Parent first (should be composed of construction and distinct nodes only)
        private final ImmutableList<UnaryOperatorNode> ancestors;
        private final Optional<ImmutableExpression> condition;
        private final IQTree child;

        /**
         * Initial constructor
         */
        public FilterNormalizationState(FilterNode initialFilterNode, IQTree initialChild) {
            projectedVariables = initialChild.getVariables();
            ancestors = ImmutableList.of();
            condition = Optional.of(initialFilterNode.getFilterCondition());
            child = initialChild;
        }

        private FilterNormalizationState(ImmutableSet<Variable> projectedVariables, ImmutableList<UnaryOperatorNode> ancestors,
                                         Optional<ImmutableExpression> condition, IQTree child) {
            this.projectedVariables = projectedVariables;
            this.ancestors = ancestors;
            this.condition = condition;
            this.child = child;
        }

        private FilterNormalizationState updateChild(IQTree newChild) {
            return new FilterNormalizationState(projectedVariables, ancestors, condition, newChild);
        }

        private FilterNormalizationState updateParentChildAndCondition(UnaryOperatorNode newParent,
                                                                       ImmutableExpression newCondition, IQTree newChild) {
            ImmutableList<UnaryOperatorNode> newAncestors = ImmutableList.<UnaryOperatorNode>builder()
                    .add(newParent)
                    .addAll(ancestors)
                    .build();

            return new FilterNormalizationState(projectedVariables, newAncestors, Optional.of(newCondition), newChild);
        }

        private FilterNormalizationState addParentRemoveConditionAndUpdateChild(UnaryOperatorNode newParent, IQTree newChild) {
            ImmutableList<UnaryOperatorNode> newAncestors = ImmutableList.<UnaryOperatorNode>builder()
                    .add(newParent)
                    .addAll(ancestors)
                    .build();

            return new FilterNormalizationState(projectedVariables, newAncestors, Optional.empty(), newChild);
        }

        private FilterNormalizationState liftChildAsParent(UnaryIQTree formerChildTree) {
            ImmutableList<UnaryOperatorNode> newAncestors = ImmutableList.<UnaryOperatorNode>builder()
                    .add(formerChildTree.getRootNode())
                    .addAll(ancestors)
                    .build();

            IQTree newChild = formerChildTree.getChild();
            return new FilterNormalizationState(projectedVariables, newAncestors, condition, newChild);
        }

        private FilterNormalizationState updateConditionAndChild(ImmutableExpression newCondition, IQTree newChild) {
            return new FilterNormalizationState(projectedVariables, ancestors, Optional.of(newCondition), newChild);
        }

        private FilterNormalizationState removeConditionAndUpdateChild(IQTree newChild) {
            return new FilterNormalizationState(projectedVariables, ancestors, Optional.empty(), newChild);
        }

        private FilterNormalizationState createEmptyState() {
            return new FilterNormalizationState(projectedVariables, ImmutableList.of(), Optional.empty(),
                    iqFactory.createEmptyNode(projectedVariables));
        }


        public FilterNormalizationState normalizeChild(VariableGenerator variableGenerator) {
            return updateChild(child.normalizeForOptimization(variableGenerator));
        }

        /**
         * Returns a tree in which the "filter-level" sub-tree is declared as normalized.
         */
        public IQTree createNormalizedTree(VariableGenerator variableGenerator, IQProperties currentIQProperties) {

            if (child.isDeclaredAsEmpty())
                return iqFactory.createEmptyNode(projectedVariables);

            IQTree filterLevelTree = condition
                    .map(c -> (IQTree) iqFactory.createUnaryIQTree(iqFactory.createFilterNode(c), child,
                            currentIQProperties.declareNormalizedForOptimization()))
                    .orElse(child);

            if (ancestors.isEmpty())
                return filterLevelTree;

            return ancestors.stream()
                    .reduce(filterLevelTree, (t, n) -> iqFactory.createUnaryIQTree(n, t),
                            // Should not be called
                            (t1, t2) -> { throw new MinorOntopInternalBugException("The order must be respected"); })
                    // Normalizes the ancestors (recursive)
                    .normalizeForOptimization(variableGenerator);
        }

        public FilterNormalizationState liftBindingsAndDistinct() {
            QueryNode childRoot = child.getRootNode();

            if (childRoot instanceof ConstructionNode)
                return liftBindings((ConstructionNode) childRoot, (UnaryIQTree) child)
                        // Recursive (maybe followed by a distinct)
                    .liftBindingsAndDistinct();

            else if (childRoot instanceof DistinctNode)
                return liftDistinct((DistinctNode) childRoot, (UnaryIQTree) child)
                        // Recursive (may be followed by another construction node)
                        .liftBindingsAndDistinct();
            else
                return this;
        }


        private FilterNormalizationState liftBindings(ConstructionNode childConstructionNode, UnaryIQTree child) {
            return condition
                    .map(e -> childConstructionNode.getSubstitution().applyToBooleanExpression(e))
                    .map(e -> updateParentChildAndCondition(childConstructionNode, e, child.getChild()))
                    .orElseGet(() -> liftChildAsParent(child));
        }

        private FilterNormalizationState liftDistinct(DistinctNode childDistinct, UnaryIQTree child) {
            return condition
                    .map(e -> updateParentChildAndCondition(childDistinct, e, child.getChild()))
                    .orElseGet(() -> liftChildAsParent(child));
        }

        /**
         * Tries to merge with the child
         */
        public FilterNormalizationState mergeWithChild() {
            if (condition.isPresent()) {

                QueryNode childRoot = child.getRootNode();

                if (childRoot instanceof FilterNode) {
                    FilterNode filterChild = (FilterNode) childRoot;

                    ImmutableExpression newCondition = termFactory.getImmutableExpression(AND, condition.get(),
                            filterChild.getFilterCondition());

                    return updateConditionAndChild(newCondition, ((UnaryIQTree)child).getChild());
                }

                else if (childRoot instanceof InnerJoinNode) {
                    ImmutableExpression newJoiningCondition = ((InnerJoinNode) childRoot).getOptionalFilterCondition()
                            .map(c -> termFactory.getImmutableExpression(AND, condition.get(), c))
                            .orElse(condition.get());

                    InnerJoinNode newJoinNode = iqFactory.createInnerJoinNode(newJoiningCondition);

                    IQTree newChild = iqFactory.createNaryIQTree(newJoinNode, child.getChildren());
                    return removeConditionAndUpdateChild(newChild);
                }
            }
            return this;
        }


        public FilterNormalizationState simplifyAndPropagateDownConstraint() {
            if (!condition.isPresent()) {
                return this;
            }

            try {
                // TODO: also consider the constraint for simplifying the condition
                ExpressionAndSubstitution conditionSimplificationResults = conditionSimplifier.simplifyCondition(
                        condition.get());

                Optional<ImmutableExpression> downConstraint = computeDownConstraint(Optional.empty(),
                        conditionSimplificationResults);

                IQTree newChild = Optional.of(conditionSimplificationResults.substitution)
                        .filter(s -> !s.isEmpty())
                        .map(s -> child.applyDescendingSubstitution(s, downConstraint))
                        .orElseGet(() -> downConstraint
                                .map(child::propagateDownConstraint)
                                .orElse(child));

                Optional<ConstructionNode> parentConstructionNode = Optional.of(conditionSimplificationResults.substitution)
                        .filter(s -> !s.isEmpty())
                        .map(s -> (ImmutableSubstitution<ImmutableTerm>) (ImmutableSubstitution<?>) s)
                        .map(s -> iqFactory.createConstructionNode(child.getVariables(), s));

                return conditionSimplificationResults.optionalExpression
                        .map(e -> parentConstructionNode
                                .map(p -> updateParentChildAndCondition(p, e, newChild))
                                .orElseGet(() -> updateConditionAndChild(e, newChild)))
                        .orElseGet(() -> parentConstructionNode
                                        .map(p -> addParentRemoveConditionAndUpdateChild(p, newChild))
                                        .orElseGet(() -> removeConditionAndUpdateChild(newChild)));
            } catch (UnsatisfiableConditionException e) {
                return createEmptyState();
            }
        }
    }
}
