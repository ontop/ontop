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
        RelationDefinition def = map.get(id);

        if (def == null) {
            RelationDefinition relation = provider.getRelation(id);
            RelationID retrievedId = relation.getID();
            def = map.computeIfAbsent(retrievedId, i -> relation);

            if (!id.hasSchema() && retrievedId.hasSchema()) {
                map.putIfAbsent(retrievedId.getSchemalessID(), def);
            }
        }

        return def;
    }

    public ImmutableMetadataProvider getImmutableDBMetadata() {
        return new ImmutableMetadataProvider(provider.getDBParameters(), ImmutableMap.copyOf(map));
    }

}
