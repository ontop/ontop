package it.unibz.inf.ontop.temporal.mapping;

import it.unibz.inf.ontop.model.Function;
import it.unibz.inf.ontop.model.Variable;

public interface TemporalMappingTarget {

    Function getObjectAtom();

    Variable getBeginInclusive();

    Variable getEndInclusive();

    Variable getBegin();

    Variable getEnd();
}
