package it.unibz.inf.ontop.spec.mapping.impl;

import it.unibz.inf.ontop.iq.IntermediateQuery;
import it.unibz.inf.ontop.temporal.mapping.TemporalMappingInterval;

public class IntervalAndIntermediateQuery {
private TemporalMappingInterval temporalMappingInterval;
private IntermediateQuery intermediateQuery;

    public IntervalAndIntermediateQuery(TemporalMappingInterval temporalMappingInterval, IntermediateQuery intermediateQuery) {
        this.temporalMappingInterval = temporalMappingInterval;
        this.intermediateQuery = intermediateQuery;
    }

    public TemporalMappingInterval getTemporalMappingInterval() {
        return temporalMappingInterval;
    }

    public IntermediateQuery getIntermediateQuery() {
        return intermediateQuery;
    }
}
