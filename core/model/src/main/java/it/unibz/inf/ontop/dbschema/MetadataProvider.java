package it.unibz.inf.ontop.dbschema;

import com.google.common.collect.ImmutableList;
import it.unibz.inf.ontop.dbschema.impl.CachingMetadataLookup;
import it.unibz.inf.ontop.dbschema.impl.ImmutableMetadataProvider;
import it.unibz.inf.ontop.exception.MetadataExtractionException;

/**
 */

public interface MetadataProvider extends MetadataLookup {

    /**
     * Extracts relation IDs for all relations
     * @return relation IDs
     */
    ImmutableList<RelationID> getRelationIDs() throws MetadataExtractionException;

    /**
     * Inserts the user-supplied primary keys, unique constraints and foreign keys
     * into the metadata object
     */
    void insertIntegrityConstraints(DatabaseRelationDefinition relation, MetadataLookup metadataLookup) throws MetadataExtractionException;

    DBParameters getDBParameters();



    static ImmutableMetadataProvider getAllRelationsWithIntegrityConstraints(MetadataProvider metadataProvider) throws MetadataExtractionException {
        CachingMetadataLookup builder = new CachingMetadataLookup(metadataProvider);
        for (RelationID id : metadataProvider.getRelationIDs())
            builder.getRelation(id);

        return builder.insertIntegrityConstraints();
    }
}
