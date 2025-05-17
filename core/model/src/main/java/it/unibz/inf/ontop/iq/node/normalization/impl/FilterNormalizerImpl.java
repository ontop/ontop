package it.unibz.inf.ontop.iq.node.normalization.impl;

import com.google.common.collect.ImmutableList;
import com.google.common.collect.ImmutableSet;
import com.google.inject.Inject;
import com.google.inject.Singleton;
import it.unibz.inf.ontop.injection.IntermediateQueryFactory;
import it.unibz.inf.ontop.iq.IQTree;
import it.unibz.inf.ontop.iq.IQTreeCache;
import it.unibz.inf.ontop.iq.NaryIQTree;
import it.unibz.inf.ontop.iq.UnaryIQTree;
import it.unibz.inf.ontop.iq.impl.DownPropagation;
import it.unibz.inf.ontop.iq.impl.IQTreeTools;
import it.unibz.inf.ontop.iq.node.*;
import it.unibz.inf.ontop.iq.node.normalization.ConditionSimplifier;
import it.unibz.inf.ontop.iq.node.impl.UnsatisfiableConditionException;
import it.unibz.inf.ontop.iq.node.normalization.FilterNormalizer;
import it.unibz.inf.ontop.iq.visit.impl.IQStateOptionalTransformer;
import it.unibz.inf.ontop.model.term.Variable;
import it.unibz.inf.ontop.utils.VariableGenerator;

import java.util.Optional;

import static it.unibz.inf.ontop.iq.impl.IQTreeTools.UnaryOperatorSequence;
import static it.unibz.inf.ontop.iq.visit.impl.IQStateOptionalTransformer.*;


@Singleton
public class FilterNormalizerImpl implements FilterNormalizer {

    private final IntermediateQueryFactory iqFactory;
    private final ConditionSimplifier conditionSimplifier;
    private final IQTreeTools iqTreeTools;

    private static final int MAX_NORMALIZATION_ITERATIONS = 10000;

    @Inject
    private FilterNormalizerImpl(IntermediateQueryFactory iqFactory,
                                 ConditionSimplifier conditionSimplifier, IQTreeTools iqTreeTools) {
        this.iqFactory = iqFactory;
        this.conditionSimplifier = conditionSimplifier;
        this.iqTreeTools = iqTreeTools;
    }

    /**
     * TODO: Optimization: lift direct construction and filter nodes before normalizing them
     *  (so as to reduce the recursive pressure)
     */
    @Override
    public IQTree normalizeForOptimization(FilterNode initialFilterNode, IQTree initialChild, VariableGenerator variableGenerator, IQTreeCache treeCache) {
        Context context = new Context(initialFilterNode, initialChild, variableGenerator, treeCache);
        return context.normalize();
    }

    private class Context {

        private final FilterNode initialFilterNode;
        private final IQTree initialChild;
        private final VariableGenerator variableGenerator;
        private final IQTreeCache treeCache;

        private final ImmutableSet<Variable> projectedVariables;

        Context(FilterNode initialFilterNode, IQTree initialChild, VariableGenerator variableGenerator, IQTreeCache treeCache) {
            this.initialFilterNode = initialFilterNode;
            this.initialChild = initialChild;
            this.variableGenerator = variableGenerator;
            this.treeCache = treeCache;
            this.projectedVariables = initialChild.getVariables();
        }

        IQTree normalize() {
            State state = reachFixedPoint(
                    new State(UnaryOperatorSequence.of(), Optional.of(initialFilterNode), initialChild),
                    s -> s.normalizeAndLiftConstructionDistinctFilterInnerJoin()
                            .simplifyAndPropagateDownConstraint(),
                    MAX_NORMALIZATION_ITERATIONS);
            return state.toIQTree();
        }

        /**
         * A sequence of ConstructionNode and DistinctNode,
         * followed by an optional FilterNode, followed by a child tree.
         *
         * The initial state has a non-empty FilterNode, which can become empty
         * only after lifting a single InnerJoinNode
         * (which terminates the normalization process).
         */

        @SuppressWarnings("OptionalUsedAsFieldOrParameterType")
        private class State {
            private final UnaryOperatorSequence<UnaryOperatorNode> ancestors;
            private final Optional<FilterNode> optionalFilterNode;
            private final IQTree child;

            State(UnaryOperatorSequence<UnaryOperatorNode> ancestors,
                            Optional<FilterNode> optionalFilterNode,
                            IQTree child) {
                this.ancestors = ancestors;
                this.optionalFilterNode = optionalFilterNode;
                this.child = child;
            }

            Optional<State> lift(UnaryOperatorNode parent, Optional<FilterNode> optionalFilterNode, IQTree child) {
                return Optional.of(new State(ancestors.append(parent), optionalFilterNode, child));
            }

