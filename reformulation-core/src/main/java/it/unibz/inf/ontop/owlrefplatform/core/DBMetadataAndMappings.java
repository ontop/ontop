package it.unibz.inf.ontop.owlrefplatform.core;

import com.google.common.collect.ImmutableList;
import it.unibz.inf.ontop.model.CQIE;
import it.unibz.inf.ontop.model.DBMetadata;

public class DBMetadataAndMappings {

    private final DBMetadata dbMetadata;
    private final ImmutableList<CQIE> mappingRules;

    public DBMetadataAndMappings(DBMetadata metadata, ImmutableList<CQIE> mappingRules) {
        this.dbMetadata = metadata;
        this.mappingRules = mappingRules;
    }

    public DBMetadata getDataSourceMetadata() {
        return dbMetadata;
    }

    public ImmutableList<CQIE> getMappingRules() {
        return mappingRules;
    }
}
