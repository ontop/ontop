package it.unibz.inf.ontop.dbschema;

import com.google.common.collect.ImmutableList;
import it.unibz.inf.ontop.exception.MetadataExtractionException;

import java.util.List;

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
    void insertIntegrityConstraints(NamedRelationDefinition relation, MetadataLookup metadataLookup) throws MetadataExtractionException;

    /**
     * Can be overloaded to take advantage of the presence of all relations to inference new constraints.
     * Useful for inferring FK between basic views.
     */
    default void insertIntegrityConstraints(ImmutableList<NamedRelationDefinition> relations, MetadataLookup metadataLookup)
            throws MetadataExtractionException {
        for (NamedRelationDefinition relation : relations) {
            insertIntegrityConstraints(relation, metadataLookup);
        }
    }

    DBParameters getDBParameters();

    /**
     * Mostly useful for OntopViewMetadataProvider-s
     */
    void normalizeRelations(List<NamedRelationDefinition> relationDefinitions);
}
