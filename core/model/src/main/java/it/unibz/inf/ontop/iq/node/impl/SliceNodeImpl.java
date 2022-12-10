package it.unibz.inf.ontop.iq.node.impl;

import com.google.common.collect.*;
import com.google.inject.assistedinject.Assisted;
import com.google.inject.assistedinject.AssistedInject;
import it.unibz.inf.ontop.exception.MinorOntopInternalBugException;
import it.unibz.inf.ontop.injection.IntermediateQueryFactory;
import it.unibz.inf.ontop.injection.OntopModelSettings;
import it.unibz.inf.ontop.iq.*;
import it.unibz.inf.ontop.iq.exception.InvalidIntermediateQueryException;
import it.unibz.inf.ontop.iq.exception.QueryNodeTransformationException;
import it.unibz.inf.ontop.iq.node.*;
import it.unibz.inf.ontop.iq.transform.IQTreeExtendedTransformer;
import it.unibz.inf.ontop.iq.transform.IQTreeVisitingTransformer;
import it.unibz.inf.ontop.iq.transform.node.HomogeneousQueryNodeTransformer;
import it.unibz.inf.ontop.iq.visit.IQVisitor;
import it.unibz.inf.ontop.model.term.ImmutableExpression;
import it.unibz.inf.ontop.model.term.Variable;
import it.unibz.inf.ontop.model.term.VariableOrGroundTerm;
import it.unibz.inf.ontop.substitution.ImmutableSubstitution;
import it.unibz.inf.ontop.substitution.InjectiveVar2VarSubstitution;
import it.unibz.inf.ontop.utils.ImmutableCollectors;
import it.unibz.inf.ontop.utils.VariableGenerator;

import javax.annotation.Nullable;
import java.util.*;
import java.util.function.Supplier;
import java.util.stream.Stream;

public class SliceNodeImpl extends QueryModifierNodeImpl implements SliceNode {

    private static final String SLICE_STR = "SLICE";

    private final long offset;

    @Nullable
    private final Long limit;

    private final OntopModelSettings settings;

    @AssistedInject
    private SliceNodeImpl(@Assisted("offset") long offset, @Assisted("limit") long limit,
                          IntermediateQueryFactory iqFactory, OntopModelSettings settings) {
        super(iqFactory);
        if (offset < 0)
            throw new IllegalArgumentException("The offset must not be negative");
        if (limit < 0)
            throw new IllegalArgumentException("The limit must not be negative");
        this.offset = offset;
        this.limit = limit;
        this.settings = settings;
    }

    @AssistedInject
    private SliceNodeImpl(@Assisted long offset, IntermediateQueryFactory iqFactory, OntopModelSettings settings) {
        super(iqFactory);
        if (offset < 0)
            throw new IllegalArgumentException("The offset must not be negative");
        this.offset = offset;
        this.limit = null;
        this.settings = settings;
    }

    /**
     * Does not lift unions, blocks them
     */
    @Override
    public IQTree liftIncompatibleDefinitions(Variable variable, IQTree child, VariableGenerator variableGenerator) {
        return iqFactory.createUnaryIQTree(this, child);
    }

    @Override
    public IQTree normalizeForOptimization(IQTree child, VariableGenerator variableGenerator, IQTreeCache treeCache) {
        if ((limit != null) && limit == 0)
            return iqFactory.createEmptyNode(child.getVariables());

        IQTree newChild = child.normalizeForOptimization(variableGenerator);

        return normalizeForOptimization(newChild, variableGenerator, treeCache, () -> !newChild.equals(child));
    }

