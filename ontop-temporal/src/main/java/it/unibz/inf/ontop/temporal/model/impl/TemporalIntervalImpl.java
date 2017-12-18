package it.unibz.inf.ontop.temporal.model.impl;

import it.unibz.inf.ontop.temporal.model.TemporalInterval;

import java.time.Instant;

public class TemporalIntervalImpl implements TemporalInterval {

    private boolean beginInclusive;
    private boolean endInclusive;

    private Instant begin;
    private Instant end;

    @Override
    public boolean isBeginInclusive() {
        return beginInclusive;
    }

    @Override
    public boolean isEndInclusive() {
        return endInclusive;
    }

    @Override
    public Instant getBegin() {
        return begin;
    }

    @Override
    public Instant getEnd() {
        return end;
    }

    @Override
    public String toString() {
        return (beginInclusive ? "[" : "(") + begin + "," + end + (endInclusive ? "]" : ")");

    }

    public TemporalIntervalImpl(boolean beginInclusive, boolean endInclusive, Instant begin, Instant end) {
        this.beginInclusive = beginInclusive;
        this.endInclusive = endInclusive;
        this.begin = begin;
        this.end = end;
    }
}