            Optional<State> lift(Optional<FilterNode> optionalFilterNode, IQTree child) {
                return Optional.of(new State(ancestors, optionalFilterNode, child));
            }

            State normalizeAndLiftConstructionDistinctFilterInnerJoin() {
                return IQStateOptionalTransformer.reachFinalState(
                        this, State::normalizeChild, State::liftConstructionDistinctFilterInnerJoin);
            }

            Optional<State> liftConstructionDistinctFilterInnerJoin() {
                return child.acceptVisitor(new IQStateOptionalTransformer<>() {

                    @Override
                    public Optional<State> transformConstruction(UnaryIQTree tree, ConstructionNode node, IQTree  newChild) {
                        var newOptionalFilterNode = optionalFilterNode
                                .map(FilterNode::getFilterCondition)
                                .map(e -> node.getSubstitution().apply(e))
                                .map(iqFactory::createFilterNode);
                        return lift(node, newOptionalFilterNode, newChild);
                    }

                    @Override
                    public Optional<State> transformDistinct(UnaryIQTree tree, DistinctNode node, IQTree newChild) {
                        return lift(node, optionalFilterNode, newChild);
                    }

                    @Override
                    public Optional<State> transformFilter(UnaryIQTree tree, FilterNode node, IQTree newChild) {
                        var newFilterNode = iqFactory.createFilterNode(
                                iqTreeTools.getConjunction(
                                        optionalFilterNode.map(FilterNode::getFilterCondition),
                                        node.getFilterCondition()));
                        return lift(Optional.of(newFilterNode), newChild);
                    }

                    @Override
                    public Optional<State> transformInnerJoin(NaryIQTree tree, InnerJoinNode node, ImmutableList <IQTree> children) {
                        if (optionalFilterNode.isPresent()) {
                            var newJoiningCondition = iqTreeTools.getConjunction(
                                    optionalFilterNode.get().getFilterCondition(),
                                    node.getOptionalFilterCondition());

                            IQTree newChild = iqTreeTools.createInnerJoinTree(Optional.of(newJoiningCondition), children);
                            // will be final on the next call as the filter will be empty then
                            return lift(Optional.empty(), newChild);
                        }
                        return done();
                    }
                });
            }

            @Override
            public boolean equals(Object o) {
                if (o instanceof State) {
                    State other = (State) o;
                    return ancestors.equals(other.ancestors)
                            && optionalFilterNode.equals(other.optionalFilterNode)
                            && child.equals(other.child);
                }
                return false;
            }

            /**
             * Returns a tree in which the "filter-level" sub-tree is declared as normalized.
             */
            IQTree toIQTree() {

                if (child.isDeclaredAsEmpty())
                    return iqFactory.createEmptyNode(projectedVariables);

                IQTree filterLevelTree = optionalFilterNode
                        .<IQTree>map(n -> iqFactory.createUnaryIQTree(n, child, treeCache.declareAsNormalizedForOptimizationWithEffect()))
                        .orElse(child);

                if (ancestors.isEmpty())
                    return filterLevelTree;

                return iqTreeTools.createAncestorsUnaryIQTree(ancestors, filterLevelTree)
                        // Normalizes the ancestors (recursive)
                        .normalizeForOptimization(variableGenerator);
            }

            State normalizeChild() {
                return new State(ancestors, optionalFilterNode, child.normalizeForOptimization(variableGenerator));
            }

            State simplifyAndPropagateDownConstraint() {
                if (optionalFilterNode.isEmpty())
                    return this;

                try {
                    var childVariableNullability = child.getVariableNullability();

                    // TODO: also consider the constraint for simplifying the condition
                    var simplifiedFilterCondition = conditionSimplifier.simplifyCondition(
                            optionalFilterNode.get().getFilterCondition(), ImmutableList.of(child), childVariableNullability);

                    var extendedDownConstraint = conditionSimplifier.extendAndSimplifyDownConstraint(
                            new DownPropagation(projectedVariables), simplifiedFilterCondition, childVariableNullability);

                    var newChild = extendedDownConstraint.applyDescendingSubstitution(child, extendedDownConstraint.getSubstitution(), variableGenerator);

                    var newOptionalParent = iqTreeTools.createOptionalConstructionNode(child::getVariables, simplifiedFilterCondition.getSubstitution());

                    var newOptionalFilterNode = iqTreeTools.createOptionalFilterNode(simplifiedFilterCondition.getOptionalExpression());

                    return new State(ancestors.append(newOptionalParent), newOptionalFilterNode, newChild);
                }
                catch (UnsatisfiableConditionException e) {
                    return new State(UnaryOperatorSequence.of(), Optional.empty(), iqFactory.createEmptyNode(projectedVariables));
                }
            }
        }
    }
}
