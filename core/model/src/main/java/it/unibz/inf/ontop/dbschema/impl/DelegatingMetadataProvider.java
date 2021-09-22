package it.unibz.inf.ontop.dbschema.impl;

import com.google.common.collect.ImmutableList;
import it.unibz.inf.ontop.dbschema.*;
import it.unibz.inf.ontop.exception.MetadataExtractionException;

import java.util.List;

public class DelegatingMetadataProvider implements MetadataProvider {

    protected final MetadataProvider provider;

    public DelegatingMetadataProvider(MetadataProvider provider) {
        this.provider = provider;
    }

    @Override
    public DBParameters getDBParameters() {
        return provider.getDBParameters();
    }

    @Override
    public void normalizeRelations(List<NamedRelationDefinition> relationDefinitions) {
        provider.normalizeRelations(relationDefinitions);
    }

    @Override
    public ImmutableList<RelationID> getRelationIDs() throws MetadataExtractionException {
        return provider.getRelationIDs();
    }

    @Override
    public NamedRelationDefinition getRelation(RelationID relationId) throws MetadataExtractionException {
        return provider.getRelation(relationId);
    }

    @Override
    public RelationDefinition getBlackBoxView(String query) throws MetadataExtractionException {
        return provider.getBlackBoxView(query);
    }

    @Override
    public QuotedIDFactory getQuotedIDFactory() {
        return provider.getQuotedIDFactory();
    }

    @Override
    public void insertIntegrityConstraints(NamedRelationDefinition relation, MetadataLookup metadataLookup) throws MetadataExtractionException {
        provider.insertIntegrityConstraints(relation, metadataLookup);
    }
}