    protected IQTree normalizeForOptimization(IQTree newChild, VariableGenerator variableGenerator, IQTreeCache treeCache,
                                              Supplier<Boolean> hasChildChanged) {

        QueryNode newChildRoot = newChild.getRootNode();

        if (newChildRoot instanceof ConstructionNode)
            return liftChildConstruction((ConstructionNode) newChildRoot, (UnaryIQTree)newChild, variableGenerator);
        if (newChildRoot instanceof SliceNode)
            return mergeWithSliceChild((SliceNode) newChildRoot, (UnaryIQTree) newChild, treeCache);
        if (newChildRoot instanceof EmptyNode)
            return newChild;
        if ((newChildRoot instanceof TrueNode)
                || ((newChildRoot instanceof AggregationNode)
                    && ((AggregationNode) newChildRoot).getGroupingVariables().isEmpty()))
            return offset > 0
                    ? iqFactory.createEmptyNode(newChild.getVariables())
                    : newChild;

        // Optimizations for Values Node will apply under the following conditions
        // Rule 1: For offset > 0, we do not perform any Values Node optimizations
        // Rule 2: The optimization of the Slice can be controlled via the configuration properties
        // Rule 3: Limit must not be null
        if ((offset == 0) && limit != null && !settings.isLimitOptimizationDisabled())
            return normalizeLimitNoOffset(limit.intValue(), newChild, variableGenerator, treeCache, hasChildChanged);

        return iqFactory.createUnaryIQTree(this, newChild,
                hasChildChanged.get()
                        ? treeCache.declareAsNormalizedForOptimizationWithEffect()
                        : treeCache.declareAsNormalizedForOptimizationWithoutEffect());
    }

    private IQTree liftChildConstruction(ConstructionNode childConstructionNode, UnaryIQTree childTree,
                                         VariableGenerator variableGenerator) {
        IQTree newSliceLevelTree = iqFactory.createUnaryIQTree(this, childTree.getChild())
                .normalizeForOptimization(variableGenerator);
        return iqFactory.createUnaryIQTree(childConstructionNode, newSliceLevelTree,
                iqFactory.createIQTreeCache(true));
    }

    private IQTree mergeWithSliceChild(SliceNode newChildRoot, UnaryIQTree newChild, IQTreeCache treeCache) {
        long newOffset = offset + newChildRoot.getOffset();
        Optional<Long> newLimit = newChildRoot.getLimit()
                .map(cl -> Math.max(cl - offset, 0L))
                .map(cl -> getLimit()
                        .map(l -> Math.min(cl, l))
                        .orElse(cl))
                .map(Optional::of)
                // No limit in the child
                .orElseGet(this::getLimit);

        SliceNode newSliceNode = newLimit
                .map(l -> iqFactory.createSliceNode(newOffset, l))
                .orElseGet(() -> iqFactory.createSliceNode(newOffset));

        return iqFactory.createUnaryIQTree(newSliceNode, newChild.getChild(), treeCache.declareAsNormalizedForOptimizationWithEffect());
    }

