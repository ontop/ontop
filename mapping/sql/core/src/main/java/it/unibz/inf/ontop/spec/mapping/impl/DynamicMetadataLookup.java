package it.unibz.inf.ontop.spec.mapping.impl;

import com.google.common.collect.ImmutableMap;
import it.unibz.inf.ontop.dbschema.*;
import it.unibz.inf.ontop.dbschema.impl.ImmutableMetadataProvider;
import it.unibz.inf.ontop.exception.MetadataExtractionException;

import java.util.*;

public class DynamicMetadataLookup implements MetadataLookup {

    private final MetadataProvider provider;
    private final Map<RelationID, RelationDefinition> map = new HashMap<>();

    public DynamicMetadataLookup(MetadataProvider provider) {
        this.provider = provider;
    }

    @Override
    public RelationDefinition getRelation(RelationID id) throws MetadataExtractionException {
        RelationDefinition relation = map.get(id);
        if (relation != null)
            return relation;

        RelationDefinition retrievedRelation = provider.getRelation(id);
        RelationID retrievedId = retrievedRelation.getID();
        if (map.containsKey(retrievedId))
            return map.get(retrievedId); // discard retrievedRelation

        map.put(retrievedId, retrievedRelation);

        if (!id.hasSchema() && retrievedId.hasSchema()) {
            RelationID schemalessId = retrievedId.getSchemalessID();
            if (map.containsKey(schemalessId))
                throw new MetadataExtractionException("Clashing schemaless IDs: " + retrievedId + " and " + map.get(schemalessId).getID());

            map.put(schemalessId, retrievedRelation);
        }

        return retrievedRelation;
    }

    public void insertIntegrityConstraints() throws MetadataExtractionException {
        ImmutableMetadataProvider metadata = new ImmutableMetadataProvider(provider.getDBParameters(), ImmutableMap.copyOf(map));
        for (RelationDefinition relation : metadata.getAllRelations())
            provider.insertIntegrityConstraints(relation, metadata);
    }

}
