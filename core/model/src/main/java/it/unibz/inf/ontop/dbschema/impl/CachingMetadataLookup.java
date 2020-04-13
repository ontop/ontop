package it.unibz.inf.ontop.dbschema.impl;

import com.google.common.collect.ImmutableMap;
import it.unibz.inf.ontop.dbschema.*;
import it.unibz.inf.ontop.exception.MetadataExtractionException;

import java.util.*;

public class CachingMetadataLookup implements MetadataLookup {

    private final MetadataProvider provider;
    private final Map<RelationID, DatabaseRelationDefinition> map = new HashMap<>();

    public CachingMetadataLookup(MetadataProvider provider) { this.provider = provider; }

    @Override
    public DatabaseRelationDefinition getRelation(RelationID relationId) throws MetadataExtractionException {
        DatabaseRelationDefinition relation = map.get(relationId);
        if (relation != null)
            return relation;

        DatabaseRelationDefinition retrievedRelation = provider.getRelation(relationId);
        for (RelationID retrievedId : retrievedRelation.getAllIDs()) {
            DatabaseRelationDefinition prev = map.put(retrievedId, retrievedRelation);
            if (prev != null)
                throw new MetadataExtractionException("Clashing relation IDs: " + retrievedId + " and " + relationId);
        }
        return retrievedRelation;
    }

    @Override
    public QuotedIDFactory getQuotedIDFactory() {
        return provider.getQuotedIDFactory();
    }



    public ImmutableMetadataProvider insertIntegrityConstraints() throws MetadataExtractionException {
        ImmutableMetadataProvider metadata = new ImmutableMetadataProvider(provider.getDBParameters(), ImmutableMap.copyOf(map));
        for (DatabaseRelationDefinition relation : metadata.getAllRelations())
            provider.insertIntegrityConstraints(relation, metadata);
        return metadata;
    }

}
