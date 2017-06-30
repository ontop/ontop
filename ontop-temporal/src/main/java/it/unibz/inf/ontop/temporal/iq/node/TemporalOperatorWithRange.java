package it.unibz.inf.ontop.temporal.iq.node;

import it.unibz.inf.ontop.temporal.model.TemporalRange;

public interface TemporalOperatorWithRange extends TemporalOperatorNode{

    TemporalRange getRange();

}
