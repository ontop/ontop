package it.unibz.inf.ontop.spec.impl;


import com.google.common.collect.ImmutableList;
import it.unibz.inf.ontop.dbschema.DBParameters;
import it.unibz.inf.ontop.spec.mapping.MappingAssertion;
import it.unibz.inf.ontop.spec.mapping.MappingExtractor;

public class MappingAndDBMetadataImpl implements MappingExtractor.MappingAndDBParameters {
    private final ImmutableList<MappingAssertion> mapping;
    private final DBParameters dbParameters;

    public MappingAndDBMetadataImpl(ImmutableList<MappingAssertion> mapping, DBParameters dbParameters) {
        this.mapping = mapping;
        this.dbParameters = dbParameters;
    }

    @Override
    public ImmutableList<MappingAssertion> getMapping() {
        return mapping;
    }

    @Override
    public DBParameters getDBParameters() {
        return dbParameters;
    }
}
