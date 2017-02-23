package it.unibz.inf.ontop.temporal.mapping;

import java.util.List;

public interface TemporalMappingAxiom {

    String getSourceSQLQuery();

    List<TemporalMappingTarget> getTargets();

}
