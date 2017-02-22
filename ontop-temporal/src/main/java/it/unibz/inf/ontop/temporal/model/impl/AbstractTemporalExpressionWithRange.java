package it.unibz.inf.ontop.temporal.model.impl;

import it.unibz.inf.ontop.temporal.model.TemporalExpression;
import it.unibz.inf.ontop.temporal.model.TemporalExpressionWithRange;
import it.unibz.inf.ontop.temporal.model.TemporalRange;

public abstract class AbstractTemporalExpressionWithRange implements TemporalExpressionWithRange{

    TemporalRange range;

    AbstractTemporalExpressionWithRange(TemporalRange range) {
        this.range = range;
    }

    @Override
    public TemporalRange getRange() {
        return range;
    }
}
