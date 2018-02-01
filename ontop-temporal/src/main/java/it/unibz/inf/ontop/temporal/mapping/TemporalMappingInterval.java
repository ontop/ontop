package it.unibz.inf.ontop.temporal.mapping;

import it.unibz.inf.ontop.model.term.Variable;
import it.unibz.inf.ontop.model.term.VariableOrGroundTerm;

public interface TemporalMappingInterval {

    VariableOrGroundTerm isBeginInclusive();

    VariableOrGroundTerm isEndInclusive();

    Variable getBegin();

    Variable getEnd();

    String isBeginInclusiveToString();

    String isEndInclusiveToString();

}
