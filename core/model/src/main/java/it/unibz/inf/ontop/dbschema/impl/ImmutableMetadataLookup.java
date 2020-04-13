package it.unibz.inf.ontop.dbschema.impl;

import com.google.common.collect.ImmutableMap;
import it.unibz.inf.ontop.dbschema.*;
import it.unibz.inf.ontop.exception.MetadataExtractionException;


public class ImmutableMetadataLookup implements MetadataLookup {

    protected final QuotedIDFactory idFactory;
    protected final ImmutableMap<RelationID, DatabaseRelationDefinition> map;

    ImmutableMetadataLookup(QuotedIDFactory idFactory, ImmutableMap<RelationID, DatabaseRelationDefinition> map) {
        this.idFactory = idFactory;
        this.map = map;
    }

    @Override
    public DatabaseRelationDefinition getRelation(RelationID id) throws MetadataExtractionException {
        DatabaseRelationDefinition relation = map.get(id);
        if (relation == null)
            throw new MetadataExtractionException("Relation " + id + " not found");

        return relation;
    }

    @Override
    public QuotedIDFactory getQuotedIDFactory() {
        return idFactory;
    }
}
