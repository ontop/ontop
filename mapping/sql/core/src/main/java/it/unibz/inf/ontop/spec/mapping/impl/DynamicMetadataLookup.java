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
        RelationID canonicalId = provider.getRelationCanonicalID(id);
        RelationDefinition def = map.get(canonicalId);

        if (def == null) {
            try {
                ImmutableList<RelationDefinition.AttributeListBuilder> builders = provider.getRelationAttributes(canonicalId);
                for (RelationDefinition.AttributeListBuilder builder : builders) {
                    RelationID retrievedId = builder.getRelationID();
                    RelationDefinition table = map.computeIfAbsent(retrievedId,
                            i -> new DatabaseRelationDefinition(builder));

                    if (def == null) // CATCH THE FIRST
                        def = table;

                    if (!id.hasSchema() && retrievedId.hasSchema())
                        map.putIfAbsent(retrievedId.getSchemalessID(), table);
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
