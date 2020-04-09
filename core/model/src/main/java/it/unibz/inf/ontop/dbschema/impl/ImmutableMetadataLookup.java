package it.unibz.inf.ontop.dbschema.impl;

import com.google.common.collect.ImmutableMap;
import it.unibz.inf.ontop.dbschema.*;
import it.unibz.inf.ontop.exception.MetadataExtractionException;


public class ImmutableMetadataLookup implements MetadataLookup {

    protected final ImmutableMap<RelationID, RelationDefinition> map;

    public ImmutableMetadataLookup(ImmutableMap<RelationID, RelationDefinition> map) {
        this.map = map;
    }

    @Override
    public RelationDefinition getRelation(RelationID id) throws MetadataExtractionException {
        RelationDefinition relation = map.get(id);
        if (relation == null)
            throw new MetadataExtractionException("Relation " + id + " not found");

        return relation;
    }
}
