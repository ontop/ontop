package it.unibz.inf.ontop.dbschema.impl;

import com.google.common.collect.ImmutableList;
import com.google.common.collect.ImmutableMap;
import it.unibz.inf.ontop.dbschema.*;
import it.unibz.inf.ontop.exception.MetadataExtractionException;
import it.unibz.inf.ontop.exception.RelationNotFoundInMetadataException;
import it.unibz.inf.ontop.utils.ImmutableCollectors;

import java.util.function.Function;

public class ImmutableMetadataLookup implements MetadataLookup {

    protected final QuotedIDFactory idFactory;
    protected final ImmutableMap<RelationID, ? extends NamedRelationDefinition> map;

    ImmutableMetadataLookup(QuotedIDFactory idFactory, ImmutableMap<RelationID, ? extends NamedRelationDefinition> map) {
        this.idFactory = idFactory;
        this.map = map;
    }

    @Override
    public NamedRelationDefinition getRelation(RelationID id) throws MetadataExtractionException {
        NamedRelationDefinition relation = map.get(id);
        if (relation == null)
            throw new RelationNotFoundInMetadataException(id, map.keySet());

        return relation;
    }

    @Override
    public RelationDefinition getBlackBoxView(String query) {
        throw new UnsupportedOperationException();
    }

    @Override
    public QuotedIDFactory getQuotedIDFactory() {
        return idFactory;
    }


    protected ImmutableList<NamedRelationDefinition> getRelations() {
        // the list contains no repetitions (based on full relation ids)
        return map.values().stream()
                .collect(ImmutableCollectors.toMultimap(NamedRelationDefinition::getAllIDs, Function.identity())).asMap().values().stream()
                .map(s -> s.iterator().next())
                .collect(ImmutableCollectors.toList());
    }

}
