package it.unibz.inf.ontop.iq.node.normalization.impl;

import com.google.common.collect.*;
import com.google.inject.Inject;
import com.google.inject.Singleton;
import it.unibz.inf.ontop.exception.MinorOntopInternalBugException;
import it.unibz.inf.ontop.injection.CoreSingletons;
import it.unibz.inf.ontop.injection.IntermediateQueryFactory;
import it.unibz.inf.ontop.injection.OntopModelSettings;
import it.unibz.inf.ontop.iq.IQTree;
import it.unibz.inf.ontop.iq.IQTreeCache;
import it.unibz.inf.ontop.iq.impl.IQTreeTools;
import it.unibz.inf.ontop.iq.impl.NaryIQTreeTools;
import it.unibz.inf.ontop.iq.node.*;
import it.unibz.inf.ontop.iq.node.normalization.SliceNormalizer;
import it.unibz.inf.ontop.model.term.Constant;
import it.unibz.inf.ontop.model.term.Variable;
import it.unibz.inf.ontop.utils.ImmutableCollectors;
import it.unibz.inf.ontop.utils.VariableGenerator;

import java.util.Map;
import java.util.Optional;
import java.util.OptionalInt;
import java.util.OptionalLong;

@Singleton
public class SliceNormalizerImpl implements SliceNormalizer {
    private final IntermediateQueryFactory iqFactory;
    private final IQTreeTools iqTreeTools;
    private final OntopModelSettings settings;

    @Inject
    private SliceNormalizerImpl(CoreSingletons coreSingletons, OntopModelSettings settings) {
        this.iqFactory = coreSingletons.getIQFactory();
        this.iqTreeTools = coreSingletons.getIQTreeTools();
        this.settings = settings;
    }

    @Override
    public IQTree normalizeForOptimization(SliceNode sliceNode, IQTree initialChild,
                                           VariableGenerator variableGenerator, IQTreeCache treeCache) {
        Context context = new Context(initialChild.getVariables(), variableGenerator, treeCache);
        return context.normalize(sliceNode, initialChild);
    }

    private class Context extends NormalizationContext {

        Context(ImmutableSet<Variable> projectedVariables, VariableGenerator variableGenerator, IQTreeCache treeCache) {
            super(projectedVariables, variableGenerator, treeCache, SliceNormalizerImpl.this.iqTreeTools);
        }

        IQTree normalize(SliceNode sliceNode, IQTree initialChild) {
            OptionalLong limit = sliceNode.getLimit();
            if (limit.isPresent() && limit.getAsLong() == 0)
                return createEmptyNode();

            IQTree newChild = normalizeSubTreeRecursively(initialChild);
            var initial = State.<ConstructionNode, UnarySubTree<SliceNode>>initial(UnarySubTree.of(sliceNode, newChild));
            var state = initial.reachFinal(this::simplifySlice);

            return asIQTree(state);
        }

        IQTree asIQTree(State<ConstructionNode, UnarySubTree<SliceNode>> state) {
            UnarySubTree<SliceNode> subTree = state.getSubTree();
            if (subTree.getChild().isDeclaredAsEmpty())
                return createEmptyNode();

            IQTree sliceLevelTree = iqTreeTools.unaryIQTreeBuilder()
                    .append(subTree.getOptionalNode(), () -> getNormalizedTreeCache(true))
                    .build(subTree.getChild());

            return asIQTree(state.getAncestors(), sliceLevelTree);
        }

        private Optional<State<ConstructionNode, UnarySubTree<SliceNode>>> simplifySlice(State<ConstructionNode, UnarySubTree<SliceNode>> state) {
            var subTree = state.getSubTree();
            if (subTree.getOptionalNode().isEmpty())
                return Optional.empty();

            var sliceNode = subTree.getOptionalNode().get();

            var construction = IQTreeTools.UnaryIQTreeDecomposition.of(subTree.getChild(), ConstructionNode.class);
            if (construction.isPresent()) {
                return Optional.of(state.lift(construction.getNode(), subTree.replaceChild(construction.getChild())));
            }

            var slice = IQTreeTools.UnaryIQTreeDecomposition.of(subTree.getChild(), SliceNode.class);
            if (slice.isPresent())
                return Optional.of(state.replace(UnarySubTree.of(mergeSliceNodes(sliceNode, slice.getNode()), slice.getChild())));

            if (subTree.getChild() instanceof EmptyNode)
                return Optional.of(state.replace(UnarySubTree.finalSubTree(subTree.getChild())));

            if ((subTree.getChild() instanceof TrueNode)
                    || IQTreeTools.UnaryIQTreeDecomposition.of(subTree.getChild(), AggregationNode.class)
                    .getOptionalNode()
                    .map(AggregationNode::getGroupingVariables)
                    .filter(ImmutableSet::isEmpty)
                    .isPresent())
                return Optional.of(state.replace(UnarySubTree.finalSubTree(
                        sliceNode.getOffset() > 0 ? createEmptyNode() : subTree.getChild())));

            if (subTree.getChild() instanceof ValuesNode) {
                ValuesNode valuesNode = (ValuesNode) subTree.getChild();
                ImmutableList<ImmutableMap<Variable, Constant>> values = valuesNode.getValueMaps();
                IQTree newTree = (values.size() <= sliceNode.getOffset())
                    ? createEmptyNode()
                    : normalizeSubTreeRecursively(
                        iqFactory.createValuesNode(
                                valuesNode.getVariables(),
                                // TODO: complain if the offset or the limit are too big to be cast as integers
                                values.subList((int)sliceNode.getOffset(),
                                        Integer.min(
                                                sliceNode.getLimit().isPresent()
                                                        ? (int)(sliceNode.getOffset() + sliceNode.getLimit().getAsLong())
                                                        : values.size(),
                                                values.size()))));

                return Optional.of(state.replace(UnarySubTree.finalSubTree(newTree)));
            }

            if ((sliceNode.getOffset() == 0) && sliceNode.getLimit().isPresent() && !settings.isLimitOptimizationDisabled())
                return normalizeLimitNoOffset(state);

            return Optional.empty();
        }

