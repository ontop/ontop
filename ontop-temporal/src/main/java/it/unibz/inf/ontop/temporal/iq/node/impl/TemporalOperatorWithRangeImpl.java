package it.unibz.inf.ontop.temporal.iq.node.impl;

import it.unibz.inf.ontop.iq.node.QueryNode;
import it.unibz.inf.ontop.temporal.iq.node.TemporalOperatorWithRange;
import it.unibz.inf.ontop.temporal.model.TemporalRange;

public abstract class TemporalOperatorWithRangeImpl implements TemporalOperatorWithRange {
    private TemporalRange temporalRange;

    public TemporalOperatorWithRangeImpl(TemporalRange temporalRange) {
        this.temporalRange = temporalRange;
    }

    @Override
    public TemporalRange getRange() {
        return temporalRange;
    }

    @Override
    public abstract QueryNode clone();
}
