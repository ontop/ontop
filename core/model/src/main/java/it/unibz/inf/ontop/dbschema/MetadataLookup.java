package it.unibz.inf.ontop.dbschema;

import it.unibz.inf.ontop.exception.MetadataExtractionException;

public interface MetadataLookup {

    /**
     * Retrieves the data definition object based on its name.
     *
     * @param id
     * @throws MetadataExtractionException if the relation is not found
     */

    DatabaseRelationDefinition getRelation(RelationID id) throws MetadataExtractionException;

    QuotedIDFactory getQuotedIDFactory();
}
