package it.unibz.inf.ontop.temporal.mapping.impl;

import com.google.common.collect.ImmutableList;
import it.unibz.inf.ontop.spec.mapping.MappingMetadata;
import it.unibz.inf.ontop.temporal.mapping.SQLPPTemporalMapping;
import it.unibz.inf.ontop.temporal.mapping.SQLPPTemporalTriplesMap;


public class SQLPPTemporalMappingImpl implements SQLPPTemporalMapping {
    @Override
    public MappingMetadata getMetadata() {
        return null;
    }

    @Override
    public ImmutableList getTripleMaps() {
        return null;
    }

    @Override
    public SQLPPTemporalTriplesMap getPPTemporalMappingAxiom(String axiomId) {
        return null;
    }

    @Override
    public ImmutableList<SQLPPTemporalTriplesMap> getPPTemporalMappingAxioms() {
        return null;
    }
}
