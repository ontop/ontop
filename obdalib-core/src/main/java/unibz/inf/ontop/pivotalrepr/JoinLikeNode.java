package unibz.inf.ontop.pivotalrepr;

import java.util.Optional;

import unibz.inf.ontop.model.ImmutableBooleanExpression;
import unibz.inf.ontop.model.VariableOrGroundTerm;
import unibz.inf.ontop.model.ImmutableSubstitution;

/**
 * Any kind of JOIN (inner, left join)
 */
public interface JoinLikeNode extends JoinOrFilterNode {
    /**
     * Constructs a new JoinLikeNode with the following optional filter condition
     * (recall that a QueryNode is immutable).
     */
    JoinLikeNode changeOptionalFilterCondition(Optional<ImmutableBooleanExpression> newOptionalFilterCondition);

    @Override
    SubstitutionResults<? extends JoinLikeNode> applyAscendentSubstitution(
            ImmutableSubstitution<? extends VariableOrGroundTerm> substitution,
            QueryNode descendantNode, IntermediateQuery query) throws QueryNodeSubstitutionException;

    @Override
    SubstitutionResults<? extends JoinLikeNode> applyDescendentSubstitution(
            ImmutableSubstitution<? extends VariableOrGroundTerm> substitution) throws QueryNodeSubstitutionException;
}
