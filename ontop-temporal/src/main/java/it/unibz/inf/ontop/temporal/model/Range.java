package it.unibz.inf.ontop.temporal.model;

import java.time.Duration;

public interface Range {

    boolean getBeginInclusive();

    boolean getEndInclusive();

    Duration getBegin();

    Duration getEnd();

}
