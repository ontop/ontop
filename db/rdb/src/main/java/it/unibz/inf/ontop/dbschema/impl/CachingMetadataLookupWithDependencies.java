package it.unibz.inf.ontop.dbschema.impl;

import com.google.common.collect.ImmutableList;
import it.unibz.inf.ontop.dbschema.*;
import it.unibz.inf.ontop.exception.MetadataExtractionException;

import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

public class CachingMetadataLookupWithDependencies extends CachingMetadataLookup {
    // maps a relation ID to the set of IDs of its bases (relations cannot be put in a set)
    private final Map<RelationID, Set<RelationID>> baseRelationIds = new HashMap<>();

    public CachingMetadataLookupWithDependencies(MetadataProvider provider) {
        super(provider);
    }

    public ImmutableList<NamedRelationDefinition> getBaseRelations(RelationID id) throws MetadataExtractionException {
        ImmutableList.Builder<NamedRelationDefinition> builder = ImmutableList.builder();
        for (RelationID baseId : baseRelationIds.get(id))
            builder.add(getRelation(baseId));
        return builder.build();
    }

    public MetadataLookup getCachingMetadataLookupFor(RelationID id) {
        return new MetadataLookup() {
            private final Set<RelationID> bases = baseRelationIds.computeIfAbsent(id, i -> new HashSet<>());

            @Override
            public NamedRelationDefinition getRelation(RelationID baseId) throws MetadataExtractionException {
                NamedRelationDefinition base = CachingMetadataLookupWithDependencies.this.getRelation(baseId);
                bases.add(base.getID());
                return base;
            }

            @Override
            public QuotedIDFactory getQuotedIDFactory() {
                return CachingMetadataLookupWithDependencies.this.getQuotedIDFactory();
            }
        };
    }
}
