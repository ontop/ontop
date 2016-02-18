package unibz.inf.ontop.pivotalrepr;

import unibz.inf.ontop.model.ImmutableBooleanExpression;
import unibz.inf.ontop.model.VariableOrGroundTerm;
import unibz.inf.ontop.model.ImmutableSubstitution;

/**
 * TODO: explain
 */
public interface FilterNode extends JoinOrFilterNode {

    @Override
    FilterNode clone();

    @Override
    FilterNode acceptNodeTransformer(HomogeneousQueryNodeTransformer transformer) throws QueryNodeTransformationException;

    /**
     * Not optional for a FilterNode.
     */
    ImmutableBooleanExpression getFilterCondition();

    /**
     * Returns a new FilterNode (immutable).
     */
    FilterNode changeFilterCondition(ImmutableBooleanExpression newFilterCondition);

    @Override
    SubstitutionResults<FilterNode> applyAscendentSubstitution(
            ImmutableSubstitution<? extends VariableOrGroundTerm> substitution,
            QueryNode descendantNode, IntermediateQuery query);

    @Override
    SubstitutionResults<FilterNode> applyDescendentSubstitution(
            ImmutableSubstitution<? extends VariableOrGroundTerm> substitution);
}
