package it.unibz.inf.ontop.temporal.model.impl;

import it.unibz.inf.ontop.temporal.model.TemporalRange;

import java.time.Duration;

public class TemporalRangeImpl implements TemporalRange {

    private Boolean beginInclusive;
    private Boolean endInclusive;

    private Duration begin;
    private Duration end;

    @Override
    public Boolean isBeginInclusive() {
        return beginInclusive;
    }

    @Override
    public Boolean isEndInclusive() {
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

    //@Inject
    public TemporalRangeImpl(Boolean beginInclusive, Boolean endInclusive, Duration begin, Duration end) {
        this.beginInclusive = beginInclusive;
        this.endInclusive = endInclusive;
        this.begin = begin;
        this.end = end;
    }

    @Override
    public boolean equals(TemporalRange temporalRange){

        if(this.isBeginInclusive().equals(temporalRange.isBeginInclusive()) &&
                this.isEndInclusive().equals(temporalRange.isEndInclusive()) &&
                this.getBegin().equals(temporalRange.getBegin()) &&
                this.getEnd().equals(temporalRange.getEnd())){

            return true;
        }

        return false;
    }
}
