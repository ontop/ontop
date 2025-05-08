package it.unibz.inf.ontop.iq.node.normalization.impl;

import com.google.common.collect.ImmutableList;
import com.google.common.collect.ImmutableSet;
import com.google.inject.Inject;
import com.google.inject.Singleton;
import it.unibz.inf.ontop.exception.MinorOntopInternalBugException;
import it.unibz.inf.ontop.injection.IntermediateQueryFactory;
import it.unibz.inf.ontop.iq.IQTree;
import it.unibz.inf.ontop.iq.IQTreeCache;
import it.unibz.inf.ontop.iq.impl.IQTreeTools;
import it.unibz.inf.ontop.iq.node.*;
import it.unibz.inf.ontop.iq.node.normalization.ConditionSimplifier;
import it.unibz.inf.ontop.iq.node.impl.UnsatisfiableConditionException;
import it.unibz.inf.ontop.iq.node.normalization.ConditionSimplifier.ExpressionAndSubstitution;
import it.unibz.inf.ontop.iq.node.normalization.FilterNormalizer;
import it.unibz.inf.ontop.model.term.ImmutableExpression;
import it.unibz.inf.ontop.model.term.TermFactory;
import it.unibz.inf.ontop.model.term.Variable;
import it.unibz.inf.ontop.utils.ImmutableCollectors;
import it.unibz.inf.ontop.utils.VariableGenerator;

import java.util.Optional;
import java.util.stream.Stream;

import static it.unibz.inf.ontop.iq.impl.IQTreeTools.UnaryIQTreeDecomposition;
import static it.unibz.inf.ontop.iq.impl.IQTreeTools.NaryIQTreeDecomposition;
import static it.unibz.inf.ontop.iq.impl.IQTreeTools.UnaryOperatorSequence;


@Singleton
public class FilterNormalizerImpl implements FilterNormalizer {

    private static final int MAX_NORMALIZATION_ITERATIONS = 10000;
    private final IntermediateQueryFactory iqFactory;
    private final TermFactory termFactory;
    private final ConditionSimplifier conditionSimplifier;
    private final IQTreeTools iqTreeTools;

