package it.unibz.inf.ontop.iq.node;

import java.util.Optional;

import it.unibz.inf.ontop.model.term.ImmutableExpression;

/**
 * Commutative: the order of children does not matter
 */
public interface CommutativeJoinNode extends JoinLikeNode, CommutativeJoinOrFilterNode, NaryOperatorNode {

}
