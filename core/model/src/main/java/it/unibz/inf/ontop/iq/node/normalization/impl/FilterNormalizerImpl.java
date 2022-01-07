package it.unibz.inf.ontop.iq.node.normalization.impl;

import com.google.common.collect.ImmutableList;
import com.google.common.collect.ImmutableSet;
import com.google.inject.Inject;
import com.google.inject.Singleton;
import it.unibz.inf.ontop.exception.MinorOntopInternalBugException;
import it.unibz.inf.ontop.injection.IntermediateQueryFactory;
import it.unibz.inf.ontop.iq.IQTree;
import it.unibz.inf.ontop.iq.IQTreeCache;
import it.unibz.inf.ontop.iq.UnaryIQTree;
import it.unibz.inf.ontop.iq.node.*;
import it.unibz.inf.ontop.iq.node.normalization.ConditionSimplifier;
import it.unibz.inf.ontop.iq.node.impl.UnsatisfiableConditionException;
import it.unibz.inf.ontop.iq.node.normalization.ConditionSimplifier.ExpressionAndSubstitution;
import it.unibz.inf.ontop.iq.node.normalization.FilterNormalizer;
import it.unibz.inf.ontop.model.term.ImmutableExpression;
import it.unibz.inf.ontop.model.term.ImmutableTerm;
import it.unibz.inf.ontop.model.term.TermFactory;
import it.unibz.inf.ontop.model.term.Variable;
import it.unibz.inf.ontop.substitution.ImmutableSubstitution;
import it.unibz.inf.ontop.utils.VariableGenerator;

import java.util.Optional;

@Singleton
public class FilterNormalizerImpl implements FilterNormalizer {

    private static final int MAX_NORMALIZATION_ITERATIONS = 10000;
    private final IntermediateQueryFactory iqFactory;
    private final TermFactory termFactory;
    private final ConditionSimplifier conditionSimplifier;

    @Inject
    private FilterNormalizerImpl(IntermediateQueryFactory iqFactory, TermFactory termFactory,
                                 ConditionSimplifier conditionSimplifier) {
        this.iqFactory = iqFactory;
        this.termFactory = termFactory;
        this.conditionSimplifier = conditionSimplifier;
    }

    /**
     * TODO: Optimization: lift direct construction and filter nodes before normalizing them
     *  (so as to reduce the recursive pressure)
     */
    @Override
    public IQTree normalizeForOptimization(FilterNode initialFilterNode, IQTree initialChild, VariableGenerator variableGenerator, IQTreeCache treeCache) {
        //Non-final
        State state = new State(initialFilterNode, initialChild)
                .normalizeChild(variableGenerator);

        for(int i=0; i < MAX_NORMALIZATION_ITERATIONS; i++) {
            State stateBeforeSimplification = state.liftBindingsAndDistinct()
                    .mergeWithChild();

            State newState = stateBeforeSimplification.simplifyAndPropagateDownConstraint()
                    .normalizeChild(variableGenerator);

            // Convergence
            if (newState.child.equals(state.child))
                return newState.createNormalizedTree(variableGenerator, treeCache);

            state = newState;
        }

        throw new MinorOntopInternalBugException("Bug: FilterNode.normalizeForOptimization() did not converge after "
                + MAX_NORMALIZATION_ITERATIONS + " iterations");
    }

    /**
     * Immutable
     *
     * Normalization operations are directly done on this structure.
     *
     */
    @SuppressWarnings("OptionalUsedAsFieldOrParameterType")
    protected class State {
        private final ImmutableSet<Variable> projectedVariables;
        // Parent first (should be composed of construction and distinct nodes only)
        private final ImmutableList<UnaryOperatorNode> ancestors;
        private final Optional<ImmutableExpression> condition;
        private final IQTree child;
        /**
         * Initial constructor
         */
        protected State(FilterNode initialFilterNode, IQTree initialChild) {
            projectedVariables = initialChild.getVariables();
            ancestors = ImmutableList.of();
            condition = Optional.of(initialFilterNode.getFilterCondition());
            child = initialChild;
        }

        protected State(ImmutableSet<Variable> projectedVariables, ImmutableList<UnaryOperatorNode> ancestors,
                        Optional<ImmutableExpression> condition, IQTree child) {
            this.projectedVariables = projectedVariables;
            this.ancestors = ancestors;
            this.condition = condition;
            this.child = child;
        }

        private State updateChild(IQTree newChild) {
            return new State(projectedVariables, ancestors, condition, newChild);
        }

        private State updateParentChildAndCondition(UnaryOperatorNode newParent,
                                                                       ImmutableExpression newCondition, IQTree newChild) {
            ImmutableList<UnaryOperatorNode> newAncestors = ImmutableList.<UnaryOperatorNode>builder()
                    .add(newParent)
                    .addAll(ancestors)
                    .build();

            return new State(projectedVariables, newAncestors, Optional.of(newCondition), newChild);
        }

