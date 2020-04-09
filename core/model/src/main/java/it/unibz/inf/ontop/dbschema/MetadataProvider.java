package it.unibz.inf.ontop.dbschema;

import com.google.common.collect.ImmutableList;
import it.unibz.inf.ontop.exception.MetadataExtractionException;

import java.util.Optional;

/**
 */

public interface MetadataProvider {

    /**
     * Extracts relation IDs for all relations
     * @return relation IDs
     */
    ImmutableList<RelationID> getRelationIDs() throws MetadataExtractionException;

    /*
     *
     * @param relationID
     * @return
     */
    Optional<RelationDefinition> getRelation(RelationID relationId) throws MetadataExtractionException;

    /**
     * Inserts the user-supplied primary keys, unique constraints and foreign keys
     * into the metadata object
     */
    void insertIntegrityConstraints(ImmutableDBMetadata md) throws MetadataExtractionException;
}
