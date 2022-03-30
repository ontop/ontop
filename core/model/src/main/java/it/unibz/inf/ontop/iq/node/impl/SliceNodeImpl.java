package it.unibz.inf.ontop.iq.node.impl;

import com.google.common.collect.ImmutableList;
import com.google.common.collect.ImmutableMap;
import com.google.common.collect.ImmutableSet;
import com.google.inject.assistedinject.Assisted;
import com.google.inject.assistedinject.AssistedInject;
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
        QueryNode newChildRoot = newChild.getRootNode();

        if (newChildRoot instanceof ConstructionNode)
            return liftChildConstruction((ConstructionNode) newChildRoot, (UnaryIQTree)newChild, variableGenerator);
        else if (newChildRoot instanceof SliceNode)
            return mergeWithSliceChild((SliceNode) newChildRoot, (UnaryIQTree) newChild, treeCache);
        else if (newChildRoot instanceof EmptyNode)
            return newChild;
        else if ((newChildRoot instanceof TrueNode)
                || ((newChildRoot instanceof AggregationNode)
                    && ((AggregationNode) newChildRoot).getGroupingVariables().isEmpty()))
            return offset > 0
                    ? iqFactory.createEmptyNode(child.getVariables())
                    : newChild;
        else if ((newChildRoot instanceof DistinctNode)
                && (offset == 0)
                && getLimit()
                    .filter(l -> l <= 1)
                    .isPresent())
            // Distinct can be eliminated
            return normalizeForOptimization(((UnaryIQTree) newChild).getChild(), variableGenerator, treeCache);
        // Condition - For offset > 0, we create the option to activate or deactivate any optimizations
        else if ((newChildRoot instanceof ValuesNode) && (offset == 0 || !settings.isSliceOptimizationDisabled()))
            return iqFactory.createValuesNode(((ValuesNode) newChildRoot).getOrderedVariables(),
                    ((ValuesNode) newChildRoot).getValues().subList((int) offset, (int) (offset+limit)));
        // Only triggered if a VALUES node is present directly under the UNION
        else if ((newChildRoot instanceof UnionNode)
                && (offset == 0 || !settings.isSliceOptimizationDisabled())
                && newChild.getChildren().stream().anyMatch(c -> c instanceof ValuesNode)
                && !treeCache.isNormalizedForOptimization()
        )
            return simplifyUnionValues((UnionNode) newChildRoot, newChild, treeCache);
        // Scenario: SLICE UNION [CONSTRUCT] [DISTINCT] UNION [VALUES NONVALUES] pattern scenario
        else if ((newChildRoot instanceof UnionNode)
                && (offset == 0 || !settings.isSliceOptimizationDisabled())
                && calculateMaxTotalValues(newChild) >= (offset + limit)
                && !treeCache.isNormalizedForOptimization())
            return simplifyConstructUnionValues((UnionNode) newChildRoot, newChild, treeCache);
        // Scenario: LIMIT DISTINCT UNION [T1 ...] -> LIMIT DISTINCT UNION [LIMIT T1 ...] if T1 is distinct
        else if ((newChildRoot instanceof DistinctNode) &&
                (offset == 0 || !settings.isSliceOptimizationDisabled()) &&
                newChild.getChildren().size() == 1 &&
                newChild.getChildren().stream().allMatch(c -> c.getRootNode() instanceof UnionNode) &&
                newChild.getChildren().get(0).getChildren().stream().anyMatch(c -> c instanceof ValuesNode) &&
                newChild.getChildren().get(0).getChildren().stream().filter(c -> c instanceof ValuesNode).anyMatch(IQTree::isDistinct)) {

            // Retrieve size of Values Node
            long totalValues = newChild.getChildren().get(0).getChildren().stream()
                    .filter(c -> c instanceof ValuesNode)
                    .map(c -> (ValuesNode) c)
                    .map(ValuesNode::getValues)
                    .mapToLong(Collection::size)
                    .sum();

            // If total values is higher than limit, get only Values Node
            // Otherwise do nothing due to the distinct in the pattern, we cannot know if non-values nodes are distinct or not
            ValuesNode vNode = newChild.getChildren().get(0).getChildren().stream()
                    .filter(c -> c instanceof ValuesNode)
                    .map(IQTree::getRootNode)
                    .map(v -> (ValuesNode) v)
                    .findFirst().get();

            return totalValues > (offset + limit)
                    ? iqFactory.createValuesNode(vNode.getOrderedVariables(), vNode.getValues().subList((int) offset, (int) (offset+limit)))
                    : iqFactory.createUnaryIQTree(this, newChild, treeCache.declareAsNormalizedForOptimizationWithEffect());
        }
        else
            return iqFactory.createUnaryIQTree(this, newChild, treeCache.declareAsNormalizedForOptimizationWithEffect());
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

    private IQTree simplifyUnionValues(UnionNode newChildRoot, IQTree newChild, IQTreeCache treeCache) {

        // Retrieve size of Values Node
        long totalValues = newChild.getChildren().stream()
                .filter(c -> c instanceof ValuesNode)
                .map(c -> (ValuesNode) c)
                .map(ValuesNode::getValues)
                .mapToLong(Collection::size)
                .sum();

        /*
          Only one Values Node should be present at this optimization step
          @see it.unibz.inf.ontop.iq.node.impl.UnionNodeImpl#liftBindingFromLiftedChildrenAndFlatten(ImmutableList, VariableGenerator, IQTreeCache)
         */
        ValuesNode vNode = newChild.getChildren().stream()
                .filter(c -> c instanceof ValuesNode)
                .map(IQTree::getRootNode)
                .map(v -> (ValuesNode) v)
                .findFirst().get();

        ImmutableList<IQTree> nonVnodeList = newChild.getChildren().stream()
                .filter(c -> !(c instanceof ValuesNode))
                .collect(ImmutableCollectors.toList());

        // Retrieve the Values Node variables - we know at least one Values Node is present
        ImmutableList<Variable> orderedVariables = newChild.getChildren().stream()
                .filter(c -> c instanceof ValuesNode)
                .map(c -> ((ValuesNode) c))
                .map(ValuesNode::getOrderedVariables)
                .findFirst().get();

        // CASE 1: Values Node does not even cover offset - No optimization
        if (offset >= totalValues) {
            return iqFactory.createUnaryIQTree(this, newChild, treeCache.declareAsNormalizedForOptimizationWithEffect());
        // CASE 2: Values Node fully covers offset and/or limit - Drop other non-Values Node
        } else if (((offset + limit)) <= totalValues) {
            return iqFactory.createValuesNode(orderedVariables,
                vNode.getValues().subList((int) offset, (int) (offset + limit)));
        // CASE 3: Values Node does not fully cover offset and/or limit - Drop slice from values node, keep for nonvalues
        } else {

            ImmutableList<ValuesNode> filteredValuesNodeList = ImmutableList.of(
                    iqFactory.createValuesNode(orderedVariables,
                    vNode.getValues().subList((int) offset, (int) (Math.min(offset + limit, totalValues)))));

            return iqFactory.createNaryIQTree(
                newChildRoot,
                Stream.concat(filteredValuesNodeList.stream(),
                        ImmutableList.of(iqFactory.createUnaryIQTree(
                                // New slice node with remaining limit
                                iqFactory.createSliceNode(Math.max(0, offset - totalValues), limit - (totalValues - offset)),
                                nonVnodeList.size() > 1
                                    // CASE 3.1 - Multiple non-Values nodes - keep Union
                                    ? iqFactory.createNaryIQTree(newChildRoot,nonVnodeList)
                                    // CASE 3.2 - Single non-Values node - drop Union
                                    : nonVnodeList.get(0)
                                )).stream())
                        .collect(ImmutableCollectors.toList()));
        }
    }

    private IQTree simplifyConstructUnionValues(UnionNode newChildRoot, IQTree newChild, IQTreeCache treeCache) {

        // PATTERN 0: CONSTRUCT VALUES
        ImmutableMap<QueryNode, Optional<ValuesNode>> treePattern0 = newChild.getChildren().stream()
                .filter(c -> c.getRootNode() instanceof ConstructionNode)
                .collect(ImmutableCollectors.toMap(
                        IQTree::getRootNode,
                        c -> c.getChildren().stream()
                                .filter(c3 -> c3 instanceof ValuesNode)
                                .map(c4 -> (ValuesNode) c4).findFirst()));

        // PATTERN 1: CONSTRUCT UNION VALUES
        ImmutableMap<QueryNode, Optional<ValuesNode>> treePattern1 = newChild.getChildren().stream()
                .filter(c -> c.getRootNode() instanceof ConstructionNode)
                .collect(ImmutableCollectors.toMap(
                        IQTree::getRootNode,
                        c -> c.getChildren().stream()
                                .filter(c2 -> c2.getRootNode() instanceof UnionNode)
                                .map(IQTree::getChildren)
                                .flatMap(Collection::stream)
                                .filter(c3 -> c3 instanceof ValuesNode)
                                .map(c4 -> (ValuesNode) c4)
                                .findFirst()));

        // PATTERN 2: CONSTRUCT DISTINCT UNION VALUES
        ImmutableMap<QueryNode, Optional<ValuesNode>> treePattern2 = newChild.getChildren().stream()
                .filter(c -> c.getRootNode() instanceof ConstructionNode)
                .collect(ImmutableCollectors.toMap(
                        IQTree::getRootNode,
                        c -> c.getChildren().stream()
                                .filter(c5 -> c5.getRootNode() instanceof DistinctNode)
                                .map(IQTree::getChildren)
                                .flatMap(Collection::stream)
                                .filter(c2 -> c2.getRootNode() instanceof UnionNode)
                                .map(IQTree::getChildren)
                                .flatMap(Collection::stream)
                                .filter(c3 -> c3 instanceof ValuesNode)
                                .map(c4 -> (ValuesNode) c4)
                                .map(v -> iqFactory.createValuesNode(v.getOrderedVariables(),
                                        v.getValues().stream().distinct().collect(ImmutableCollectors.toList())))
                                .findFirst()));

        // Drop CONSTRUCT without VALUES
        ImmutableMap<QueryNode, Optional<ValuesNode>> allValuesNodes =
                Stream.concat(treePattern0.entrySet().stream(),
                        Stream.concat(treePattern1.entrySet().stream(),
                        treePattern2.entrySet().stream()))
                .filter(c -> c.getValue().isPresent())
                .collect(ImmutableCollectors.toMap(
                        Map.Entry::getKey,
                        Map.Entry::getValue
                ));

        // Final CONSTRUCT VALUES IQtrees - Construct needed to keep track of all projected variables
        ImmutableList<IQTree> vNodesList = allValuesNodes.entrySet().stream()
                .map(v -> iqFactory.createUnaryIQTree((ConstructionNode) v.getKey(), v.getValue().get()))
                        .collect(ImmutableCollectors.toList());

        // Final pattern - SLICE UNION VALUES
        return iqFactory.createUnaryIQTree(iqFactory.createSliceNode(offset, limit),
                iqFactory.createNaryIQTree(newChildRoot, vNodesList)
        );
    }

    // Check if enough Values records are availale to simplify Slice
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
                                              Optional<ImmutableExpression> constraint, IQTree child) {
        return iqFactory.createUnaryIQTree(this,
                child.applyDescendingSubstitution(descendingSubstitution, constraint));
    }

    @Override
    public IQTree applyDescendingSubstitutionWithoutOptimizing(
            ImmutableSubstitution<? extends VariableOrGroundTerm> descendingSubstitution, IQTree child) {
        return iqFactory.createUnaryIQTree(this,
                child.applyDescendingSubstitutionWithoutOptimizing(descendingSubstitution));
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
    public IQTree propagateDownConstraint(ImmutableExpression constraint, IQTree child) {
        return iqFactory.createUnaryIQTree(this, child);
    }
}
