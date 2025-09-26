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
    public IQTree normalizeForOptimization(FilterNode filterNode, IQTree child, VariableGenerator variableGenerator, IQTreeCache treeCache) {
        Context context = new Context(child.getVariables(), variableGenerator, treeCache);
        return context.normalize(filterNode, child);
    }

    private class Context extends NormalizationContext {

        Context(ImmutableSet<Variable> projectedVariables, VariableGenerator variableGenerator, IQTreeCache treeCache) {
            super(projectedVariables, variableGenerator, treeCache, FilterNormalizerImpl.this.iqTreeTools);
        }

        /**
         * A state is a sequence of CONSTRUCT and DISTINCT,
         * followed by an optional FILTER, followed by a normalized child tree.
         *
         * The initial state has a non-empty FILTER, which can become empty
         * only after lifting a single INNER JOIN, which terminates the lifting process.
         */

        IQTree normalize(FilterNode filterNode, IQTree child) {
            var initial = State.initial(UnarySubTree.of(filterNode, normalizeSubTreeRecursively(child)));
            var state = initial.reachFixedPoint(MAX_NORMALIZATION_ITERATIONS,
                    this::liftThroughFilter,
                    this::simplifyAndPropagateDownConstraint);
            return asIQTree(state);
        }

        State<UnaryOperatorNode, UnarySubTree<FilterNode>> liftThroughFilter(State<UnaryOperatorNode, UnarySubTree<FilterNode>> state) {
            return state.reachFinal(this::liftThroughFilterStep);
        }

        /**
         * One-step lifting of CONSTRUCT, DISTINCT and FILTER nodes through the FILTER.
         * An INNER JOIN child node absorbs the FILTER, which terminates lifting
         * (on the next iteration). Note that the INNER JOIN needs to be normalized.
         */
        Optional<State<UnaryOperatorNode, UnarySubTree<FilterNode>>> liftThroughFilterStep(State<UnaryOperatorNode, UnarySubTree<FilterNode>> state) {
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
                    return Optional.of(state.lift(node, UnarySubTree.of(newFilterNode, newChild)));
                }

                @Override
                public Optional<State<UnaryOperatorNode, UnarySubTree<FilterNode>>> transformDistinct(UnaryIQTree tree, DistinctNode node, IQTree newChild) {
                    return Optional.of(state.lift(node, UnarySubTree.of(filterNode, newChild)));
                }

                @Override
                public Optional<State<UnaryOperatorNode, UnarySubTree<FilterNode>>> transformFilter(UnaryIQTree tree, FilterNode node, IQTree newChild) {
                    var newFilterNode = iqFactory.createFilterNode(
                            iqTreeTools.getConjunction(filterNode.getFilterCondition(), node.getFilterCondition()));
                    return Optional.of(state.replace(UnarySubTree.of(newFilterNode, newChild)));
                }

                @Override
                public Optional<State<UnaryOperatorNode, UnarySubTree<FilterNode>>> transformInnerJoin(NaryIQTree tree, InnerJoinNode node, ImmutableList <IQTree> children) {
                    IQTree newChild = normalizeSubTreeRecursively(
                            iqTreeTools.createInnerJoinTree(
                                    Optional.of(iqTreeTools.getConjunction(
                                            filterNode.getFilterCondition(), node.getOptionalFilterCondition())),
                                    children));
                    return Optional.of(state.replace(UnarySubTree.finalSubTree(newChild)));
                }
            });
        }

        State<UnaryOperatorNode, UnarySubTree<FilterNode>> simplifyAndPropagateDownConstraint(State<UnaryOperatorNode, UnarySubTree<FilterNode>> state) {
            UnarySubTree<FilterNode> subTree = state.getSubTree();
            if (subTree.getOptionalNode().isEmpty())
                return state;

            FilterNode filterNode = subTree.getOptionalNode().get();
            IQTree child = subTree.getChild();
            try {
                var childVariableNullability = child.getVariableNullability();

                var simplification = conditionSimplifier.simplifyAndPropagate(
                        DownPropagation.of(Optional.empty(), child.getVariables(), variableGenerator, null),
                        Optional.of(filterNode.getFilterCondition()),
                        ImmutableList.of(child),
                        childVariableNullability);

                return state.lift(
                        simplification.getConstructionNode(),
                        UnarySubTree.of(iqTreeTools.createOptionalFilterNode(simplification.getOptionalExpression()),
                                normalizeSubTreeRecursively(simplification.getChildren().get(0))));
            }
            catch (UnsatisfiableConditionException e) {
                return State.initial(UnarySubTree.finalSubTree(createEmptyNode()));
            }
        }

        /**
         * Returns a tree in which the "filter-level" sub-tree is declared as normalized.
         */
        IQTree asIQTree(State<UnaryOperatorNode, UnarySubTree<FilterNode>> state) {
            UnarySubTree<FilterNode> subTree = state.getSubTree();
            if (subTree.getChild().isDeclaredAsEmpty())
                return createEmptyNode();

            IQTree filterLevelTree = iqTreeTools.unaryIQTreeBuilder()
                    .append(subTree.getOptionalNode(), () -> getNormalizedTreeCache(true))
                    .build(subTree.getChild());

            return asIQTree(state.getAncestors(), filterLevelTree);
        }
    }
}

