package it.unibz.inf.ontop.temporal.mapping.impl;

import com.google.common.collect.ImmutableList;
import it.unibz.inf.ontop.exception.DuplicateMappingException;
import it.unibz.inf.ontop.spec.mapping.MappingMetadata;
import it.unibz.inf.ontop.temporal.mapping.SQLPPTemporalMapping;
import it.unibz.inf.ontop.temporal.mapping.SQLPPTemporalTriplesMap;
import it.unibz.inf.ontop.temporal.mapping.SQLPPTemporalMappingFactory;

public class SQLPPTemporalMappingFactoryImpl implements SQLPPTemporalMappingFactory {

    @Override
    public SQLPPTemporalMapping createSQLPreProcessedMapping(ImmutableList<SQLPPTemporalTriplesMap> newMappings, MappingMetadata metadata) throws DuplicateMappingException {
        return null;
    }
}
