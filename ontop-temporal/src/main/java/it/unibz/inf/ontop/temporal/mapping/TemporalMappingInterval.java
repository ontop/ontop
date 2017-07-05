package it.unibz.inf.ontop.temporal.mapping;

import it.unibz.inf.ontop.model.Variable;

public interface TemporalMappingInterval {

    boolean isBeginInclusive();

    boolean isEndInclusive();

    Variable getBegin();

    Variable getEnd();

}
