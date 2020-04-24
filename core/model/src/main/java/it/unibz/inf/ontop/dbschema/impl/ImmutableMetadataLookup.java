package it.unibz.inf.ontop.dbschema.impl;

import com.google.common.collect.ImmutableList;
import com.google.common.collect.ImmutableMap;
import it.unibz.inf.ontop.dbschema.DatabaseRelationDefinition;
import it.unibz.inf.ontop.dbschema.MetadataLookup;
import it.unibz.inf.ontop.dbschema.QuotedIDFactory;
import it.unibz.inf.ontop.dbschema.RelationID;
import it.unibz.inf.ontop.exception.MetadataExtractionException;
import it.unibz.inf.ontop.utils.ImmutableCollectors;

import java.util.function.Function;

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


    protected ImmutableList<DatabaseRelationDefinition> getRelations() {
        // the list contains no repetitions (based on full relation ids)
        return map.values().stream()
                .collect(ImmutableCollectors.toMultimap(DatabaseRelationDefinition::getAllIDs, Function.identity())).asMap().values().stream()
                .map(s -> s.iterator().next())
                .collect(ImmutableCollectors.toList());
    }

}
