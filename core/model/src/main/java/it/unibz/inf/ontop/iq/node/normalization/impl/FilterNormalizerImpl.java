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

        private FilterSubTree(Optional<FilterNode> optionalFilterNode, IQTree child) {
            this.optionalFilterNode = optionalFilterNode;
            this.child = child;
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
            NormalizationState2<UnaryOperatorNode, FilterSubTree> state = reachFixedPoint(
                    new NormalizationState2<>(new FilterSubTree(Optional.of(initialFilterNode), initialChild)),
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


        NormalizationState2<UnaryOperatorNode, FilterSubTree> normalizeAndLiftConstructionDistinctFilterInnerJoin(NormalizationState2<UnaryOperatorNode, FilterSubTree> state) {
            return IQStateOptionalTransformer.reachFinalState(
                    state, this::normalizeChild, this::liftConstructionDistinctFilterInnerJoin);
        }

        Optional<NormalizationState2<UnaryOperatorNode, FilterSubTree>> liftConstructionDistinctFilterInnerJoin(NormalizationState2<UnaryOperatorNode, FilterSubTree> state) {
            return state.getSubTree().child.acceptVisitor(new IQStateOptionalTransformer<>() {

                @Override
                public Optional<NormalizationState2<UnaryOperatorNode, FilterSubTree>> transformConstruction(UnaryIQTree tree, ConstructionNode node, IQTree  newChild) {
                    var newOptionalFilterNode = state.getSubTree().optionalFilterNode
                            .map(FilterNode::getFilterCondition)
                            .map(e -> node.getSubstitution().apply(e))
                            .map(iqFactory::createFilterNode);
                    return Optional.of(state.of(node, new FilterSubTree(newOptionalFilterNode, newChild)));
                }

                @Override
                public Optional<NormalizationState2<UnaryOperatorNode, FilterSubTree>> transformDistinct(UnaryIQTree tree, DistinctNode node, IQTree newChild) {
                    return Optional.of(state.of(node, new FilterSubTree(state.getSubTree().optionalFilterNode, newChild)));
                }

                @Override
                public Optional<NormalizationState2<UnaryOperatorNode, FilterSubTree>> transformFilter(UnaryIQTree tree, FilterNode node, IQTree newChild) {
                    var newFilterNode = iqFactory.createFilterNode(
                            iqTreeTools.getConjunction(
                                    state.getSubTree().optionalFilterNode.map(FilterNode::getFilterCondition),
                                    node.getFilterCondition()));
                    return Optional.of(state.of(new FilterSubTree(Optional.of(newFilterNode), newChild)));
                }

                @Override
                public Optional<NormalizationState2<UnaryOperatorNode, FilterSubTree>> transformInnerJoin(NaryIQTree tree, InnerJoinNode node, ImmutableList <IQTree> children) {
                    if (state.getSubTree().optionalFilterNode.isPresent()) {
                        var newJoiningCondition = iqTreeTools.getConjunction(
                                state.getSubTree().optionalFilterNode.get().getFilterCondition(),
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
        protected IQTree asIQTree(NormalizationState2<UnaryOperatorNode, FilterSubTree> state) {

            if (state.getSubTree().child.isDeclaredAsEmpty())
                return iqFactory.createEmptyNode(projectedVariables);

            IQTree filterLevelTree = iqTreeTools.unaryIQTreeBuilder()
                    .append(state.getSubTree().optionalFilterNode, treeCache::declareAsNormalizedForOptimizationWithEffect)
                    .build(state.getSubTree().child);

            if (state.getAncestors().isEmpty())
                return filterLevelTree;

            return iqTreeTools.unaryIQTreeBuilder()
                    .append(state.getAncestors())
                    .build(filterLevelTree)
                    // Normalizes the ancestors (recursive)
                    .normalizeForOptimization(variableGenerator);
        }

        NormalizationState2<UnaryOperatorNode, FilterSubTree> normalizeChild(NormalizationState2<UnaryOperatorNode, FilterSubTree> state) {
            return state.of(new FilterSubTree(state.getSubTree().optionalFilterNode, state.getSubTree().child.normalizeForOptimization(variableGenerator)));
        }

        NormalizationState2<UnaryOperatorNode, FilterSubTree> simplifyAndPropagateDownConstraint(NormalizationState2<UnaryOperatorNode, FilterSubTree> state) {
            if (state.getSubTree().optionalFilterNode.isEmpty())
                return state;

            try {
                var childVariableNullability = state.getSubTree().child.getVariableNullability();

                // TODO: also consider the constraint for simplifying the condition
                var simplifiedFilterCondition = conditionSimplifier.simplifyCondition(
                        state.getSubTree().optionalFilterNode.get().getFilterCondition(), ImmutableList.of(state.getSubTree().child), childVariableNullability);

                var extendedDownConstraint = conditionSimplifier.extendAndSimplifyDownConstraint(
                        new DownPropagation(projectedVariables), simplifiedFilterCondition, childVariableNullability);

                var newChild = extendedDownConstraint.propagate(state.getSubTree().child, variableGenerator);

                return new NormalizationState2<>(
                        state.getAncestors().append(iqTreeTools.createOptionalConstructionNode(state.getSubTree().child::getVariables, simplifiedFilterCondition.getSubstitution())),
                        new FilterSubTree(iqTreeTools.createOptionalFilterNode(simplifiedFilterCondition.getOptionalExpression()),
                                newChild));
            }
            catch (UnsatisfiableConditionException e) {
                return new NormalizationState2<>(UnaryOperatorSequence.of(), new FilterSubTree(Optional.empty(), iqFactory.createEmptyNode(projectedVariables)));
            }
        }
    }
}

