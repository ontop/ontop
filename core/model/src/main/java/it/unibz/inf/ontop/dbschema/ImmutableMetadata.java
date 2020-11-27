package it.unibz.inf.ontop.dbschema;

import com.fasterxml.jackson.databind.annotation.JsonDeserialize;
import com.google.common.collect.ImmutableList;
import it.unibz.inf.ontop.dbschema.impl.CachingMetadataLookup;
import it.unibz.inf.ontop.dbschema.impl.ImmutableMetadataImpl;
import it.unibz.inf.ontop.exception.MetadataExtractionException;

import java.io.File;
import java.io.IOException;

@JsonDeserialize(as = ImmutableMetadataImpl.class)
public interface ImmutableMetadata {

    ImmutableList<DatabaseRelationDefinition> getAllRelations();

    DBParameters getDBParameters();

    File getFile();

    static ImmutableMetadata extractImmutableMetadata(MetadataProvider metadataProvider) throws MetadataExtractionException {
        CachingMetadataLookup lookup = new CachingMetadataLookup(metadataProvider);
        for (RelationID id : metadataProvider.getRelationIDs())
            lookup.getRelation(id);
        return lookup.extractImmutableMetadata();
    }

    static ImmutableMetadata loadImmutableMetadata(File dbMetadataFile) throws MetadataExtractionException, IOException {
        CachingMetadataLookup lookup = new CachingMetadataLookup(dbMetadataFile);
//        for (RelationID id : metadataProvider.getRelationIDs())
//            lookup.getRelation(id);
        return lookup.loadImmutableMetadata();
    }
}
