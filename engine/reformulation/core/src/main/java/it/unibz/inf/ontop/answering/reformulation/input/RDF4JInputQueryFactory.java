package it.unibz.inf.ontop.answering.reformulation.input;

import org.eclipse.rdf4j.query.parser.ParsedQuery;

/**
 * Depends on RDF4J
 */
public interface RDF4JInputQueryFactory {

    /**
     * TODO: support bindings
     */
    SelectQuery createSelectQuery(String queryString, ParsedQuery parsedQuery);

    /**
     * TODO: support bindings
     */
    AskQuery createAskQuery(String queryString, ParsedQuery parsedQuery);

    /**
     * TODO: support bindings
     */
    ConstructQuery createConstructQuery(String queryString, ParsedQuery parsedQuery);

    /**
     * TODO: support bindings
     */
    DescribeQuery createDescribeQuery(String queryString, ParsedQuery parsedQuery);
}
