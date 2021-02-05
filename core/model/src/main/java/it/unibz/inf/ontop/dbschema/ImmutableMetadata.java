package it.unibz.inf.ontop.dbschema;

import com.google.common.collect.ImmutableList;
import it.unibz.inf.ontop.dbschema.impl.CachingMetadataLookup;
import it.unibz.inf.ontop.exception.MetadataExtractionException;

public interface ImmutableMetadata {

    ImmutableList<NamedRelationDefinition> getAllRelations();

    DBParameters getDBParameters();

    static ImmutableMetadata extractImmutableMetadata(MetadataProvider metadataProvider) throws MetadataExtractionException {
        CachingMetadataLookup lookup = new CachingMetadataLookup(metadataProvider);
        for (RelationID id : metadataProvider.getRelationIDs())
            lookup.getRelation(id);
        return lookup.extractImmutableMetadata();
    }
}
