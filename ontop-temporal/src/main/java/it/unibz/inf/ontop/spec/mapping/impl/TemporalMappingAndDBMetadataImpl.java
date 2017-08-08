package it.unibz.inf.ontop.spec.mapping.impl;

import it.unibz.inf.ontop.dbschema.DBMetadata;
import it.unibz.inf.ontop.spec.mapping.Mapping;
import it.unibz.inf.ontop.spec.mapping.TemporalMappingExtractor;

public class TemporalMappingAndDBMetadataImpl implements TemporalMappingExtractor.MappingAndDBMetadata {
    private final Mapping mapping;
    private final DBMetadata dbMetadata;
    private final Mapping temporalMapping;

    public TemporalMappingAndDBMetadataImpl(Mapping temporalMapping, Mapping mapping, DBMetadata dbMetadata) {
        this.temporalMapping = temporalMapping;
        this.mapping = mapping;
        this.dbMetadata = dbMetadata;
    }

    @Override
    public Mapping getMapping() {
        return mapping;
    }

    @Override
    public DBMetadata getDBMetadata() {
        return dbMetadata;
    }

    @Override
    public Mapping getTemporalMapping() {
        return temporalMapping;
    }
}
