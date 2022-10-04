package it.unibz.inf.ontop.query;

import it.unibz.inf.ontop.exception.OntopInvalidKGQueryException;
import it.unibz.inf.ontop.exception.OntopUnsupportedKGQueryException;

/**
 * NB: NOT USED by the Ontop RDF4J repository
 */
public interface KGQueryFactory {

    //-----------------
    // Concrete types
    //-----------------

    SelectQuery createSelectQuery(String queryString) throws OntopInvalidKGQueryException;

    AskQuery createAskQuery(String queryString) throws OntopInvalidKGQueryException;

    ConstructQuery createConstructQuery(String queryString) throws OntopInvalidKGQueryException;

    DescribeQuery createDescribeQuery(String queryString) throws OntopInvalidKGQueryException;

    InsertOperation createInsertQuery(String operationString) throws OntopInvalidKGQueryException;

    //-----------------
    // Generic types
    //-----------------

    SPARQLQuery createSPARQLQuery(String queryString) throws OntopInvalidKGQueryException, OntopUnsupportedKGQueryException;

    GraphSPARQLQuery createGraphQuery(String queryString) throws OntopInvalidKGQueryException, OntopUnsupportedKGQueryException;

}
