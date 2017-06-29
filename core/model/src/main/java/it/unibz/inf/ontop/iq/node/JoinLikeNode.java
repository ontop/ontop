package it.unibz.inf.ontop.iq.node;

import java.util.Optional;

import it.unibz.inf.ontop.iq.IntermediateQuery;
import it.unibz.inf.ontop.iq.exception.QueryNodeSubstitutionException;
import it.unibz.inf.ontop.model.term.ImmutableExpression;
import it.unibz.inf.ontop.model.term.ImmutableTerm;
import it.unibz.inf.ontop.substitution.ImmutableSubstitution;

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
            QueryNode childNode, IntermediateQuery query) throws QueryNodeSubstitutionException;

    @Override
    SubstitutionResults<? extends JoinLikeNode> applyDescendingSubstitution(
            ImmutableSubstitution<? extends ImmutableTerm> substitution, IntermediateQuery query)
            throws QueryNodeSubstitutionException;
}