        private SliceNode mergeSliceNodes(SliceNode sliceNode, SliceNode innerSliceNode) {
            long newOffset = sliceNode.getOffset() + innerSliceNode.getOffset();
            final OptionalLong newLimit;
            if (innerSliceNode.getLimit().isPresent()) {
                long offsetLimit = Math.max(innerSliceNode.getLimit().getAsLong() - sliceNode.getOffset(), 0L);
                newLimit = OptionalLong.of(
                        sliceNode.getLimit().isPresent()
                                ? Math.min(sliceNode.getLimit().getAsLong(), offsetLimit)
                                : offsetLimit);
            }
            else
                newLimit = sliceNode.getLimit();

            return newLimit.isPresent()
                    ? iqFactory.createSliceNode(newOffset, newLimit.getAsLong())
                    : iqFactory.createSliceNode(newOffset);
        }

        /**
         * Limit > 0, no offset and limit optimization is enabled
         */
        private Optional<State<ConstructionNode, UnarySubTree<SliceNode>>> normalizeLimitNoOffset(State<ConstructionNode, UnarySubTree<SliceNode>> state) {
            var subTree = state.getSubTree();

            var union = NaryIQTreeTools.UnionDecomposition.of(subTree.getChild());
            if (union.isPresent())
                return simplifyUnion(state, union);

            //noinspection OptionalGetWithoutIsPresent
            int limit = (int)subTree.getOptionalNode().get().getLimit().getAsLong();

            var innerJoin = NaryIQTreeTools.InnerJoinDecomposition.of(subTree.getChild());
            // TODO: consider a more general technique (distinct removal in sub-tree)
            if (innerJoin.isPresent() && limit <= 1) {
                // Distinct-s can be eliminated
                var newJoinChildren = IQTreeTools.UnaryIQTreeDecomposition.getTails
                        (IQTreeTools.UnaryIQTreeDecomposition.of(innerJoin.getChildren(), DistinctNode.class));

                if (!innerJoin.getChildren().equals(newJoinChildren)) {
                    return Optional.of(state.replace(subTree.replaceChild(normalizeSubTreeRecursively(
                            iqFactory.createNaryIQTree(innerJoin.getNode(), newJoinChildren)))));
                }
            }

            var distinct = IQTreeTools.UnaryIQTreeDecomposition.of(subTree.getChild(), DistinctNode.class);
            if (distinct.isPresent()) {
                if (limit <= 1) // Distinct can be eliminated
                    return Optional.of(state.replace(subTree.replaceChild(distinct.getChild())));

                var innerUnion = NaryIQTreeTools.UnionDecomposition.of(distinct.getChild());
                if (innerUnion.isPresent())
                    return simplifyDistinctUnion(state, innerUnion);
            }

            return Optional.empty();
        }