        private State addParentRemoveConditionAndUpdateChild(UnaryOperatorNode newParent, IQTree newChild) {
            ImmutableList<UnaryOperatorNode> newAncestors = ImmutableList.<UnaryOperatorNode>builder()
                    .add(newParent)
                    .addAll(ancestors)
                    .build();

            return new State(projectedVariables, newAncestors, Optional.empty(), newChild);
        }

        private State liftChildAsParent(UnaryIQTree formerChildTree) {
            ImmutableList<UnaryOperatorNode> newAncestors = ImmutableList.<UnaryOperatorNode>builder()
                    .add(formerChildTree.getRootNode())
                    .addAll(ancestors)
                    .build();

            IQTree newChild = formerChildTree.getChild();
            return new State(projectedVariables, newAncestors, condition, newChild);
        }

        private State updateConditionAndChild(ImmutableExpression newCondition, IQTree newChild) {
            return new State(projectedVariables, ancestors, Optional.of(newCondition), newChild);
        }

        private State removeConditionAndUpdateChild(IQTree newChild) {
            return new State(projectedVariables, ancestors, Optional.empty(), newChild);
        }

        private State createEmptyState() {
            return new State(projectedVariables, ImmutableList.of(), Optional.empty(),
                    iqFactory.createEmptyNode(projectedVariables));
        }

        public State normalizeChild(VariableGenerator variableGenerator) {
            return updateChild(child.normalizeForOptimization(variableGenerator));
        }

        /**
         * Returns a tree in which the "filter-level" sub-tree is declared as normalized.
         */
        public IQTree createNormalizedTree(VariableGenerator variableGenerator, IQTreeCache treeCache) {

            if (child.isDeclaredAsEmpty())
                return iqFactory.createEmptyNode(projectedVariables);

            IQTree filterLevelTree = condition
                    .map(c -> (IQTree) iqFactory.createUnaryIQTree(iqFactory.createFilterNode(c), child,
                            treeCache.declareAsNormalizedForOptimizationWithEffect()))
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

        public State liftBindingsAndDistinct() {
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


        private State liftBindings(ConstructionNode childConstructionNode, UnaryIQTree child) {
            return condition
                    .map(e -> childConstructionNode.getSubstitution().applyToBooleanExpression(e))
                    .map(e -> updateParentChildAndCondition(childConstructionNode, e, child.getChild()))
                    .orElseGet(() -> liftChildAsParent(child));
        }

        private State liftDistinct(DistinctNode childDistinct, UnaryIQTree child) {
            return condition
                    .map(e -> updateParentChildAndCondition(childDistinct, e, child.getChild()))
                    .orElseGet(() -> liftChildAsParent(child));
        }

        /**
         * Tries to merge with the child
         */
        public State mergeWithChild() {
            if (condition.isPresent()) {

                QueryNode childRoot = child.getRootNode();

                if (childRoot instanceof FilterNode) {
                    FilterNode filterChild = (FilterNode) childRoot;

                    ImmutableExpression newCondition = termFactory.getConjunction(condition.get(),
                            filterChild.getFilterCondition());

                    return updateConditionAndChild(newCondition, ((UnaryIQTree)child).getChild());
                }

                else if (childRoot instanceof InnerJoinNode) {
                    ImmutableExpression newJoiningCondition = ((InnerJoinNode) childRoot).getOptionalFilterCondition()
                            .map(c -> termFactory.getConjunction(condition.get(), c))
                            .orElse(condition.get());

                    InnerJoinNode newJoinNode = iqFactory.createInnerJoinNode(newJoiningCondition);

                    IQTree newChild = iqFactory.createNaryIQTree(newJoinNode, child.getChildren());
                    return removeConditionAndUpdateChild(newChild);
                }
            }
            return this;
        }

        public State simplifyAndPropagateDownConstraint() {
            if (!condition.isPresent()) {
                return this;
            }

            try {
                VariableNullability childVariableNullability = child.getVariableNullability();

                // TODO: also consider the constraint for simplifying the condition
                ExpressionAndSubstitution conditionSimplificationResults = conditionSimplifier.simplifyCondition(
                        condition.get(), childVariableNullability);

                Optional<ImmutableExpression> downConstraint = conditionSimplifier.computeDownConstraint(Optional.empty(),
                        conditionSimplificationResults, childVariableNullability);

                IQTree newChild = Optional.of(conditionSimplificationResults.getSubstitution())
                        .filter(s -> !s.isEmpty())
                        .map(s -> child.applyDescendingSubstitution(s, downConstraint))
                        .orElseGet(() -> downConstraint
                                .map(child::propagateDownConstraint)
                                .orElse(child));

                Optional<ConstructionNode> parentConstructionNode = Optional.of(conditionSimplificationResults.getSubstitution())
                        .filter(s -> !s.isEmpty())
                        .map(s -> (ImmutableSubstitution<ImmutableTerm>) (ImmutableSubstitution<?>) s)
                        .map(s -> iqFactory.createConstructionNode(child.getVariables(), s));

                return conditionSimplificationResults.getOptionalExpression()
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
