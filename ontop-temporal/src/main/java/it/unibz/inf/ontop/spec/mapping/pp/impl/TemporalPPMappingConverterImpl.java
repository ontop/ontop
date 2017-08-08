package it.unibz.inf.ontop.spec.mapping.pp.impl;

import com.google.inject.Inject;
import it.unibz.inf.ontop.datalog.Datalog2QueryMappingConverter;
import it.unibz.inf.ontop.dbschema.DBMetadata;
import it.unibz.inf.ontop.exception.InvalidMappingSourceQueriesException;
import it.unibz.inf.ontop.iq.tools.ExecutorRegistry;
import it.unibz.inf.ontop.spec.mapping.MappingWithProvenance;
import it.unibz.inf.ontop.spec.mapping.pp.SQLPPMapping;
import it.unibz.inf.ontop.spec.mapping.pp.TemporalPPMappingConverter;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class TemporalPPMappingConverterImpl implements TemporalPPMappingConverter {

    private static final Logger LOGGER = LoggerFactory.getLogger(LegacySQLPPMappingConverter.class);
    private final Datalog2QueryMappingConverter mappingConverter;

    @Inject
    private TemporalPPMappingConverterImpl(Datalog2QueryMappingConverter mappingConverter) {
        this.mappingConverter = mappingConverter;
    }
    @Override
    public MappingWithProvenance convert(SQLPPMapping ppMapping, DBMetadata dbMetadata, ExecutorRegistry executorRegistry) throws InvalidMappingSourceQueriesException {

        return convertIntoMappingWithProvenance(ppMapping, dbMetadata);
    }

    private MappingWithProvenance convertIntoMappingWithProvenance(SQLPPMapping ppMapping, DBMetadata dbMetadata) {
        return null;
    }
}
