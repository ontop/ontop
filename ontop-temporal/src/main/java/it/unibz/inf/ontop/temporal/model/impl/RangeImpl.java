package it.unibz.inf.ontop.temporal.model.impl;

import it.unibz.inf.ontop.temporal.model.Range;

import java.time.Duration;

public class RangeImpl implements Range {


    @Override
    public boolean getBeginInclusive() {
        return false;
    }

    @Override
    public boolean getEndInclusive() {
        return false;
    }

    @Override
    public Duration getBegin() {
        return null;
    }

    @Override
    public Duration getEnd() {
        return null;
    }
}
