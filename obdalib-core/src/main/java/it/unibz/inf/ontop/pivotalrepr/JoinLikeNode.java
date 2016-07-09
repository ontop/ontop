package it.unibz.inf.ontop.pivotalrepr;

import java.util.Optional;

import it.unibz.inf.ontop.model.ImmutableExpression;
import it.unibz.inf.ontop.model.ImmutableTerm;
import it.unibz.inf.ontop.model.ImmutableSubstitution;

/**
 * Any kind of JOIN (inner, left join)
 */
public interface JoinLikeNode extends JoinOrFilterNode {
    /**
     * Constructs a new JoinLikeNode with the following optional filter condition
     * (recall that a QueryNode is immutable).
     */
    JoinLikeNode changeOptionalFilterCondition(Optional<ImmutableExpression> newOptionalFilterCondition);

    @Override
    SubstitutionResults<? extends JoinLikeNode> applyAscendingSubstitution(
            ImmutableSubstitution<? extends ImmutableTerm> substitution,
            QueryNode descendantNode, IntermediateQuery query) throws QueryNodeSubstitutionException;

    @Override
    SubstitutionResults<? extends JoinLikeNode> applyDescendingSubstitution(
            ImmutableSubstitution<? extends ImmutableTerm> substitution, IntermediateQuery query)
            throws QueryNodeSubstitutionException;
}
