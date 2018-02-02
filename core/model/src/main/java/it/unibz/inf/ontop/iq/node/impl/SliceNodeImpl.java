package it.unibz.inf.ontop.iq.node.impl;

import com.google.common.collect.ImmutableSet;
import com.google.inject.assistedinject.Assisted;
import com.google.inject.assistedinject.AssistedInject;
import it.unibz.inf.ontop.injection.IntermediateQueryFactory;
import it.unibz.inf.ontop.iq.IQProperties;
import it.unibz.inf.ontop.iq.IQTree;
import it.unibz.inf.ontop.iq.IntermediateQuery;
import it.unibz.inf.ontop.iq.exception.InvalidIntermediateQueryException;
import it.unibz.inf.ontop.iq.exception.QueryNodeTransformationException;
import it.unibz.inf.ontop.iq.node.*;
import it.unibz.inf.ontop.iq.transform.IQTransformer;
import it.unibz.inf.ontop.iq.transform.node.HeterogeneousQueryNodeTransformer;
import it.unibz.inf.ontop.iq.transform.node.HomogeneousQueryNodeTransformer;
import it.unibz.inf.ontop.model.term.ImmutableExpression;
import it.unibz.inf.ontop.model.term.Variable;
import it.unibz.inf.ontop.model.term.VariableOrGroundTerm;
import it.unibz.inf.ontop.substitution.ImmutableSubstitution;
import it.unibz.inf.ontop.utils.VariableGenerator;

import javax.annotation.Nullable;
import java.util.Optional;

public class SliceNodeImpl extends QueryModifierNodeImpl implements SliceNode {

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

    /**
     *
     * TODO: should not block substitutions anymore!
     *
     * TODO: refactor
     */
    private IQTree liftBinding(IQTree child, VariableGenerator variableGenerator, IQProperties currentIQProperties) {
        IQTree newChild = child.normalizeForOptimization(variableGenerator);
        QueryNode newChildRoot = newChild.getRootNode();

        if (newChildRoot instanceof SliceNode)
            return liftSliceChild((SliceNode) newChildRoot, newChild, currentIQProperties);
        else if (newChildRoot instanceof EmptyNode)
            return newChild;
        else
            return iqFactory.createUnaryIQTree(this, newChild, currentIQProperties.declareNormalizedForOptimization());
    }

    /**
     * TODO: implement it seriously
     */
    private IQTree liftSliceChild(SliceNode newChildRoot, IQTree newChild, IQProperties currentIQProperties) {
        return iqFactory.createUnaryIQTree(this, newChild, currentIQProperties.declareNormalizedForOptimization());
    }

    @Override
    public IQTree liftIncompatibleDefinitions(Variable variable, IQTree child) {
        throw new RuntimeException("TODO: implement it");
    }

    /**
     * TODO: refactor
     */
    @Override
    public IQTree normalizeForOptimization(IQTree child, VariableGenerator variableGenerator, IQProperties currentIQProperties) {
        return liftBinding(child, variableGenerator, currentIQProperties);
    }

    @Override
    public IQTree applyDescendingSubstitution(ImmutableSubstitution<? extends VariableOrGroundTerm> descendingSubstitution,
                                              Optional<ImmutableExpression> constraint, IQTree child) {
        return iqFactory.createUnaryIQTree(this,
                child.applyDescendingSubstitution(descendingSubstitution, constraint));
    }

    @Override
    public IQTree acceptTransformer(IQTree tree, IQTransformer transformer, IQTree child) {
        return transformer.transformSlice(tree, this, child);
    }

    @Override
    public void validateNode(IQTree child) throws InvalidIntermediateQueryException {
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
    public NodeTransformationProposal acceptNodeTransformer(HeterogeneousQueryNodeTransformer transformer) {
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
}
