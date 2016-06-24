package it.unibz.inf.ontop.pivotalrepr;

import it.unibz.inf.ontop.model.ImmutableExpression;
import it.unibz.inf.ontop.model.ImmutableTerm;
import it.unibz.inf.ontop.model.ImmutableSubstitution;

/**
 * TODO: explain
 */
public interface FilterNode extends CommutativeJoinOrFilterNode {

    @Override
    FilterNode clone();

    @Override
    FilterNode acceptNodeTransformer(HomogeneousQueryNodeTransformer transformer) throws QueryNodeTransformationException;

    /**
     * Not optional for a FilterNode.
     */
    ImmutableExpression getFilterCondition();

    /**
     * Returns a new FilterNode (immutable).
     */
    FilterNode changeFilterCondition(ImmutableExpression newFilterCondition);

    @Override
    SubstitutionResults<FilterNode> applyAscendingSubstitution(
            ImmutableSubstitution<? extends ImmutableTerm> substitution,
            QueryNode descendantNode, IntermediateQuery query);

    @Override
    SubstitutionResults<FilterNode> applyDescendingSubstitution(
            ImmutableSubstitution<? extends ImmutableTerm> substitution, IntermediateQuery query);
}
