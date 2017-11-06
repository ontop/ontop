package it.unibz.inf.ontop.temporal.mapping.impl;

import it.unibz.inf.ontop.model.term.Variable;
import it.unibz.inf.ontop.temporal.mapping.TemporalMappingInterval;

public class TemporalMappingIntervalImpl implements TemporalMappingInterval {

    private boolean beginInclusive;
    private boolean endInclusive;

    private Variable begin;
    private Variable end;

    @Override
    public boolean isBeginInclusive() {
        return beginInclusive;
    }

    @Override
    public boolean isEndInclusive() {
        return endInclusive;
    }

    @Override
    public String isBeginInclusiveToString() {
        return String.valueOf(beginInclusive);
    }

    @Override
    public String isEndInclusiveToString() {
        return String.valueOf(endInclusive);
    }

    @Override
    public Variable getBegin() {
        return begin;
    }

    @Override
    public Variable getEnd() {
        return end;
    }

    @Override
    public String toString() {
        return (beginInclusive ? "[" : "(") + begin + "," + end + (endInclusive ? "]" : ")");

    }

    public TemporalMappingIntervalImpl(boolean beginInclusive, boolean endInclusive, Variable begin, Variable end) {
        this.beginInclusive = beginInclusive;
        this.endInclusive = endInclusive;
        this.begin = begin;
        this.end = end;
    }

}
