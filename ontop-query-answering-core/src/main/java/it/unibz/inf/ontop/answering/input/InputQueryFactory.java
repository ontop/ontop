package it.unibz.inf.ontop.answering.input;

import it.unibz.inf.ontop.exception.OntopInvalidInputQueryException;

/**
 * NB: NOT USED by the Ontop RDF4J repository
 */
public interface InputQueryFactory {

    SelectQuery createSelectQuery(String queryString) throws OntopInvalidInputQueryException;

    AskQuery createAskQuery(String queryString) throws OntopInvalidInputQueryException;

    ConstructQuery createConstructQuery(String queryString) throws OntopInvalidInputQueryException;

    DescribeQuery createDescribeQuery(String queryString) throws OntopInvalidInputQueryException;
}
