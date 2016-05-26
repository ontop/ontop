package it.unibz.inf.ontop.pivotalrepr;

import it.unibz.inf.ontop.model.ImmutableSubstitution;
import it.unibz.inf.ontop.model.ImmutableTerm;

/**
 * Union of CommutativeJoinNode and FilterNode
 */
public interface CommutativeJoinOrFilterNode extends JoinOrFilterNode {

    @Override
    SubstitutionResults<? extends CommutativeJoinOrFilterNode> applyAscendingSubstitution(
            ImmutableSubstitution<? extends ImmutableTerm> substitution,
            QueryNode descendantNode, IntermediateQuery query) throws QueryNodeSubstitutionException;

    @Override
    SubstitutionResults<? extends CommutativeJoinOrFilterNode> applyDescendingSubstitution(
            ImmutableSubstitution<? extends ImmutableTerm> substitution, IntermediateQuery query)
            throws QueryNodeSubstitutionException;

}
