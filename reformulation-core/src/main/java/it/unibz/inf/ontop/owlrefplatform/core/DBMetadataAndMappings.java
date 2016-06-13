package it.unibz.inf.ontop.owlrefplatform.core;

import com.google.common.collect.ImmutableList;
import it.unibz.inf.ontop.model.CQIE;
import it.unibz.inf.ontop.model.DataSourceMetadata;

public class DBMetadataAndMappings {

    private final DataSourceMetadata dbMetadata;
    private final ImmutableList<CQIE> mappingRules;

    public DBMetadataAndMappings(DataSourceMetadata metadata, ImmutableList<CQIE> mappingRules) {
        this.dbMetadata = metadata;
        this.mappingRules = mappingRules;
    }

    public DataSourceMetadata getDataSourceMetadata() {
        return dbMetadata;
    }

    public ImmutableList<CQIE> getMappingRules() {
        return mappingRules;
    }
}
