package it.unibz.inf.ontop.spec.impl;


import it.unibz.inf.ontop.dbschema.DBMetadata;
import it.unibz.inf.ontop.spec.mapping.MappingExtractor;
import it.unibz.inf.ontop.spec.mapping.MappingWithProvenance;

public class MappingAndDBMetadataImpl implements MappingExtractor.MappingAndDBMetadata {
    private final MappingWithProvenance mapping;
    private final DBMetadata dbMetadata;

    public MappingAndDBMetadataImpl(MappingWithProvenance mapping, DBMetadata dbMetadata) {
        this.mapping = mapping;
        this.dbMetadata = dbMetadata;
    }

    @Override
    public MappingWithProvenance getMapping() {
        return mapping;
    }

    @Override
    public DBMetadata getDBMetadata() {
        return dbMetadata;
    }
}
