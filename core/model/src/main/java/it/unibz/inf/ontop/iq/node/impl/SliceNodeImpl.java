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
import it.unibz.inf.ontop.iq.request.FunctionalDependencies;
import it.unibz.inf.ontop.iq.request.VariableNonRequirement;
import it.unibz.inf.ontop.iq.transform.IQTreeExtendedTransformer;
import it.unibz.inf.ontop.iq.transform.IQTreeVisitingTransformer;
import it.unibz.inf.ontop.iq.transform.node.HomogeneousQueryNodeTransformer;
import it.unibz.inf.ontop.iq.visit.IQVisitor;
import it.unibz.inf.ontop.model.term.Constant;
import it.unibz.inf.ontop.model.term.ImmutableExpression;
import it.unibz.inf.ontop.model.term.Variable;
import it.unibz.inf.ontop.model.term.VariableOrGroundTerm;
import it.unibz.inf.ontop.substitution.Substitution;
import it.unibz.inf.ontop.substitution.InjectiveSubstitution;
import it.unibz.inf.ontop.utils.ImmutableCollectors;
import it.unibz.inf.ontop.utils.VariableGenerator;

import javax.annotation.Nullable;
import java.util.*;
import java.util.function.Supplier;

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
        if (newChildRoot instanceof ValuesNode) {
            ValuesNode valuesNode = (ValuesNode) newChildRoot;
            ImmutableList<ImmutableList<Constant>> values = valuesNode.getValues();
            if (values.size() <= offset)
                return iqFactory.createEmptyNode(valuesNode.getVariables());

            return iqFactory.createValuesNode(
                    valuesNode.getOrderedVariables(),
                    // TODO: complain if the offset or the limit are too big to be casted as integers
                    values.subList((int)offset,
                            Integer.min(getLimit().map(l -> l + offset).orElse(offset).intValue(), values.size())))
                    .normalizeForOptimization(variableGenerator);
        }

        // Limit optimizations will apply under the following conditions
        // Rule 1: For offset = 0,
        // Rule 2: Limit optimizations are not disabled
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
                .or(this::getLimit);

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
            // Only triggered if a child with a known cardinality is present directly under the UNION
        if (newChildRoot instanceof UnionNode) {
            UnionNode unionNode = (UnionNode) newChildRoot;

            Optional<IQTree> newTree = newChild.getChildren().stream().anyMatch(c -> getKnownCardinality(c).isPresent())
                    ? simplifyUnionWithChildrenOfKnownCardinality(unionNode, newChild, limit, variableGenerator)
                    : pushLimitInUnionChildren(unionNode, newChild, variableGenerator);
            if (newTree.isPresent())
                return newTree.get();
        }
        else if (newChildRoot instanceof DistinctNode) {
            if (limit <= 1)
                // Distinct can be eliminated
                return normalizeForOptimization(((UnaryIQTree) newChild).getChild(), variableGenerator, treeCache,
                        () -> true);

            IQTree childOfDistinct = newChild.getChildren().get(0);

            if ((childOfDistinct.getRootNode() instanceof UnionNode)
                    // If any subtree Ti is distinct we proceed with the optimization
                    && childOfDistinct.getChildren().stream().anyMatch(IQTree::isDistinct)) {

                Optional<IQTree> newTree = simplifyDistinctUnionWithDistinctChildren(
                        childOfDistinct, limit, variableGenerator);

                if (newTree.isPresent())
                    return newTree.get();
            }
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

    private Optional<IQTree> pushLimitInUnionChildren(UnionNode unionNode, IQTree unionTree, VariableGenerator variableGenerator) {
        ImmutableList<IQTree> newUnionChildren = unionTree.getChildren().stream()
                .map(c -> iqFactory.createUnaryIQTree(this, c))
                .map(c -> c.normalizeForOptimization(variableGenerator))
                .collect(ImmutableCollectors.toList());

        return unionTree.getChildren().equals(newUnionChildren)
                ? Optional.empty()
                : Optional.of(iqFactory.createUnaryIQTree(
                        this,
                        iqFactory.createNaryIQTree(unionNode, newUnionChildren)));
    }

    private Optional<IQTree> simplifyDistinctUnionWithDistinctChildren(IQTree unionTree, int limit, VariableGenerator variableGenerator) {
        ImmutableList<IQTree> unionChildren = unionTree.getChildren();

        Optional<IQTree> sufficientChild = unionChildren.stream()
                .filter(IQTree::isDistinct)
                .filter(c -> getKnownCardinality(c).filter(card -> card >= limit)
                        .isPresent())
                .findAny();

        if (sufficientChild.isPresent())
            // Eliminates the distinct and the union
            return Optional.of(iqFactory.createUnaryIQTree(this, sufficientChild.get())
                    .normalizeForOptimization(variableGenerator));

        // Scenario: LIMIT DISTINCT UNION [T1 ...] -> LIMIT DISTINCT UNION [LIMIT T1 ...] if T1 is distinct
        ImmutableList<IQTree> newUnionChildren = unionChildren.stream()
                .map(c -> c.isDistinct()
                        ? iqFactory.createUnaryIQTree(this, c)
                        : c)
                .map(c -> c.normalizeForOptimization(variableGenerator))
                .collect(ImmutableCollectors.toList());

        return newUnionChildren.equals(unionChildren)
                ? Optional.empty()
                : Optional.of(
                        iqFactory.createUnaryIQTree(
                                this,
                                iqFactory.createUnaryIQTree(
                                        iqFactory.createDistinctNode(),
                                        iqFactory.createNaryIQTree(
                                                (UnionNode) unionTree.getRootNode(),
                                                newUnionChildren)))
                                .normalizeForOptimization(variableGenerator));
    }

    @Override
    public IQTree applyDescendingSubstitution(Substitution<? extends VariableOrGroundTerm> descendingSubstitution,
                                              Optional<ImmutableExpression> constraint, IQTree child, VariableGenerator variableGenerator) {
        return iqFactory.createUnaryIQTree(this,
                child.applyDescendingSubstitution(descendingSubstitution, constraint, variableGenerator));
    }

    @Override
    public IQTree applyDescendingSubstitutionWithoutOptimizing(
            Substitution<? extends VariableOrGroundTerm> descendingSubstitution, IQTree child, VariableGenerator variableGenerator) {
        return iqFactory.createUnaryIQTree(this,
                child.applyDescendingSubstitutionWithoutOptimizing(descendingSubstitution, variableGenerator));
    }

    @Override
    public IQTree applyFreshRenaming(InjectiveSubstitution<Variable> renamingSubstitution, IQTree child, IQTreeCache treeCache) {
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
    public FunctionalDependencies inferFunctionalDependencies(IQTree child, ImmutableSet<ImmutableSet<Variable>> uniqueConstraints, ImmutableSet<Variable> variables) {
        return child.inferFunctionalDependencies();
    }

    @Override
    public VariableNonRequirement computeVariableNonRequirement(IQTree child) {
        return child.getVariableNonRequirement();
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
