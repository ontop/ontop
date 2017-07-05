package it.unibz.inf.ontop.temporal.mapping;

import it.unibz.inf.ontop.model.OBDASQLQuery;

import java.util.List;

public interface TemporalMappingAxiom {

    OBDASQLQuery getSourceSQLQuery();

    List<TemporalMappingTarget> getTargets();

    TemporalMappingInterval getTemporalMappingInterval();

}
