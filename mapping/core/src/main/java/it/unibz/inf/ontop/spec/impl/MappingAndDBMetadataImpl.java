package it.unibz.inf.ontop.spec.impl;


import it.unibz.inf.ontop.mapping.Mapping;
import it.unibz.inf.ontop.model.DBMetadata;
import it.unibz.inf.ontop.spec.MappingExtractor;

public class MappingAndDBMetadataImpl implements MappingExtractor.MappingAndDBMetadata {
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
