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

    @SuppressWarnings("OptionalUsedAsFieldOrParameterType")
    private static class FilterSubTree {
        private final Optional<FilterNode> optionalFilterNode;
        private final IQTree child;

        FilterSubTree(Optional<FilterNode> optionalFilterNode, IQTree child) {
            this.optionalFilterNode = optionalFilterNode;
            this.child = child;
        }

        IQTree getChild() {
            return child;
        }

        Optional<FilterNode> getOptionalNode() {
            return optionalFilterNode;
        }

        @Override
        public boolean equals(Object o) {
            if (o instanceof FilterSubTree) {
                FilterSubTree other = (FilterSubTree)o;
                return optionalFilterNode.equals(other.optionalFilterNode)
                    && child.equals(other.child);
            }
            return false;
        }
    }

    private class Context extends NormalizationContext {

        private final FilterNode initialFilterNode;
        private final IQTree initialChild;
        private final IQTreeCache treeCache;

        private final ImmutableSet<Variable> projectedVariables;

        Context(FilterNode initialFilterNode, IQTree initialChild, VariableGenerator variableGenerator, IQTreeCache treeCache) {
            super(variableGenerator);
            this.initialFilterNode = initialFilterNode;
            this.initialChild = initialChild;
            this.treeCache = treeCache;
            this.projectedVariables = initialChild.getVariables();
        }

        IQTree normalize() {
            State<UnaryOperatorNode, FilterSubTree> initial = new State<>(new FilterSubTree(Optional.of(initialFilterNode), initialChild));

            State<UnaryOperatorNode, FilterSubTree> state = initial.reachFixedPoint(
                    s -> simplifyAndPropagateDownConstraint(normalizeAndLiftConstructionDistinctFilterInnerJoin(s)),
                    MAX_NORMALIZATION_ITERATIONS);

            return asIQTree(state);
        }


        /**
         * A sequence of ConstructionNode and DistinctNode,
         * followed by an optional FilterNode, followed by a child tree.
         *
         * The initial state has a non-empty FilterNode, which can become empty
         * only after lifting a single InnerJoinNode
         * (which terminates the normalization process).
         */


        State<UnaryOperatorNode, FilterSubTree> normalizeAndLiftConstructionDistinctFilterInnerJoin(State<UnaryOperatorNode, FilterSubTree> state) {
            return IQStateOptionalTransformer.reachFinalState(
                    state, this::normalizeChild, this::liftConstructionDistinctFilterInnerJoin);
        }

        Optional<State<UnaryOperatorNode, FilterSubTree>> liftConstructionDistinctFilterInnerJoin(State<UnaryOperatorNode, FilterSubTree> state) {
            FilterSubTree subTree = state.getSubTree();
            return subTree.getChild().acceptVisitor(new IQStateOptionalTransformer<>() {

                @Override
                public Optional<State<UnaryOperatorNode, FilterSubTree>> transformConstruction(UnaryIQTree tree, ConstructionNode node, IQTree  newChild) {
                    var newOptionalFilterNode = subTree.getOptionalNode()
                            .map(FilterNode::getFilterCondition)
                            .map(e -> node.getSubstitution().apply(e))
                            .map(iqFactory::createFilterNode);
                    return Optional.of(state.of(node, new FilterSubTree(newOptionalFilterNode, newChild)));
                }

                @Override
                public Optional<State<UnaryOperatorNode, FilterSubTree>> transformDistinct(UnaryIQTree tree, DistinctNode node, IQTree newChild) {
                    return Optional.of(state.of(node, new FilterSubTree(subTree.getOptionalNode(), newChild)));
                }

                @Override
                public Optional<State<UnaryOperatorNode, FilterSubTree>> transformFilter(UnaryIQTree tree, FilterNode node, IQTree newChild) {
                    var newFilterNode = iqFactory.createFilterNode(
                            iqTreeTools.getConjunction(
                                    subTree.getOptionalNode().map(FilterNode::getFilterCondition),
                                    node.getFilterCondition()));
                    return Optional.of(state.of(new FilterSubTree(Optional.of(newFilterNode), newChild)));
                }

                @Override
                public Optional<State<UnaryOperatorNode, FilterSubTree>> transformInnerJoin(NaryIQTree tree, InnerJoinNode node, ImmutableList <IQTree> children) {
                    if (subTree.getOptionalNode().isPresent()) {
                        var newJoiningCondition = iqTreeTools.getConjunction(
                                subTree.getOptionalNode().get().getFilterCondition(),
                                node.getOptionalFilterCondition());

                        IQTree newChild = iqTreeTools.createInnerJoinTree(Optional.of(newJoiningCondition), children);
                        // will be final on the next call as the filter will be empty then
                        return Optional.of(state.of(new FilterSubTree(Optional.empty(), newChild)));
                    }
                    return done();
                }
            });
        }

        /**
         * Returns a tree in which the "filter-level" sub-tree is declared as normalized.
         */
        IQTree asIQTree(State<UnaryOperatorNode, FilterSubTree> state) {

            if (state.getSubTree().getChild().isDeclaredAsEmpty())
                return iqFactory.createEmptyNode(projectedVariables);

            IQTree filterLevelTree = iqTreeTools.unaryIQTreeBuilder()
                    .append(state.getSubTree().getOptionalNode(), treeCache::declareAsNormalizedForOptimizationWithEffect)
                    .build(state.getSubTree().getChild());

            return asIQTree(state.getAncestors(), filterLevelTree, iqTreeTools);
        }

        State<UnaryOperatorNode, FilterSubTree> normalizeChild(State<UnaryOperatorNode, FilterSubTree> state) {
            return state.of(new FilterSubTree(state.getSubTree().getOptionalNode(), state.getSubTree().getChild().normalizeForOptimization(variableGenerator)));
        }

        State<UnaryOperatorNode, FilterSubTree> simplifyAndPropagateDownConstraint(State<UnaryOperatorNode, FilterSubTree> state) {
            FilterSubTree subTree = state.getSubTree();
            if (subTree.getOptionalNode().isEmpty())
                return state;

            try {
                var childVariableNullability = subTree.getChild().getVariableNullability();

                // TODO: also consider the constraint for simplifying the condition
                var simplifiedFilterCondition = conditionSimplifier.simplifyCondition(
                        subTree.getOptionalNode().get().getFilterCondition(), ImmutableList.of(subTree.getChild()), childVariableNullability);

                var extendedDownConstraint = conditionSimplifier.extendAndSimplifyDownConstraint(
                        new DownPropagation(projectedVariables), simplifiedFilterCondition, childVariableNullability);

                var newChild = extendedDownConstraint.propagate(subTree.getChild(), variableGenerator);

                return state.of(
                        iqTreeTools.createOptionalConstructionNode(subTree.getChild()::getVariables, simplifiedFilterCondition.getSubstitution()),
                        new FilterSubTree(iqTreeTools.createOptionalFilterNode(simplifiedFilterCondition.getOptionalExpression()),
                                newChild));
            }
            catch (UnsatisfiableConditionException e) {
                return new State<>(new FilterSubTree(Optional.empty(), iqFactory.createEmptyNode(projectedVariables)));
            }
        }
    }
}

