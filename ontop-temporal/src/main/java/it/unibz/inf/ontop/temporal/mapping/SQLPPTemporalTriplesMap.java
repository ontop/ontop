package it.unibz.inf.ontop.temporal.mapping;

import it.unibz.inf.ontop.spec.mapping.pp.SQLPPTriplesMap;


public interface SQLPPTemporalTriplesMap extends SQLPPTriplesMap {

    void setTemporalMappingInterval(TemporalMappingInterval temporalMappingInterval);

    TemporalMappingInterval getTemporalMappingInterval();

}
