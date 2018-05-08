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
        return (beginInclusive ? "[" : "(") + durationToString(begin) + "," + durationToString(end) + (endInclusive ? "]" : ")");

    }

    public String durationToString(Duration dur){
        String durStr = dur.toString().replace("PT", "");
        durStr = durStr.replace("P", "");
        return durStr;
    }

    //@Inject
    public TemporalRangeImpl(Boolean beginInclusive, Duration begin, Duration end, Boolean endInclusive) {
        this.beginInclusive = beginInclusive;
        this.endInclusive = endInclusive;
        this.begin = begin;
        this.end = end;
    }

    public TemporalRangeImpl(Boolean beginInclusive, String beginStr, String endStr, Boolean endInclusive) {
        this.beginInclusive = beginInclusive;
        this.endInclusive = endInclusive;
        this.begin = createDuration(beginStr);
        this.end = createDuration(endStr);
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

    @Override
    public TemporalRange clone() {
        return new TemporalRangeImpl(this.beginInclusive, this.begin, this.end, this.endInclusive);
    }

    private Duration createDuration(String durText){
        if (durText.contains("MS")) {
            durText = durText.substring(0, durText.indexOf("MS"));
            int ms = Integer.parseInt(durText);
            return Duration.parse("PT" + (ms / 1000) + "S");
        } else if (durText.contains("D")) {
            return Duration.parse("P" + durText);
        } else if (durText.contains("H") | durText.contains("M") | durText.contains("S")) {
            return Duration.parse("PT" + durText);
        } else if(durText.equals("0")) {
            return Duration.parse("PT0S");
        } else
            throw  new IllegalArgumentException("invalid duration "+ durText);
    }
}