    /**
     * Limit > 0, no offset and limit optimization is enabled
     *
     */
    private IQTree normalizeLimitNoOffset(int limit, IQTree newChild, VariableGenerator variableGenerator,
                                          IQTreeCache treeCache, Supplier<Boolean> hasChildChanged) {
        QueryNode newChildRoot = newChild.getRootNode();
        if ((newChildRoot instanceof DistinctNode) && limit <= 1)
            // Distinct can be eliminated
            return normalizeForOptimization(((UnaryIQTree) newChild).getChild(), variableGenerator, treeCache,
                    () -> true);
        if ((newChildRoot instanceof ValuesNode))
            return iqFactory.createValuesNode(((ValuesNode) newChildRoot).getOrderedVariables(),
                    ((ValuesNode) newChildRoot).getValues().subList(0, limit));
            // Only triggered if a child with a known cardinality is present directly under the UNION
        if ((newChildRoot instanceof UnionNode)
                && newChild.getChildren().stream().anyMatch(c -> getKnownCardinality(c).isPresent())) {
            Optional<IQTree> newTree = simplifyUnionWithChildrenOfKnownCardinality(
                    (UnionNode) newChildRoot, newChild, limit, variableGenerator);
            if (newTree.isPresent())
                return newTree.get();
            }
            // Scenario: SLICE DISTINCT UNION [VALUES ...] -> VALUES if VALUES is distinct
            // TODO: generalize and merge the LIMIT DISTINCT UNION optimizations
        if ((newChildRoot instanceof DistinctNode)
                && newChild.getChildren().size() == 1
                && newChild.getChildren().stream().allMatch(c -> c.getRootNode() instanceof UnionNode)
                && newChild.getChildren().get(0).getChildren().stream().anyMatch(c -> c instanceof ValuesNode)
                && newChild.getChildren().get(0).getChildren().stream().filter(c -> c instanceof ValuesNode).anyMatch(IQTree::isDistinct)) {
            return simplifyDistinctUnionValues(newChild, treeCache);
        }
            // Scenario: LIMIT DISTINCT UNION [T1 ...] -> LIMIT DISTINCT UNION [LIMIT T1 ...] if T1 is distinct
        if ((newChildRoot instanceof DistinctNode)
                // Applies only to LIMIT i.e. not SLICE with OFFSET
                && offset == 0
                && newChild.getChildren().size() == 1
                && newChild.getChildren().stream().allMatch(c -> c.getRootNode() instanceof UnionNode)
                // If any subtree Ti is distinct we proceed with the optimization
                && newChild.getChildren().get(0).getChildren().stream().anyMatch(IQTree::isDistinct)) {
            ImmutableList<IQTree> childTrees = newChild.getChildren().get(0).getChildren().stream()
                    // No stacking up of slices over one another
                    .map(c -> c.isDistinct() && !(c.getRootNode() instanceof SliceNode)
                            ? iqFactory.createUnaryIQTree(iqFactory.createSliceNode(0, limit), c)
                            : c)
                    .collect(ImmutableCollectors.toList());
            return iqFactory.createUnaryIQTree(iqFactory.createSliceNode(offset, limit),
                    iqFactory.createUnaryIQTree(iqFactory.createDistinctNode(),
                        iqFactory.createNaryIQTree((UnionNode) newChild.getChildren().get(0).getRootNode(),
                                childTrees)));
        }
        return iqFactory.createUnaryIQTree(this, newChild,
                hasChildChanged.get()
                        ? treeCache.declareAsNormalizedForOptimizationWithEffect()
                        : treeCache.declareAsNormalizedForOptimizationWithoutEffect());
    }

    private static Optional<Integer> getKnownCardinality(IQTree tree) {
        if (tree instanceof TrueNode)
            return Optional.of(1);
        if (tree instanceof ValuesNode)
            return Optional.of(((ValuesNode) tree).getValues().size());

        QueryNode rootNode = tree.getRootNode();
        if (rootNode instanceof ConstructionNode)
            return getKnownCardinality(tree.getChildren().get(0));
        // TODO: shall we consider other nodes, like union nodes?
        return Optional.empty();
    }

    private Optional<IQTree> simplifyUnionWithChildrenOfKnownCardinality(UnionNode childRoot,
                                                                         IQTree childTree, int limit,
                                                                         VariableGenerator variableGenerator) {

        ImmutableList<IQTree> children = childTree.getChildren();

        ImmutableMultimap<IQTree, Integer> cardinalityMultimap = children.stream()
                .flatMap(c -> getKnownCardinality(c).stream()
                        .map(card -> Maps.immutableEntry(c, card)))
                .collect(ImmutableCollectors.toMultimap());

        Optional<Integer> maxChildCardinality = cardinalityMultimap.values().stream().max(Integer::compareTo);

        if (maxChildCardinality.isEmpty())
            return Optional.empty();

        if (maxChildCardinality.get() >= limit) {
            IQTree largestChild = cardinalityMultimap.entries().stream()
                    .filter(e -> e.getValue().equals(maxChildCardinality.get()))
                    .map(Map.Entry::getKey)
                    .findAny()
                    .orElseThrow(() -> new MinorOntopInternalBugException("There should be one child"));

            return Optional.of(iqFactory.createUnaryIQTree(this, largestChild)
                    .normalizeForOptimization(variableGenerator));
        }

        int sum = cardinalityMultimap.values().stream().reduce(Integer::sum)
                .orElseThrow(() -> new MinorOntopInternalBugException("At least one child expected"));

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

        long countChildrenWithUnknownCardinality = children.stream()
                .filter(c -> !cardinalityMultimap.containsKey(c))
                .count();

        if (countChildrenWithUnknownCardinality == 0)
            // No more limit
            return Optional.of(childTree);

        ImmutableList<IQTree> newChildren = children.stream()
                .map(c -> cardinalityMultimap.containsKey(c)
                        ? c
                        : iqFactory.createUnaryIQTree(
                        iqFactory.createSliceNode(0, limit - sum),
                        c))
                .collect(ImmutableCollectors.toList());

        IQTree newUnionTree = iqFactory.createNaryIQTree(childRoot, newChildren)
                .normalizeForOptimization(variableGenerator);

        return (countChildrenWithUnknownCardinality == 1)
                ? Optional.of(newUnionTree)
                : newUnionTree.equals(childTree)
                    ? Optional.empty()
                    : Optional.of(iqFactory.createUnaryIQTree(this, newUnionTree)
                        .normalizeForOptimization(variableGenerator));
    }