        private Optional<State<ConstructionNode, UnarySubTree<SliceNode>>> simplifyUnion(State<ConstructionNode, UnarySubTree<SliceNode>> state, NaryIQTreeTools.UnionDecomposition union) {
            var subTree = state.getSubTree();
            //noinspection OptionalGetWithoutIsPresent
            var sliceNode = subTree.getOptionalNode().get();

            var children = union.getTree().getChildren();

            ImmutableMultimap<IQTree, Integer> cardinalityMultimap = children.stream()
                    .flatMap(c -> getKnownCardinality(c).stream()
                            .mapToObj(card -> Maps.immutableEntry(c, card)))
                    .collect(ImmutableCollectors.toMultimap());

            Optional<Integer> maxChildCardinality = cardinalityMultimap.values().stream().max(Integer::compareTo);
            if (maxChildCardinality.isEmpty()) {
                ImmutableList<IQTree> newUnionChildren = NaryIQTreeTools.transformChildren(children,
                        c -> normalizeSubTreeRecursively(iqFactory.createUnaryIQTree(sliceNode, c)));

                if (children.equals(newUnionChildren))
                    return Optional.empty();

                return Optional.of(state.replace(subTree.replaceChild(normalizeSubTreeRecursively(
                        iqFactory.createNaryIQTree(union.getNode(), newUnionChildren)))));
            }

            //noinspection OptionalGetWithoutIsPresent
            int limit = (int)sliceNode.getLimit().getAsLong();
            if (maxChildCardinality.get() >= limit) {
                IQTree largestChild = cardinalityMultimap.inverse().get(maxChildCardinality.get()).stream()
                        .findAny()
                        .orElseThrow(() -> new MinorOntopInternalBugException("There should be one child"));

                return Optional.of(state.replace(subTree.replaceChild(largestChild)));
            }

            int sum = cardinalityMultimap.values().stream().reduce(0, Integer::sum);
            if (sum >= limit) {
                // Non-final
                int remainingLimit = limit;
                ImmutableList.Builder<IQTree> newChildrenBuilder = ImmutableList.builder();
                for (Map.Entry<IQTree, Integer> entry : cardinalityMultimap.entries()) {
                    IQTree newChild = (entry.getValue() <= remainingLimit)
                            ? entry.getKey()
                            : iqFactory.createUnaryIQTree(
                            iqFactory.createSliceNode(0, remainingLimit),
                            entry.getKey());
                    newChildrenBuilder.add(newChild);
                    remainingLimit -= entry.getValue();
                    if (remainingLimit <= 0)
                        break;
                }
                // Should have at least 2 children, otherwise it would have been already optimized
                return Optional.of(state.replace(subTree.replaceChild(normalizeSubTreeRecursively(
                        iqFactory.createNaryIQTree(union.getNode(), newChildrenBuilder.build())))));
            }

            int numberOfChildrenWithUnknownCardinality = children.size() - cardinalityMultimap.size();
            if (numberOfChildrenWithUnknownCardinality == 0)
                // No more limit (sum < limit)
                return Optional.of(state.replace(UnarySubTree.finalSubTree(subTree.getChild())));

            ImmutableList<IQTree> newChildren = NaryIQTreeTools.transformChildren(children,
                    c -> cardinalityMultimap.containsKey(c)
                            ? c
                            : iqFactory.createUnaryIQTree(
                            iqFactory.createSliceNode(0, limit - sum), c));

            IQTree newUnionTree = normalizeSubTreeRecursively(
                    iqFactory.createNaryIQTree(union.getNode(), newChildren));

            if (numberOfChildrenWithUnknownCardinality == 1)
                return Optional.of(state.replace(UnarySubTree.finalSubTree(newUnionTree)));

            if (newUnionTree.equals(union.getTree()))
                return Optional.empty();

            return Optional.of(state.replace(subTree.replaceChild(newUnionTree)));
        }

        private Optional<State<ConstructionNode, UnarySubTree<SliceNode>>> simplifyDistinctUnion(State<ConstructionNode, UnarySubTree<SliceNode>> state, NaryIQTreeTools.UnionDecomposition union) {
            var subTree = state.getSubTree();
            //noinspection OptionalGetWithoutIsPresent
            var sliceNode = subTree.getOptionalNode().get();
            //noinspection OptionalGetWithoutIsPresent
            int limit = (int)sliceNode.getLimit().getAsLong();

            var unionChildren = union.getChildren();
            if (unionChildren.stream().noneMatch(IQTree::isDistinct))
                return Optional.empty();

            Optional<IQTree> sufficientChild = unionChildren.stream()
                    .filter(IQTree::isDistinct)
                    .filter(c -> getKnownCardinality(c).stream()
                            .filter(card -> card >= limit)
                            .findAny()
                            .isPresent())
                    .findAny();

            if (sufficientChild.isPresent())
                // Eliminates the distinct and the union
                return Optional.of(state.replace(subTree.replaceChild(sufficientChild.get())));

            // Scenario: LIMIT DISTINCT UNION [T1 ...] -> LIMIT DISTINCT UNION [LIMIT T1 ...] if T1 is distinct
            ImmutableList<IQTree> newUnionChildren = NaryIQTreeTools.transformChildren(
                    unionChildren,
                    c -> c.isDistinct()
                            ? normalizeSubTreeRecursively(iqFactory.createUnaryIQTree(sliceNode, c))
                            : c);

            if (newUnionChildren.equals(unionChildren))
                    return Optional.empty();

            return Optional.of(state.replace(subTree.replaceChild(normalizeSubTreeRecursively(
                    iqTreeTools.unaryIQTreeBuilder()
                            .append(iqFactory.createDistinctNode())
                            .build(iqFactory.createNaryIQTree(union.getNode(), newUnionChildren))))));
        }

        private OptionalInt getKnownCardinality(IQTree tree) {
            if (tree instanceof TrueNode)
                return OptionalInt.of(1);
            if (tree instanceof ValuesNode)
                return OptionalInt.of(((ValuesNode) tree).getValueMaps().size());

            var construction = IQTreeTools.UnaryIQTreeDecomposition.of(tree, ConstructionNode.class);
            if (construction.isPresent())
                return getKnownCardinality(construction.getChild());
            // TODO: shall we consider other nodes, like union nodes?
            return OptionalInt.empty();
        }
    }
}
