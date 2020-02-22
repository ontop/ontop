package it.unibz.inf.ontop.iq.node.impl;

import com.google.common.collect.ImmutableSet;
import com.google.inject.assistedinject.Assisted;
import com.google.inject.assistedinject.AssistedInject;
import it.unibz.inf.ontop.injection.IntermediateQueryFactory;
import it.unibz.inf.ontop.iq.*;
import it.unibz.inf.ontop.iq.exception.InvalidIntermediateQueryException;
import it.unibz.inf.ontop.iq.exception.QueryNodeTransformationException;
import it.unibz.inf.ontop.iq.node.*;
import it.unibz.inf.ontop.iq.transform.IQTreeVisitingTransformer;
import it.unibz.inf.ontop.iq.transform.node.HomogeneousQueryNodeTransformer;
import it.unibz.inf.ontop.iq.visit.IQVisitor;
import it.unibz.inf.ontop.model.term.ImmutableExpression;
import it.unibz.inf.ontop.model.term.Variable;
import it.unibz.inf.ontop.model.term.VariableOrGroundTerm;
import it.unibz.inf.ontop.substitution.ImmutableSubstitution;
import it.unibz.inf.ontop.substitution.InjectiveVar2VarSubstitution;
import it.unibz.inf.ontop.utils.VariableGenerator;

import javax.annotation.Nullable;
import java.util.Optional;

public class SliceNodeImpl extends QueryModifierNodeImpl implements SliceNode {

    private static final String SLICE_STR = "SLICE";

    private final long offset;

    @Nullable
    private final Long limit;

    @AssistedInject
    private SliceNodeImpl(@Assisted("offset") long offset, @Assisted("limit") long limit, IntermediateQueryFactory iqFactory) {
        super(iqFactory);
        if (offset < 0)
            throw new IllegalArgumentException("The offset must not be negative");
        if (limit < 0)
            throw new IllegalArgumentException("The limit must not be negative");
        this.offset = offset;
        this.limit = limit;
    }

    @AssistedInject
    private SliceNodeImpl(@Assisted long offset, IntermediateQueryFactory iqFactory) {
        super(iqFactory);
        if (offset < 0)
            throw new IllegalArgumentException("The offset must not be negative");
        this.offset = offset;
        this.limit = null;
    }

    @Override
    public IQTree liftIncompatibleDefinitions(Variable variable, IQTree child, VariableGenerator variableGenerator) {
        throw new RuntimeException("TODO: implement it");
    }

    @Override
    public IQTree normalizeForOptimization(IQTree child, VariableGenerator variableGenerator, IQProperties currentIQProperties) {
        if ((limit != null) && limit == 0)
            return iqFactory.createEmptyNode(child.getVariables());

        IQTree newChild = child.normalizeForOptimization(variableGenerator);
        QueryNode newChildRoot = newChild.getRootNode();

        if (newChildRoot instanceof ConstructionNode)
            return liftChildConstruction((ConstructionNode) newChildRoot, (UnaryIQTree)newChild, variableGenerator);
        else if (newChildRoot instanceof SliceNode)
            return mergeWithSliceChild((SliceNode) newChildRoot, newChild, currentIQProperties);
        else if (newChildRoot instanceof EmptyNode)
            return newChild;
        else
            return iqFactory.createUnaryIQTree(this, newChild, currentIQProperties.declareNormalizedForOptimization());
    }

    private IQTree liftChildConstruction(ConstructionNode childConstructionNode, UnaryIQTree childTree,
                                         VariableGenerator variableGenerator) {
        IQTree newSliceLevelTree = iqFactory.createUnaryIQTree(this, childTree.getChild())
                .normalizeForOptimization(variableGenerator);
        return iqFactory.createUnaryIQTree(childConstructionNode, newSliceLevelTree,
                iqFactory.createIQProperties().declareNormalizedForOptimization());
    }

    /**
     * TODO: implement it seriously
     */
    private IQTree mergeWithSliceChild(SliceNode newChildRoot, IQTree newChild, IQProperties currentIQProperties) {
        return iqFactory.createUnaryIQTree(this, newChild, currentIQProperties.declareNormalizedForOptimization());
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
    public boolean isDistinct(IQTree child) {
        return child.isDistinct();
    }

    @Override
    public IQTree acceptTransformer(IQTree tree, IQTreeVisitingTransformer transformer, IQTree child) {
        return transformer.transformSlice(tree, this, child);
    }

    @Override
    public <T> T acceptVisitor(IQVisitor<T> visitor, IQTree child) {
        return visitor.visitSlice(this, child);
    }

    @Override
    public void validateNode(IQTree child) throws InvalidIntermediateQueryException {
    }

    @Override
    public IQTree removeDistincts(IQTree child, IQProperties iqProperties) {
        IQTree newChild = child.removeDistincts();

        IQProperties newProperties = newChild.equals(child)
                ? iqProperties.declareDistinctRemovalWithoutEffect()
                : iqProperties.declareDistinctRemovalWithEffect();

        return iqFactory.createUnaryIQTree(this, newChild, newProperties);
    }

    @Override
    public ImmutableSet<ImmutableSet<Variable>> inferUniqueConstraints(IQTree child) {
        return child.inferUniqueConstraints();
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
    public ImmutableSet<Variable> getRequiredVariables(IntermediateQuery query) {
        return ImmutableSet.of();
    }

    @Override
    public ImmutableSet<Variable> getLocallyDefinedVariables() {
        return ImmutableSet.of();
    }

    @Override
    public boolean isEquivalentTo(QueryNode queryNode) {
        return queryNode instanceof SliceNode
                && ((SliceNode) queryNode).getOffset() == offset
                && ((SliceNode) queryNode).getLimit().equals(getLimit());
    }

    @Override
    public ImmutableSet<Variable> getLocalVariables() {
        return ImmutableSet.of();
    }

    @Override
    public boolean isSyntacticallyEquivalentTo(QueryNode node) {
        return isEquivalentTo(node);
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
    public SliceNode clone() {
        return getLimit()
                .map(l -> iqFactory.createSliceNode(offset, l))
                .orElseGet(() -> iqFactory.createSliceNode(offset));
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