    /**
     * TODO: fix it: needs all the values nodes to be distinct!
     * NB: they should normally have been merged together into one values node, so the code can be simplified.
     */
    private IQTree simplifyDistinctUnionValues(IQTree newChild, IQTreeCache treeCache) {
        // Retrieve size of Values Node, first child node is the Distinct node
        long totalValues = newChild.getChildren().get(0).getChildren().stream()
                .filter(c -> c instanceof ValuesNode)
                .map(c -> (ValuesNode) c)
                .map(ValuesNode::getValues)
                .mapToLong(Collection::size)
                .sum();

        ValuesNode vNode = newChild.getChildren().get(0).getChildren().stream()
                .filter(c -> c instanceof ValuesNode)
                .map(IQTree::getRootNode)
                .map(v -> (ValuesNode) v)
                .findFirst().get();

        // If total values is higher than limit, get only Values Node
        // Otherwise do nothing due to the distinct in the pattern, we cannot know if non-values nodes are distinct or not
        // Offset = 0
        return totalValues >= limit
                ? iqFactory.createValuesNode(vNode.getOrderedVariables(), vNode.getValues().subList(0, limit.intValue()))
                : iqFactory.createUnaryIQTree(this, newChild, treeCache.declareAsNormalizedForOptimizationWithEffect());
    }

    // Check if enough Values records are available to simplify Slice
    private long calculateMaxTotalValues(IQTree newChild) {

        long directValuesNodes = newChild.getChildren().stream()
                .filter(c -> c instanceof ValuesNode)
                .map(c -> (ValuesNode) c)
                .map(ValuesNode::getValues)
                .mapToLong(Collection::size)
                .sum();

        long nonDistinctValuesNodes = newChild.getChildren().stream()
                .filter(c -> c.getRootNode() instanceof ConstructionNode)
                .map(IQTree::getChildren)
                .flatMap(Collection::stream)
                .filter(c -> c.getRootNode() instanceof UnionNode)
                .map(IQTree::getChildren)
                .flatMap(Collection::stream)
                .filter(c -> c instanceof ValuesNode)
                .map(c -> (ValuesNode) c)
                .map(ValuesNode::getValues)
                .mapToLong(Collection::size)
                .sum();

        long distinctValuesNodes = newChild.getChildren().stream()
                .filter(c -> c.getRootNode() instanceof ConstructionNode)
                .map(IQTree::getChildren)
                .flatMap(Collection::stream)
                .filter(c -> c.getRootNode() instanceof DistinctNode)
                .map(IQTree::getChildren)
                .flatMap(Collection::stream)
                .filter(c -> c.getRootNode() instanceof UnionNode)
                .map(IQTree::getChildren)
                .flatMap(Collection::stream)
                .filter(c -> c instanceof ValuesNode)
                .map(c -> (ValuesNode) c)
                .map(ValuesNode::getValues)
                .distinct()
                .mapToLong(Collection::size)
                .sum();

        return nonDistinctValuesNodes + distinctValuesNodes + directValuesNodes;
    }

