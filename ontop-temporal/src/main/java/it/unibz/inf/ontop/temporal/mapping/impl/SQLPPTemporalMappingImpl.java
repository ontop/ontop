package it.unibz.inf.ontop.temporal.mapping.impl;

import com.google.common.collect.ImmutableList;
import it.unibz.inf.ontop.mapping.MappingMetadata;
import it.unibz.inf.ontop.temporal.mapping.SQLPPTemporalMapping;
import it.unibz.inf.ontop.temporal.mapping.SQLPPTemporalMappingAxiom;


public class SQLPPTemporalMappingImpl implements SQLPPTemporalMapping {
    @Override
    public MappingMetadata getMetadata() {
        return null;
    }

    @Override
    public SQLPPTemporalMappingAxiom getPPTemporalMappingAxiom(String axiomId) {
        return null;
    }

    @Override
    public ImmutableList<SQLPPTemporalMappingAxiom> getPPTemporalMappingAxioms() {
        return null;
    }
}
