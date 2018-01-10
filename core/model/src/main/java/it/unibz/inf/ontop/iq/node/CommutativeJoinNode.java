package it.unibz.inf.ontop.iq.node;

import java.util.Optional;

import it.unibz.inf.ontop.model.term.ImmutableExpression;

/**
 * Commutative: the children order does not matter
 */
public interface CommutativeJoinNode extends JoinLikeNode, CommutativeJoinOrFilterNode, NaryOperatorNode {

    @Override
    CommutativeJoinNode changeOptionalFilterCondition(Optional<ImmutableExpression> newOptionalFilterCondition);

}
