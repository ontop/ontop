package it.unibz.inf.ontop.temporal.model.impl;

import it.unibz.inf.ontop.temporal.model.TemporalRange;

import java.time.Duration;

public class TemporalRangeImpl implements TemporalRange {

    private boolean beginInclusive;
    private boolean endInclusive;

    private Duration begin;
    private Duration end;

    @Override
    public boolean isBeginInclusive() {
        return beginInclusive;
    }

    @Override
    public boolean isEndInclusive() {
        return endInclusive;
    }

    @Override
    public Duration getBegin() {
        return begin;
    }

    @Override
    public Duration getEnd() {
        return end;
    }

    @Override
    public String toString() {
        return (beginInclusive ? "[" : "(") + begin + "," + end + (endInclusive ? "]" : ")");

    }

    public TemporalRangeImpl(boolean beginInclusive, boolean endInclusive, Duration begin, Duration end) {
        this.beginInclusive = beginInclusive;
        this.endInclusive = endInclusive;
        this.begin = begin;
        this.end = end;
    }
}
