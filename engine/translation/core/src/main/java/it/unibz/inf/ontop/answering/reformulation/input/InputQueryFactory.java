package it.unibz.inf.ontop.answering.reformulation.input;

import it.unibz.inf.ontop.exception.OntopInvalidInputQueryException;
import it.unibz.inf.ontop.exception.OntopUnsupportedInputQueryException;

/**
 * NB: NOT USED by the Ontop RDF4J repository
 */
public interface InputQueryFactory {

    //-----------------
    // Concrete types
    //-----------------

    SelectQuery createSelectQuery(String queryString) throws OntopInvalidInputQueryException;

    AskQuery createAskQuery(String queryString) throws OntopInvalidInputQueryException;

    ConstructQuery createConstructQuery(String queryString) throws OntopInvalidInputQueryException;

    DescribeQuery createDescribeQuery(String queryString) throws OntopInvalidInputQueryException;

    //-----------------
    // Generic types
    //-----------------

    SPARQLQuery createSPARQLQuery(String queryString) throws OntopInvalidInputQueryException, OntopUnsupportedInputQueryException;

    GraphSPARQLQuery createGraphQuery(String queryString) throws OntopInvalidInputQueryException, OntopUnsupportedInputQueryException;

}
