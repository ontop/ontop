package it.unibz.inf.ontop.pivotalrepr.impl;


import java.util.Optional;
import it.unibz.inf.ontop.model.ImmutableBooleanExpression;
import it.unibz.inf.ontop.model.ImmutableSubstitution;
import it.unibz.inf.ontop.model.VariableOrGroundTerm;
import it.unibz.inf.ontop.pivotalrepr.*;
import it.unibz.inf.ontop.pivotalrepr.*;

public class FilterNodeImpl extends JoinOrFilterNodeImpl implements FilterNode {

    private static final String FILTER_NODE_STR = "FILTER";

    public FilterNodeImpl(ImmutableBooleanExpression filterCondition) {
        super(Optional.of(filterCondition));
    }

    @Override
    public void acceptVisitor(QueryNodeVisitor visitor) {
        visitor.visit(this);
    }

    @Override
    public FilterNode clone() {
        return new FilterNodeImpl(getOptionalFilterCondition().get());
    }

    @Override
    public FilterNode acceptNodeTransformer(HomogeneousQueryNodeTransformer transformer) throws QueryNodeTransformationException {
        return transformer.transform(this);
    }

    @Override
    public NodeTransformationProposal acceptNodeTransformer(HeterogeneousQueryNodeTransformer transformer) {
        return transformer.transform(this);
    }

    @Override
    public ImmutableBooleanExpression getFilterCondition() {
        return getOptionalFilterCondition().get();
    }

    @Override
    public FilterNode changeFilterCondition(ImmutableBooleanExpression newFilterCondition) {
        return new FilterNodeImpl(newFilterCondition);
    }

    @Override
    public SubstitutionResults<FilterNode> applyAscendentSubstitution(
            ImmutableSubstitution<? extends VariableOrGroundTerm> substitution,
            QueryNode descendantNode, IntermediateQuery query) {
        return applyDescendentSubstitution(substitution);
    }

    @Override
    public SubstitutionResults<FilterNode> applyDescendentSubstitution(
            ImmutableSubstitution<? extends VariableOrGroundTerm> substitution) {
        ImmutableBooleanExpression newFilterCondition = transformBooleanExpression(substitution, getFilterCondition());
        FilterNode newNode = new FilterNodeImpl(newFilterCondition);

        return new SubstitutionResultsImpl<>(newNode, substitution);
    }

    @Override
    public String toString() {
        return FILTER_NODE_STR + getOptionalFilterString();
    }
}
