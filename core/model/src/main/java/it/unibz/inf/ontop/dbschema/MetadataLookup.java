package it.unibz.inf.ontop.dbschema;

import it.unibz.inf.ontop.exception.InvalidQueryException;
import it.unibz.inf.ontop.exception.MetadataExtractionException;

public interface MetadataLookup {

    /**
     * Retrieves the data definition object based on its name.
     *
     * @param id
     * @throws MetadataExtractionException if the relation is not found
     */
    NamedRelationDefinition getRelation(RelationID id) throws MetadataExtractionException;

    RelationDefinition getBlackBoxView(String query) throws InvalidQueryException, MetadataExtractionException;

    QuotedIDFactory getQuotedIDFactory();
}
