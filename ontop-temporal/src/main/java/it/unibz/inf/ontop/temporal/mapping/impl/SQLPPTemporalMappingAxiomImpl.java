package it.unibz.inf.ontop.temporal.mapping.impl;


import com.google.common.collect.ImmutableList;
import it.unibz.inf.ontop.model.Function;
import it.unibz.inf.ontop.model.OBDASQLQuery;
import it.unibz.inf.ontop.model.SQLPPMappingAxiom;
import it.unibz.inf.ontop.model.SourceQuery;
import it.unibz.inf.ontop.model.impl.AbstractSQLPPMappingAxiom;
import it.unibz.inf.ontop.temporal.mapping.SQLPPTemporalMappingAxiom;
import it.unibz.inf.ontop.temporal.mapping.TemporalMappingInterval;

import java.util.List;

public class SQLPPTemporalMappingAxiomImpl extends AbstractSQLPPMappingAxiom implements SQLPPTemporalMappingAxiom{

    private SourceQuery sourceQuery;
    private List<Function> targetQuery;
    private TemporalMappingInterval temporalMappingInterval;

    public SQLPPTemporalMappingAxiomImpl(String id, SourceQuery sourceQuery, List<Function> targetQuery, TemporalMappingInterval temporalMappingInterval) {
        super(id);
        setSourceQuery(sourceQuery);
        setTargetQuery(targetQuery);
        setTemporalMappingInterval(temporalMappingInterval);
    }

    @Override
    public void setTemporalMappingInterval(TemporalMappingInterval temporalMappingInterval) {
        this.temporalMappingInterval = temporalMappingInterval;

    }

    @Override
    public TemporalMappingInterval getTemporalMappingInterval() {
        return temporalMappingInterval;
    }

    @Override
    public void setSourceQuery(SourceQuery query) {
        this.sourceQuery = query;

    }

    @Override
    public SourceQuery getSourceQuery() {
        return sourceQuery;
    }

    @Override
    public void setTargetQuery(List<Function> query) {
        this.targetQuery = ImmutableList.copyOf(query);
    }

    @Override
    public List<Function> getTargetQuery() {
        return targetQuery;
    }

    @Override
    public void setId(String id) {
        this.setId(id);
    }

    @Override
    public String getId() {
        return this.getId();
    }

    @Override
    public SQLPPMappingAxiom clone() {
        return null;
    }
}
