package it.unibz.inf.ontop.temporal.mapping.impl;

import com.google.common.collect.ImmutableList;
import it.unibz.inf.ontop.exception.DuplicateMappingException;
import it.unibz.inf.ontop.mapping.MappingMetadata;
import it.unibz.inf.ontop.temporal.mapping.SQLPPTemporalMapping;
import it.unibz.inf.ontop.temporal.mapping.SQLPPTemporalMappingAxiom;
import it.unibz.inf.ontop.temporal.mapping.SQLPPTemporalMappingFactory;

public class SQLPPTemporalMappingFactoryImpl implements SQLPPTemporalMappingFactory {

    @Override
    public SQLPPTemporalMapping createSQLPreProcessedMapping(ImmutableList<SQLPPTemporalMappingAxiom> newMappings, MappingMetadata metadata) throws DuplicateMappingException {
        return null;
    }
}
