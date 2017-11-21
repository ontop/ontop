package it.unibz.inf.ontop.temporal.model;

import java.time.Duration;

public interface TemporalRange {

    Boolean isBeginInclusive();

    Boolean isEndInclusive();

    Duration getBegin();

    Duration getEnd();

}