    @Inject
    private FilterNormalizerImpl(IntermediateQueryFactory iqFactory, TermFactory termFactory,
                                 ConditionSimplifier conditionSimplifier, IQTreeTools iqTreeTools) {
        this.iqFactory = iqFactory;
        this.termFactory = termFactory;
        this.conditionSimplifier = conditionSimplifier;
        this.iqTreeTools = iqTreeTools;
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

            State newState = stateBeforeSimplification.simplifyAndPropagateDownConstraint(variableGenerator)
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
        private final UnaryOperatorSequence<UnaryOperatorNode> ancestors;
        private final Optional<ImmutableExpression> condition;
        private final IQTree child;
        /**
         * Initial constructor
         */
        protected State(FilterNode initialFilterNode, IQTree initialChild) {
            this(initialChild.getVariables(),
                    UnaryOperatorSequence.of(),
                    Optional.of(initialFilterNode.getFilterCondition()),
                    initialChild);
        }

        protected State(ImmutableSet<Variable> projectedVariables, UnaryOperatorSequence<UnaryOperatorNode> ancestors,
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
                                                                       Optional<ImmutableExpression> newCondition, IQTree newChild) {
            return new State(projectedVariables, ancestors.append(newParent), newCondition, newChild);
        }

        private State liftChildAsParent(UnaryIQTreeDecomposition<?> decomposition) {
            return new State(projectedVariables, ancestors.append(decomposition.getNode()), condition, decomposition.getChild());
        }

        private State updateConditionAndChild(Optional<ImmutableExpression> newCondition, IQTree newChild) {
            return new State(projectedVariables, ancestors, newCondition, newChild);
        }

        private State createEmptyState() {
            return new State(projectedVariables, UnaryOperatorSequence.of(), Optional.empty(),
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
                    .map(iqFactory::createFilterNode)
                    .<IQTree>map(n -> iqFactory.createUnaryIQTree(n, child, treeCache.declareAsNormalizedForOptimizationWithEffect()))
                    .orElse(child);

            if (ancestors.isEmpty())
                return filterLevelTree;

            return iqTreeTools.createAncestorsUnaryIQTree(ancestors, filterLevelTree)
                    // Normalizes the ancestors (recursive)
                    .normalizeForOptimization(variableGenerator);
        }

        public State liftBindingsAndDistinct() {

            var construction = UnaryIQTreeDecomposition.of(child, ConstructionNode.class);
            if (construction.isPresent()) {
                return condition
                        .map(e -> construction.getNode().getSubstitution().apply(e))
                        .map(e -> updateParentChildAndCondition(construction.getNode(), Optional.of(e), construction.getChild()))
                        .orElseGet(() -> liftChildAsParent(construction))
                        // Recursive (maybe followed by a distinct)
                        .liftBindingsAndDistinct();
            }

            var distinct = UnaryIQTreeDecomposition.of(child, DistinctNode.class);
            if (distinct.isPresent()) {
                return condition
                        .map(e -> updateParentChildAndCondition(distinct.getNode(), Optional.of(e), distinct.getChild()))
                        .orElseGet(() -> liftChildAsParent(distinct))
                        // Recursive (may be followed by another construction node)
                        .liftBindingsAndDistinct();
            }

            return this;
        }


        /**
         * Tries to merge with the child
         */
        public State mergeWithChild() {
            if (condition.isPresent()) {
                var filter = UnaryIQTreeDecomposition.of(child, FilterNode.class);
                if (filter.isPresent()) {
                    ImmutableExpression newCondition = termFactory.getConjunction(condition.get(),
                            filter.getNode().getFilterCondition());

                    return updateConditionAndChild(Optional.of(newCondition), filter.getChild());
                }
                var join = NaryIQTreeDecomposition.of(child, InnerJoinNode.class);
                if (join.isPresent()) {
                    ImmutableExpression newJoiningCondition = join.getNode().getOptionalFilterCondition()
                            .map(c -> termFactory.getConjunction(condition.get(), c))
                            .orElse(condition.get());

                    IQTree newChild = iqFactory.createNaryIQTree(
                            iqFactory.createInnerJoinNode(newJoiningCondition),
                            join.getChildren());
                    return updateConditionAndChild(Optional.empty(), newChild);
                }
            }
            return this;
        }

        public State simplifyAndPropagateDownConstraint(VariableGenerator variableGenerator) {
            if (condition.isEmpty()) {
                return this;
            }

            try {
                VariableNullability childVariableNullability = child.getVariableNullability();

                // TODO: also consider the constraint for simplifying the condition
                ExpressionAndSubstitution conditionSimplificationResults = conditionSimplifier.simplifyCondition(
                        condition.get(), ImmutableList.of(child), childVariableNullability);

                Optional<ImmutableExpression> downConstraint = conditionSimplifier.computeDownConstraint(Optional.empty(),
                        conditionSimplificationResults, childVariableNullability);

                IQTree newChild = Optional.of(conditionSimplificationResults.getSubstitution())
                        .filter(s -> !s.isEmpty())
                        .map(s -> child.applyDescendingSubstitution(s, downConstraint, variableGenerator))
                        .orElseGet(() -> downConstraint
                                .map(c -> child.propagateDownConstraint(c, variableGenerator))
                                .orElse(child));

                Optional<ConstructionNode> parentConstructionNode = Optional.of(conditionSimplificationResults.getSubstitution())
                        .filter(s -> !s.isEmpty())
                        .map(s -> iqFactory.createConstructionNode(child.getVariables(), s));

                return parentConstructionNode
                        .map(p -> updateParentChildAndCondition(p, conditionSimplificationResults.getOptionalExpression(), newChild))
                        .orElseGet(() -> updateConditionAndChild(conditionSimplificationResults.getOptionalExpression(), newChild));
            }
            catch (UnsatisfiableConditionException e) {
                return createEmptyState();
            }
        }
    }
}
