package it.unibz.inf.ontop.iq.node.normalization.impl;

import com.google.common.collect.ImmutableList;
import com.google.common.collect.ImmutableMap;
import com.google.common.collect.ImmutableMultimap;
import com.google.common.collect.Maps;
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

import java.util.AbstractCollection;
import java.util.Map;
import java.util.Optional;
import java.util.function.BooleanSupplier;

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
        Context context = new Context(sliceNode, initialChild, variableGenerator, treeCache);
        return context.normalize();
    }
    
    private class Context extends NormalizationContext {
        private final SliceNode sliceNode;
        private final IQTree initialChild;
        private final IQTreeCache treeCache;

        public Context(SliceNode sliceNode, IQTree initialChild, VariableGenerator variableGenerator, IQTreeCache treeCache) {
            super(variableGenerator);
            this.sliceNode = sliceNode;
            this.initialChild = initialChild;
            this.treeCache = treeCache;
        }

        public IQTree normalize() {
            Optional<Long> limit = sliceNode.getLimit();
            if (limit.isPresent() && limit.get() == 0)
                return iqFactory.createEmptyNode(initialChild.getVariables());

            IQTree newChild = initialChild.normalizeForOptimization(variableGenerator);

            return normalizeForOptimization(newChild, variableGenerator, treeCache, () -> !newChild.equals(initialChild));
        }

        protected IQTree normalizeForOptimization(IQTree newChild, VariableGenerator variableGenerator, IQTreeCache treeCache,
                                                  BooleanSupplier hasChildChanged) {

            var construction = IQTreeTools.UnaryIQTreeDecomposition.of(newChild, ConstructionNode.class);
            if (construction.isPresent())
                return liftChildConstruction(construction.getNode(), construction.getChild(), variableGenerator);

            var slice = IQTreeTools.UnaryIQTreeDecomposition.of(newChild, SliceNode.class);
            if (slice.isPresent())
                return mergeWithSliceChild(slice.getNode(), slice.getChild(), treeCache);

            if (newChild instanceof EmptyNode)
                return newChild;

            if ((newChild instanceof TrueNode)
                    || IQTreeTools.UnaryIQTreeDecomposition.of(newChild, AggregationNode.class)
                    .getOptionalNode()
                    .map(AggregationNode::getGroupingVariables)
                    .filter(AbstractCollection::isEmpty)
                    .isPresent())
                return sliceNode.getOffset() > 0
                        ? iqFactory.createEmptyNode(newChild.getVariables())
                        : newChild;

            if (newChild instanceof ValuesNode) {
                ValuesNode valuesNode = (ValuesNode) newChild;
                ImmutableList<ImmutableMap<Variable, Constant>> values = valuesNode.getValueMaps();
                if (values.size() <= sliceNode.getOffset())
                    return iqFactory.createEmptyNode(valuesNode.getVariables());

                return iqFactory.createValuesNode(
                                valuesNode.getVariables(),
                                // TODO: complain if the offset or the limit are too big to be casted as integers
                                values.subList((int)sliceNode.getOffset(),
                                        Integer.min(sliceNode.getLimit().map(l -> l + sliceNode.getOffset()).orElse(sliceNode.getOffset()).intValue(), values.size())))
                        .normalizeForOptimization(variableGenerator);
            }

            // Limit optimizations will apply under the following conditions
            // Rule 1: For offset = 0,
            // Rule 2: Limit optimizations are not disabled
            // Rule 3: Limit must not be null
            if ((sliceNode.getOffset() == 0) && sliceNode.getLimit().isPresent() && !settings.isLimitOptimizationDisabled())
                return normalizeLimitNoOffset(sliceNode.getLimit().get().intValue(), newChild, variableGenerator, treeCache, hasChildChanged);

            return iqFactory.createUnaryIQTree(sliceNode, newChild,
                    hasChildChanged.getAsBoolean()
                            ? treeCache.declareAsNormalizedForOptimizationWithEffect()
                            : treeCache.declareAsNormalizedForOptimizationWithoutEffect());
        }

        private IQTree liftChildConstruction(ConstructionNode childConstructionNode, IQTree child,
                                             VariableGenerator variableGenerator) {
            IQTree newSliceLevelTree = iqFactory.createUnaryIQTree(sliceNode, child)
                    .normalizeForOptimization(variableGenerator);
            return iqFactory.createUnaryIQTree(childConstructionNode, newSliceLevelTree,
                    iqFactory.createIQTreeCache(true));
        }

        private IQTree mergeWithSliceChild(SliceNode newChildRoot, IQTree child, IQTreeCache treeCache) {
            long newOffset = sliceNode.getOffset() + newChildRoot.getOffset();
            Optional<Long> newLimit = newChildRoot.getLimit()
                    .map(cl -> Math.max(cl - sliceNode.getOffset(), 0L))
                    .map(cl -> sliceNode.getLimit()
                            .map(l -> Math.min(cl, l))
                            .orElse(cl))
                    .or(sliceNode::getLimit);

            SliceNode newSliceNode = newLimit
                    .map(l -> iqFactory.createSliceNode(newOffset, l))
                    .orElseGet(() -> iqFactory.createSliceNode(newOffset));

            return iqFactory.createUnaryIQTree(newSliceNode, child, treeCache.declareAsNormalizedForOptimizationWithEffect());
        }

        /**
         * Limit > 0, no offset and limit optimization is enabled
         *
         */
        private IQTree normalizeLimitNoOffset(int limit, IQTree newChild, VariableGenerator variableGenerator,
                                              IQTreeCache treeCache, BooleanSupplier hasChildChanged) {
            QueryNode newChildRoot = newChild.getRootNode();
            // Only triggered if a child with a known cardinality is present directly under the UNION
            var union = NaryIQTreeTools.UnionDecomposition.of(newChild);
            if (union.isPresent()) {
                Optional<IQTree> newTree = union.getChildren().stream().anyMatch(c -> getKnownCardinality(c).isPresent())
                        ? simplifyUnionWithChildrenOfKnownCardinality(union.getNode(), union.getTree(), limit, variableGenerator)
                        : pushLimitInUnionChildren(union.getNode(), union.getChildren(), variableGenerator);
                if (newTree.isPresent())
                    return newTree.get();
            }
            var innerJoin = NaryIQTreeTools.InnerJoinDecomposition.of(newChild);
            // TODO: consider a more general technique (distinct removal in sub-tree)
            if (innerJoin.isPresent() && limit <= 1) {
                // Distinct-s can be eliminated
                var newJoinChildren = IQTreeTools.UnaryIQTreeDecomposition.getTails
                        (IQTreeTools.UnaryIQTreeDecomposition.of(innerJoin.getChildren(), DistinctNode.class));

                if (!innerJoin.getChildren().equals(newJoinChildren)) {
                    var updatedChildTree = iqFactory.createNaryIQTree(innerJoin.getNode(), newJoinChildren);
                    return normalizeForOptimization(updatedChildTree, variableGenerator, treeCache,
                            () -> true);
                }
            }
            var distinct = IQTreeTools.UnaryIQTreeDecomposition.of(newChild, DistinctNode.class);
            if (distinct.isPresent()) {
                if (limit <= 1)
                    // Distinct can be eliminated
                    return normalizeForOptimization(distinct.getChild(), variableGenerator, treeCache,
                            () -> true);

                var innerUnion = NaryIQTreeTools.UnionDecomposition.of(distinct.getChild())
                        .filter(d -> d.getChildren().stream().anyMatch(IQTree::isDistinct));
                if (innerUnion.isPresent()) {
                    Optional<IQTree> newTree = simplifyDistinctUnionWithDistinctChildren(
                            innerUnion.getNode(), innerUnion.getChildren(), limit, variableGenerator);

                    if (newTree.isPresent())
                        return newTree.get();
                }
            }

            return iqFactory.createUnaryIQTree(sliceNode, newChild,
                    hasChildChanged.getAsBoolean()
                            ? treeCache.declareAsNormalizedForOptimizationWithEffect()
                            : treeCache.declareAsNormalizedForOptimizationWithoutEffect());
        }

        private Optional<IQTree> simplifyUnionWithChildrenOfKnownCardinality(UnionNode childRoot,
                                                                             IQTree childTree, int limit,
                                                                             VariableGenerator variableGenerator) {
            ImmutableList<IQTree> children = childTree.getChildren();

            ImmutableMultimap<IQTree, Integer> cardinalityMultimap = children.stream()
                    .flatMap(c -> getKnownCardinality(c)
                            .map(card -> Maps.immutableEntry(c, card)).stream())
                    .collect(ImmutableCollectors.toMultimap());

            Optional<Integer> maxChildCardinality = cardinalityMultimap.values().stream().max(Integer::compareTo);

            if (maxChildCardinality.isEmpty())
                return Optional.empty();

            if (maxChildCardinality.get() >= limit) {
                IQTree largestChild = cardinalityMultimap.inverse().get(maxChildCardinality.get()).stream()
                        .findAny()
                        .orElseThrow(() -> new MinorOntopInternalBugException("There should be one child"));

                return Optional.of(iqFactory.createUnaryIQTree(sliceNode, largestChild)
                        .normalizeForOptimization(variableGenerator));
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
                return Optional.of(
                        iqFactory.createNaryIQTree(
                                        childRoot,
                                        newChildrenBuilder.build())
                                .normalizeForOptimization(variableGenerator));
            }

            int numberOfChildrenWithUnknownCardinality = children.size() - cardinalityMultimap.size();

            if (numberOfChildrenWithUnknownCardinality == 0)
                // No more limit
                return Optional.of(childTree);

            ImmutableList<IQTree> newChildren = NaryIQTreeTools.transformChildren(children,
                    c -> cardinalityMultimap.containsKey(c)
                            ? c
                            : iqFactory.createUnaryIQTree(
                            iqFactory.createSliceNode(0, limit - sum), c));

            IQTree newUnionTree = iqFactory.createNaryIQTree(childRoot, newChildren)
                    .normalizeForOptimization(variableGenerator);

            return (numberOfChildrenWithUnknownCardinality == 1)
                    ? Optional.of(newUnionTree)
                    : newUnionTree.equals(childTree)
                    ? Optional.empty()
                    : Optional.of(iqFactory.createUnaryIQTree(sliceNode, newUnionTree)
                    .normalizeForOptimization(variableGenerator));
        }

        private Optional<IQTree> pushLimitInUnionChildren(UnionNode unionNode, ImmutableList<IQTree> children, VariableGenerator variableGenerator) {
            ImmutableList<IQTree> newUnionChildren = NaryIQTreeTools.transformChildren(children,
                    c -> iqFactory.createUnaryIQTree(sliceNode, c)
                            .normalizeForOptimization(variableGenerator));

            return children.equals(newUnionChildren)
                    ? Optional.empty()
                    : Optional.of(iqFactory.createUnaryIQTree(
                    sliceNode,
                    iqFactory.createNaryIQTree(unionNode, newUnionChildren)));
        }

        private Optional<IQTree> simplifyDistinctUnionWithDistinctChildren(UnionNode union, ImmutableList<IQTree> unionChildren, int limit, VariableGenerator variableGenerator) {

            Optional<IQTree> sufficientChild = unionChildren.stream()
                    .filter(IQTree::isDistinct)
                    .filter(c -> getKnownCardinality(c)
                            .filter(card -> card >= limit)
                            .isPresent())
                    .findAny();

            if (sufficientChild.isPresent())
                // Eliminates the distinct and the union
                return Optional.of(iqFactory.createUnaryIQTree(sliceNode, sufficientChild.get())
                        .normalizeForOptimization(variableGenerator));

            // Scenario: LIMIT DISTINCT UNION [T1 ...] -> LIMIT DISTINCT UNION [LIMIT T1 ...] if T1 is distinct
            ImmutableList<IQTree> newUnionChildren = unionChildren.stream()
                    .map(c -> c.isDistinct()
                            ? iqFactory.createUnaryIQTree(sliceNode, c)
                            : c)
                    .map(c -> c.normalizeForOptimization(variableGenerator))
                    .collect(ImmutableCollectors.toList());

            return newUnionChildren.equals(unionChildren)
                    ? Optional.empty()
                    : Optional.of(iqTreeTools.unaryIQTreeBuilder()
                    .append(sliceNode)
                    .append(iqFactory.createDistinctNode())
                    .build(iqFactory.createNaryIQTree(union, newUnionChildren))
                    .normalizeForOptimization(variableGenerator));
        }


        private Optional<Integer> getKnownCardinality(IQTree tree) {
            if (tree instanceof TrueNode)
                return Optional.of(1);
            if (tree instanceof ValuesNode)
                return Optional.of(((ValuesNode) tree).getValueMaps().size());

            var construction = IQTreeTools.UnaryIQTreeDecomposition.of(tree, ConstructionNode.class);
            if (construction.isPresent())
                return getKnownCardinality(construction.getChild());
            // TODO: shall we consider other nodes, like union nodes?
            return Optional.empty();
        }


    }

}
