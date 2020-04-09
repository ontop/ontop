package it.unibz.inf.ontop.dbschema.impl;

import com.google.common.collect.ImmutableList;
import it.unibz.inf.ontop.dbschema.*;
import it.unibz.inf.ontop.exception.MetadataExtractionException;

public class DelegatingRDBMetadataProvider implements MetadataProvider {
    protected final MetadataProvider provider;

    public DelegatingRDBMetadataProvider(MetadataProvider provider) {
        this.provider = provider;
    }

    @Override
    public DBParameters getDBParameters() {
        return provider.getDBParameters();
    }

    @Override
    public ImmutableList<RelationID> getRelationIDs() throws MetadataExtractionException {
        return provider.getRelationIDs();
    }

    @Override
    public RelationDefinition getRelation(RelationID relationId) throws MetadataExtractionException {
        return provider.getRelation(relationId);
    }

    @Override
    public void insertIntegrityConstraints(RelationDefinition relation, MetadataLookup metadataLookup) throws MetadataExtractionException {
        provider.insertIntegrityConstraints(relation, metadataLookup);
    }
}
