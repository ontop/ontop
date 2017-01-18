package it.unibz.inf.ontop.mapping.extraction.impl;

import it.unibz.inf.ontop.mapping.Mapping;
import it.unibz.inf.ontop.mapping.extraction.MappingAndDBMetadata;
import it.unibz.inf.ontop.model.DBMetadata;


public class MappingAndDBMetadataImpl implements MappingAndDBMetadata {

    private final Mapping mapping;
    private final DBMetadata dbMetadata;

    public MappingAndDBMetadataImpl(Mapping mapping, DBMetadata dbMetadata) {
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
}
