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

    private class Context extends NormalizationContext {
        private final UnarySubTree<FilterNode> initialSubTree;
        private final ImmutableSet<Variable> projectedVariables;
        private final IQTreeCache treeCache;

        Context(FilterNode initialFilterNode, IQTree initialChild, VariableGenerator variableGenerator, IQTreeCache treeCache) {
            super(variableGenerator);
            this.initialSubTree = UnarySubTree.of(Optional.of(initialFilterNode), initialChild);
            this.projectedVariables = initialChild.getVariables();
            this.treeCache = treeCache;
        }

        /**
         * A sequence of CONSTRUCT and DISTINCT,
         * followed by an optional FILTER, followed by a child tree.
         *
         * The initial state has a non-empty FILTER, which can become empty
         * only after lifting a single INNER JOIN, which terminates the lifting process.
         */

        IQTree normalize() {
            var initial = State.initial(initialSubTree);
            var state = initial.reachFixedPoint(MAX_NORMALIZATION_ITERATIONS,
                    this::normalizeAndLiftConstructionDistinctFilterInnerJoin,
                    this::simplifyAndPropagateDownConstraint);
            return asIQTree(state);
        }

        State<UnaryOperatorNode, UnarySubTree<FilterNode>> normalizeAndLiftConstructionDistinctFilterInnerJoin(State<UnaryOperatorNode, UnarySubTree<FilterNode>> state) {
            return state.replace(this::normalizeChild).reachFinal(this::liftThroughFilter);
        }

        /**
         * One-step lifting of CONSTRUCT, DISTINCT and FILTER nodes through the FILTER.
         * An INNER JOIN child node absorbs the FILTER, which terminates lifting
         * (on the next iteration).
         * The child is assumed to be normalized, so repeated applications are possible
         * (without the need to normalize the child again).
         */
        Optional<State<UnaryOperatorNode, UnarySubTree<FilterNode>>> liftThroughFilter(State<UnaryOperatorNode, UnarySubTree<FilterNode>> state) {
            UnarySubTree<FilterNode> subTree = state.getSubTree();
            Optional<FilterNode> optionalFilterNode = subTree.getOptionalNode();
            if (optionalFilterNode.isEmpty())
                return Optional.empty();

            FilterNode filterNode = optionalFilterNode.get();
            return subTree.getChild().acceptVisitor(new IQStateOptionalTransformer<>() {

                @Override
                public Optional<State<UnaryOperatorNode, UnarySubTree<FilterNode>>> transformConstruction(UnaryIQTree tree, ConstructionNode node, IQTree  newChild) {
                    var newFilterNode = iqFactory.createFilterNode(
                            node.getSubstitution().apply(filterNode.getFilterCondition()));
                    return Optional.of(state.lift(node, UnarySubTree.of(Optional.of(newFilterNode), newChild)));
                }

                @Override
                public Optional<State<UnaryOperatorNode, UnarySubTree<FilterNode>>> transformDistinct(UnaryIQTree tree, DistinctNode node, IQTree newChild) {
                    return Optional.of(state.lift(node, UnarySubTree.of(optionalFilterNode, newChild)));
                }

                @Override
                public Optional<State<UnaryOperatorNode, UnarySubTree<FilterNode>>> transformFilter(UnaryIQTree tree, FilterNode node, IQTree newChild) {
                    var newFilterNode = iqFactory.createFilterNode(
                            iqTreeTools.getConjunction(filterNode.getFilterCondition(), node.getFilterCondition()));
                    return Optional.of(state.replace(UnarySubTree.of(Optional.of(newFilterNode), newChild)));
                }

                @Override
                public Optional<State<UnaryOperatorNode, UnarySubTree<FilterNode>>> transformInnerJoin(NaryIQTree tree, InnerJoinNode node, ImmutableList <IQTree> children) {
                    IQTree newChild = iqTreeTools.createInnerJoinTree(
                            Optional.of(iqTreeTools.getConjunction(
                                    filterNode.getFilterCondition(), node.getOptionalFilterCondition())),
                            children);
                    // will be final on the next call as the FILTER will be empty then
                    return Optional.of(state.replace(UnarySubTree.of(Optional.empty(), newChild)));
                }
            });
        }

        /**
         * Returns a tree in which the "filter-level" sub-tree is declared as normalized.
         */
        IQTree asIQTree(State<UnaryOperatorNode, UnarySubTree<FilterNode>> state) {
            UnarySubTree<FilterNode> subTree = state.getSubTree();
            // deals with EMPTY and empty VALUES (but normalization of the child replaces it with EMPTY)
            if (subTree.getChild().isDeclaredAsEmpty())
                return iqFactory.createEmptyNode(projectedVariables);

            IQTree filterLevelTree = iqTreeTools.unaryIQTreeBuilder()
                    .append(subTree.getOptionalNode(), treeCache::declareAsNormalizedForOptimizationWithEffect)
                    .build(subTree.getChild());

            return asIQTree(state.getAncestors(), filterLevelTree, iqTreeTools);
        }

        State<UnaryOperatorNode, UnarySubTree<FilterNode>> simplifyAndPropagateDownConstraint(State<UnaryOperatorNode, UnarySubTree<FilterNode>> state) {
            UnarySubTree<FilterNode> subTree = state.getSubTree();
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

                return state.lift(
                        iqTreeTools.createOptionalConstructionNode(subTree.getChild()::getVariables, simplifiedFilterCondition.getSubstitution()),
                        UnarySubTree.of(iqTreeTools.createOptionalFilterNode(simplifiedFilterCondition.getOptionalExpression()),
                                newChild));
            }
            catch (UnsatisfiableConditionException e) {
                return State.initial(UnarySubTree.of(Optional.empty(), iqFactory.createEmptyNode(projectedVariables)));
            }
        }
    }
}

