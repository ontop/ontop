package it.unibz.inf.ontop.temporal.mapping;

import it.unibz.inf.ontop.spec.mapping.OBDASQLQuery;

import java.util.List;

public interface TemporalMappingAxiom {

    OBDASQLQuery getSourceSQLQuery();

    List<TemporalMappingTarget> getTargets();

    TemporalMappingInterval getTemporalMappingInterval();

}
