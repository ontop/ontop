package it.unibz.inf.ontop.spec.mapping.impl;

import com.google.common.collect.ImmutableList;
import com.google.common.collect.ImmutableMap;
import it.unibz.inf.ontop.dbschema.*;
import it.unibz.inf.ontop.dbschema.impl.ImmutableDBMetadataImpl;
import it.unibz.inf.ontop.exception.MetadataExtractionException;

import java.util.*;

public class DynamicMetadataLookup implements MetadataLookup {

    private final RDBMetadataProvider provider;
    private final Map<RelationID, RelationDefinition> map = new HashMap<>();

    public DynamicMetadataLookup(RDBMetadataProvider provider) {
        this.provider = provider;
    }

    @Override
    public Optional<RelationDefinition> getRelation(RelationID id) {
        RelationDefinition def = map.get(id);

        if (def == null) {
            try {
                Optional<RelationDefinition> relation = provider.getRelation(id);
                if (relation.isPresent()) {
                    RelationID retrievedId = relation.get().getID();
                    def = map.computeIfAbsent(retrievedId, i -> relation.get());

                    if (!id.hasSchema() && retrievedId.hasSchema()) {
                        map.putIfAbsent(retrievedId.getSchemalessID(), def);
                    }
                }
            }
            catch (MetadataExtractionException e) {
                return Optional.empty();
            }
        }

        return Optional.ofNullable(def);
    }

    public ImmutableDBMetadata getImmutableDBMetadata() {
        return new ImmutableDBMetadataImpl(provider.getDBParameters(), ImmutableMap.copyOf(map));
    }

}