    @Override
    public IQTree applyDescendingSubstitution(ImmutableSubstitution<? extends VariableOrGroundTerm> descendingSubstitution,
                                              Optional<ImmutableExpression> constraint, IQTree child, VariableGenerator variableGenerator) {
        return iqFactory.createUnaryIQTree(this,
                child.applyDescendingSubstitution(descendingSubstitution, constraint, variableGenerator));
    }

    @Override
    public IQTree applyDescendingSubstitutionWithoutOptimizing(
            ImmutableSubstitution<? extends VariableOrGroundTerm> descendingSubstitution, IQTree child, VariableGenerator variableGenerator) {
        return iqFactory.createUnaryIQTree(this,
                child.applyDescendingSubstitutionWithoutOptimizing(descendingSubstitution, variableGenerator));
    }

    @Override
    public IQTree applyFreshRenaming(InjectiveVar2VarSubstitution renamingSubstitution, IQTree child, IQTreeCache treeCache) {
        IQTree newChild = child.applyFreshRenaming(renamingSubstitution);
        IQTreeCache newTreeCache = treeCache.applyFreshRenaming(renamingSubstitution);
        return iqFactory.createUnaryIQTree(this, newChild, newTreeCache);
    }

    @Override
    public boolean isDistinct(IQTree tree, IQTree child) {
        if (limit != null && limit <= 1)
            return true;
        return child.isDistinct();
    }

    @Override
    public IQTree acceptTransformer(IQTree tree, IQTreeVisitingTransformer transformer, IQTree child) {
        return transformer.transformSlice(tree, this, child);
    }

    @Override
    public <T> IQTree acceptTransformer(IQTree tree, IQTreeExtendedTransformer<T> transformer, IQTree child, T context) {
        return transformer.transformSlice(tree, this, child, context);
    }

    @Override
    public <T> T acceptVisitor(IQVisitor<T> visitor, IQTree child) {
        return visitor.visitSlice(this, child);
    }

    @Override
    public void validateNode(IQTree child) throws InvalidIntermediateQueryException {
    }

    @Override
    public IQTree removeDistincts(IQTree child, IQTreeCache treeCache) {
        IQTree newChild = child.removeDistincts();
        IQTreeCache newTreeCache = treeCache.declareDistinctRemoval(newChild.equals(child));
        return iqFactory.createUnaryIQTree(this, newChild, newTreeCache);
    }

    @Override
    public ImmutableSet<ImmutableSet<Variable>> inferUniqueConstraints(IQTree child) {
        return child.inferUniqueConstraints();
    }

    @Override
    public ImmutableSet<Variable> computeNotInternallyRequiredVariables(IQTree child) {
        return child.getNotInternallyRequiredVariables();
    }

    @Override
    public void acceptVisitor(QueryNodeVisitor visitor) {
        visitor.visit(this);
    }

    @Override
    public SliceNode acceptNodeTransformer(HomogeneousQueryNodeTransformer transformer)
            throws QueryNodeTransformationException {
        return transformer.transform(this);
    }

    @Override
    public ImmutableSet<Variable> getLocallyRequiredVariables() {
        return ImmutableSet.of();
    }

    @Override
    public ImmutableSet<Variable> getLocallyDefinedVariables() {
        return ImmutableSet.of();
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        SliceNodeImpl sliceNode = (SliceNodeImpl) o;
        return offset == sliceNode.offset && Objects.equals(limit, sliceNode.limit);
    }

    @Override
    public int hashCode() {
        return Objects.hash(offset, limit);
    }

    @Override
    public ImmutableSet<Variable> getLocalVariables() {
        return ImmutableSet.of();
    }

    @Override
    public long getOffset() {
        return offset;
    }

    @Override
    public Optional<Long> getLimit() {
        return Optional.ofNullable(limit);
    }

    @Override
    public String toString() {
        return SLICE_STR
                + (offset > 0 ? " offset=" + offset : "")
                + (limit == null ? "" : " limit=" + limit);
    }

    /**
     * Stops constraints
     */
    @Override
    public IQTree propagateDownConstraint(ImmutableExpression constraint, IQTree child, VariableGenerator variableGenerator) {
        return iqFactory.createUnaryIQTree(this, child);
    }
}
