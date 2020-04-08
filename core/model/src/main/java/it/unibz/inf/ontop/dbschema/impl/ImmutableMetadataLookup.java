package it.unibz.inf.ontop.dbschema.impl;

import com.google.common.collect.ImmutableList;
import com.google.common.collect.ImmutableMap;
import com.google.common.collect.Maps;
import it.unibz.inf.ontop.dbschema.*;
import it.unibz.inf.ontop.model.type.DBTypeFactory;
import it.unibz.inf.ontop.utils.ImmutableCollectors;

import java.util.stream.Stream;

/**
 * USED ONLY IN TESTS, DOES NOT HANDLE DEFAULT SCHEMAS CORRECTLY
 */


public class ImmutableMetadataLookup implements MetadataLookup {
    private final ImmutableMap<RelationID, RelationDefinition> map;
    private final QuotedIDFactory idFactory;
    private final DBTypeFactory dbTypeFactory;

    public ImmutableMetadataLookup(DBParameters dbParameters, ImmutableList<RelationDefinition> list) {
        map = Stream.concat(
                list.stream()
                        .map(r -> Maps.immutableEntry(r.getID(), r)),
                list.stream()
                        .filter(r -> r.getID().hasSchema())
                        .map(r -> Maps.immutableEntry(r.getID().getSchemalessID(), r)))
                .collect(ImmutableCollectors.toMap());
        idFactory = dbParameters.getQuotedIDFactory();
        dbTypeFactory = dbParameters.getDBTypeFactory();
    }

    @Override
    public RelationDefinition get(RelationID id) throws RelationNotFoundException {
        RelationDefinition relation = map.get(id);
        if (relation != null)
            return relation;
        relation = map.get(id.getSchemalessID());
        if (relation != null)
            return relation;

        throw new RelationNotFoundException(id);
    }

    @Override
    public QuotedIDFactory getQuotedIDFactory() {
        return idFactory;
    }

    @Override
    public DBTypeFactory getDBTypeFactory() {
        return dbTypeFactory;
    }
}
