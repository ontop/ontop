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

    DBParameters getDBParameters();

    /**
     * Mostly useful for OntopViewMetadataProvider-s
     */
    void normalizeAndOptimizeRelations(List<NamedRelationDefinition> relationDefinitions);
}
