package it.unibz.inf.ontop.spec.mapping.impl;

import it.unibz.inf.ontop.dbschema.DBMetadata;
import it.unibz.inf.ontop.spec.mapping.Mapping;
import it.unibz.inf.ontop.spec.mapping.TemporalMapping;
import it.unibz.inf.ontop.spec.mapping.TemporalMappingExtractor;

public class TemporalMappingAndDBMetadataImpl implements TemporalMappingExtractor.MappingAndDBMetadata {
    private final TemporalMapping temporalMapping;
    private final DBMetadata dbMetadata;

    public TemporalMappingAndDBMetadataImpl(TemporalMapping mapping,  DBMetadata dbMetadata) {
        this.temporalMapping = mapping;
        this.dbMetadata = dbMetadata;
    }

    @Override
    public Mapping getMapping() {
        return null;
    }

    @Override
    public DBMetadata getDBMetadata() {
        return dbMetadata;
    }

    @Override
    public TemporalMapping getTemporalMapping() {
        return temporalMapping;
    }

}
