package it.unibz.inf.ontop.temporal.iq.node;

import it.unibz.inf.ontop.model.ImmutableExpression;
import it.unibz.inf.ontop.model.ImmutableSubstitution;
import it.unibz.inf.ontop.model.ImmutableTerm;
import it.unibz.inf.ontop.model.Term;
import it.unibz.inf.ontop.model.Variable;
import it.unibz.inf.ontop.pivotalrepr.CommutativeJoinNode;
import it.unibz.inf.ontop.pivotalrepr.InnerJoinNode;

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
