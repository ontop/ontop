package it.unibz.inf.ontop.mapping.extraction.impl;

import it.unibz.inf.ontop.mapping.Mapping;
import it.unibz.inf.ontop.mapping.extraction.DataSourceModel;
import it.unibz.inf.ontop.model.DBMetadata;


public class DataSourceModelImpl implements DataSourceModel {

    private final Mapping mapping;
    private final DBMetadata dbMetadata;

    public DataSourceModelImpl(Mapping mapping, DBMetadata dbMetadata) {
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
