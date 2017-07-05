package it.unibz.inf.ontop.temporal.mapping;

import it.unibz.inf.ontop.model.SQLPPMappingAxiom;

public interface SQLPPTemporalMappingAxiom extends SQLPPMappingAxiom {

    void setTemporalMappingInterval(TemporalMappingInterval temporalMappingInterval);

    TemporalMappingInterval getTemporalMappingInterval();

}
