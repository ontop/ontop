package it.unibz.inf.ontop.spec.impl;


import it.unibz.inf.ontop.spec.mapping.Mapping;
import it.unibz.inf.ontop.dbschema.DBMetadata;
import it.unibz.inf.ontop.spec.mapping.MappingExtractor;
import it.unibz.inf.ontop.spec.mapping.MappingInTransformation;

public class MappingAndDBMetadataImpl implements MappingExtractor.MappingAndDBMetadata {
    private final MappingInTransformation mapping;
    private final DBMetadata dbMetadata;

    public MappingAndDBMetadataImpl(MappingInTransformation mapping, DBMetadata dbMetadata) {
        this.mapping = mapping;
        this.dbMetadata = dbMetadata;
    }

    @Override
    public MappingInTransformation getMapping() {
        return mapping;
    }

    @Override
    public DBMetadata getDBMetadata() {
        return dbMetadata;
    }
}
