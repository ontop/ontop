package it.unibz.inf.ontop.dbschema;

import com.google.common.collect.ImmutableList;
import it.unibz.inf.ontop.exception.MetadataExtractionException;

/**
 */
public interface MetadataProvider {

    /**
     * Extracts relation IDs for all relations
     * @return relation IDs
     */
    ImmutableList<RelationID> getRelationIDs() throws MetadataExtractionException;

    /**
     * relationID can be mapped to many tables (if, for example, it has no schema)
     *
     * @param relationID
     * @return
     */
    ImmutableList<RelationDefinition.AttributeListBuilder> getRelationAttributes(RelationID relationID) throws MetadataExtractionException;

    /**
     * Inserts the user-supplied primary keys, unique constraints and foreign keys
     * into the metadata object
     */
    void insertIntegrityConstraints(MetadataLookup md) throws MetadataExtractionException;
}
