package it.unibz.inf.ontop.dbschema.impl;

import com.google.common.collect.ImmutableList;
import com.google.common.collect.ImmutableMap;
import it.unibz.inf.ontop.dbschema.*;
import it.unibz.inf.ontop.exception.MetadataExtractionException;

import java.util.*;

public class CachingMetadataLookup implements MetadataLookup {

    private final MetadataProvider provider;
    private final Map<RelationID, NamedRelationDefinition> map = new HashMap<>();

    public CachingMetadataLookup(MetadataProvider provider) { this.provider = provider; }

    @Override
    public NamedRelationDefinition getRelation(RelationID relationId) throws MetadataExtractionException {
        NamedRelationDefinition relation = map.get(relationId);
        if (relation != null)
            return relation;

        NamedRelationDefinition retrievedRelation = provider.getRelation(relationId);
        for (RelationID retrievedId : retrievedRelation.getAllIDs()) {
            NamedRelationDefinition prev = map.put(retrievedId, retrievedRelation);
            if (prev != null)
                throw new MetadataExtractionException("Clashing relation IDs: " + retrievedId + " and " + relationId);
        }
        return retrievedRelation;
    }

    @Override
    public QuotedIDFactory getQuotedIDFactory() {
        return provider.getQuotedIDFactory();
    }


    public ImmutableMetadata extractImmutableMetadata() throws MetadataExtractionException {

        ImmutableMetadataLookup lookup = new ImmutableMetadataLookup(getQuotedIDFactory(), ImmutableMap.copyOf(map));
        ImmutableList<NamedRelationDefinition> list = lookup.getRelations();

        for (NamedRelationDefinition relation : list)
            provider.insertIntegrityConstraints(relation, lookup);

        provider.normalizeRelations(list);

        return new ImmutableMetadataImpl(provider.getDBParameters(), list);
    }
}
