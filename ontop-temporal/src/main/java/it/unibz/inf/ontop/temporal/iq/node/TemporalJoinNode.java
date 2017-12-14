package it.unibz.inf.ontop.temporal.iq.node;

import it.unibz.inf.ontop.iq.node.CommutativeJoinNode;
import it.unibz.inf.ontop.model.term.ImmutableExpression;

import java.util.Optional;

public interface TemporalJoinNode extends NaryTemporalOperatorNode, CommutativeJoinNode {

    @Override
    TemporalJoinNode changeOptionalFilterCondition(Optional<ImmutableExpression> newOptionalFilterCondition);
}
