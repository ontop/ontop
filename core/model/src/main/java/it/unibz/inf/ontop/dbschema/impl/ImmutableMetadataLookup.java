package it.unibz.inf.ontop.dbschema.impl;

import com.google.common.collect.ImmutableList;
import com.google.common.collect.ImmutableMap;
import com.google.common.collect.Maps;
import it.unibz.inf.ontop.dbschema.*;
import it.unibz.inf.ontop.exception.MetadataExtractionException;
import it.unibz.inf.ontop.utils.ImmutableCollectors;

import java.util.Optional;
import java.util.stream.Stream;

/**
 * USED ONLY IN TESTS, DOES NOT HANDLE DEFAULT SCHEMAS CORRECTLY
 */

public class ImmutableMetadataLookup implements MetadataLookup {
    private final ImmutableMap<RelationID, RelationDefinition> map;

    public ImmutableMetadataLookup(ImmutableList<RelationDefinition> list) {
        map = Stream.concat(
                list.stream()
                        .map(r -> Maps.immutableEntry(r.getID(), r)),
                list.stream()
                        .filter(r -> r.getID().hasSchema())
                        .map(r -> Maps.immutableEntry(r.getID().getSchemalessID(), r)))
                .collect(ImmutableCollectors.toMap());
    }

    @Override
    public RelationDefinition getRelation(RelationID id) throws MetadataExtractionException {
        RelationDefinition relation = map.get(id);
        if (relation == null)
            relation = map.get(id.getSchemalessID());

        if (relation == null)
            throw new MetadataExtractionException("Relation " + id + " not found");

        return relation;
    }
}
