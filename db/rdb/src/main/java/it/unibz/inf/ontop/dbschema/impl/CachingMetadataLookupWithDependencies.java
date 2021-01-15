package it.unibz.inf.ontop.dbschema.impl;

import it.unibz.inf.ontop.dbschema.*;
import it.unibz.inf.ontop.exception.MetadataExtractionException;

import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

public class CachingMetadataLookupWithDependencies extends CachingMetadataLookup {
    private final Map<RelationID, Set<RelationID>> baseRelations = new HashMap<>();
    private final Set<RelationID> completeRelations = new HashSet<>(); // with integrity constraints

    public CachingMetadataLookupWithDependencies(MetadataProvider provider) {
        super(provider);
    }

    public Set<RelationID> getBaseRelations(RelationID id) { return baseRelations.get(id); }

    public MetadataLookup getCachingMetadataLookup(RelationID dependantRelationId) {
        return new MetadataLookup() {
            @Override
            public NamedRelationDefinition getRelation(RelationID id) throws MetadataExtractionException {
                NamedRelationDefinition relation = CachingMetadataLookupWithDependencies.this.getRelation(id);
                Set<RelationID> base = baseRelations.computeIfAbsent(dependantRelationId, i -> new HashSet<>());
                base.add(relation.getID());
                return relation;
            }

            @Override
            public QuotedIDFactory getQuotedIDFactory() {
                return CachingMetadataLookupWithDependencies.this.getQuotedIDFactory();
            }
        };
    }

    public boolean completeRelation(RelationID id) {
        return completeRelations.add(id);
    }
}
