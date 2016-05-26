package it.unibz.inf.ontop.pivotalrepr;

import java.util.Optional;

import it.unibz.inf.ontop.model.ImmutableExpression;
import it.unibz.inf.ontop.model.ImmutableTerm;
import it.unibz.inf.ontop.model.ImmutableSubstitution;

/**
 * TODO: explain
 */
public interface JoinOrFilterNode extends QueryNode {

    Optional<ImmutableExpression> getOptionalFilterCondition();

    @Override
    SubstitutionResults<? extends JoinOrFilterNode> applyAscendingSubstitution(
            ImmutableSubstitution<? extends ImmutableTerm> substitution,
            QueryNode descendantNode, IntermediateQuery query) throws QueryNodeSubstitutionException;

    @Override
    SubstitutionResults<? extends JoinOrFilterNode> applyDescendingSubstitution(
            ImmutableSubstitution<? extends ImmutableTerm> substitution, IntermediateQuery query)
            throws QueryNodeSubstitutionException;

}
