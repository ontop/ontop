package it.unibz.inf.ontop.temporal.iq.node;


import it.unibz.inf.ontop.iq.node.InnerJoinNode;
import it.unibz.inf.ontop.model.term.ImmutableExpression;

import java.util.List;
import java.util.Optional;

public interface TemporalOperatorNode extends InnerJoinNode {

    /**
     * non-temporal join condition
     *
     */
    @Override
    InnerJoinNode changeOptionalFilterCondition(Optional<ImmutableExpression> newOptionalFilterCondition);


    List<IntervalColumnsProjection> getInputIntervalColumnsProjections();

    IntervalColumnsProjection getInputIntervalColumnsProjection();
}
