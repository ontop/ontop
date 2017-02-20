package it.unibz.inf.ontop.temporal.model.impl;

import it.unibz.inf.ontop.temporal.model.Range;

import java.time.Duration;

public class RangeImpl implements Range {

    private boolean beginInclusive;
    private boolean endInclusive;

    private Duration begin;
    private Duration end;

    @Override
    public boolean getBeginInclusive() {
        return beginInclusive;
    }

    @Override
    public boolean getEndInclusive() {
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

    public RangeImpl(boolean beginInclusive, boolean endInclusive, Duration begin, Duration end) {
        this.beginInclusive = beginInclusive;
        this.endInclusive = endInclusive;
        this.begin = begin;
        this.end = end;
    }
}
