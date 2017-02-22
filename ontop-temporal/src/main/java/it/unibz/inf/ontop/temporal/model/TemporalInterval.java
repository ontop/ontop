package it.unibz.inf.ontop.temporal.model;

import java.time.Duration;
import java.time.Instant;

public interface TemporalInterval {

    boolean isBeginInclusive();

    boolean isEndInclusive();

    Instant getBegin();

    Instant getEnd();

}
