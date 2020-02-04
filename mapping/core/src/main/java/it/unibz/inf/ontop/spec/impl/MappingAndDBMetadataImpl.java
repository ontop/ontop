package it.unibz.inf.ontop.spec.impl;


import com.google.common.collect.ImmutableList;
import it.unibz.inf.ontop.dbschema.DBMetadata;
import it.unibz.inf.ontop.spec.mapping.MappingAssertion;
import it.unibz.inf.ontop.spec.mapping.MappingExtractor;

public class MappingAndDBMetadataImpl implements MappingExtractor.MappingAndDBMetadata {
    private final ImmutableList<MappingAssertion> mapping;
    private final DBMetadata dbMetadata;

    public MappingAndDBMetadataImpl(ImmutableList<MappingAssertion> mapping, DBMetadata dbMetadata) {
        this.mapping = mapping;
        this.dbMetadata = dbMetadata;
    }

    @Override
    public ImmutableList<MappingAssertion> getMapping() {
        return mapping;
    }

    @Override
    public DBMetadata getDBMetadata() {
        return dbMetadata;
    }
}
