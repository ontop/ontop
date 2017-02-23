package it.unibz.inf.ontop.temporal.mapping.impl;

import it.unibz.inf.ontop.temporal.mapping.TemporalMappingAxiom;
import it.unibz.inf.ontop.temporal.mapping.TemporalMappingTarget;

import java.util.List;

public class TemporalMappingAxiomImpl implements TemporalMappingAxiom {
    
    public TemporalMappingAxiomImpl(String sourceQuery, List<TemporalMappingTarget> targets) {
        this.sourceQuery = sourceQuery;
        this.targets = targets;
    }

    // TODO: use proper parsed query
    private final String sourceQuery;

    private final List<TemporalMappingTarget> targets;

    @Override
    public String getSourceSQLQuery() {
        return sourceQuery;
    }

    @Override
    public List<TemporalMappingTarget> getTargets() {
        return targets;
    }
}
