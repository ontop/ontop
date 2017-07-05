package it.unibz.inf.ontop.temporal.mapping.impl;

import it.unibz.inf.ontop.model.Function;
import it.unibz.inf.ontop.model.OBDASQLQuery;
import it.unibz.inf.ontop.temporal.mapping.TemporalMappingAxiom;
import it.unibz.inf.ontop.temporal.mapping.TemporalMappingInterval;
import it.unibz.inf.ontop.temporal.mapping.TemporalMappingTarget;

import java.util.List;

public class TemporalMappingAxiomImpl implements TemporalMappingAxiom {

    // TODO: use proper parsed query
    private final OBDASQLQuery sourceQuery;
    private List<Function> targetQuery;
    private final TemporalMappingInterval temporalMappingInterval;
    
    public TemporalMappingAxiomImpl(OBDASQLQuery sourceQuery, TemporalMappingInterval temporalMappingInterval, List<TemporalMappingTarget> targets) {
        this.sourceQuery = sourceQuery;
        this.temporalMappingInterval = temporalMappingInterval;
        this.targets = targets;
    }

    private final List<TemporalMappingTarget> targets;

    @Override
    public OBDASQLQuery getSourceSQLQuery() {
        return sourceQuery;
    }

    @Override
    public List<TemporalMappingTarget> getTargets() {
        return targets;
    }

    @Override
    public TemporalMappingInterval getTemporalMappingInterval() {
        return temporalMappingInterval;
    }
}
