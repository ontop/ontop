package it.unibz.inf.ontop.temporal.model;

import java.time.Duration;

public interface TemporalRange {

    boolean isBeginInclusive();

    boolean isEndInclusive();

    Duration getBegin();

    Duration getEnd();

}
