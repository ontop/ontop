package it.unibz.inf.ontop.answering.input;

import org.eclipse.rdf4j.query.parser.ParsedQuery;

/**
 * Depends on RDF4J
 */
public interface RDF4JInputQueryFactory {

    /**
     * TODO: support bindings
     */
    SelectQuery createSelectQuery(ParsedQuery parsedQuery);

    /**
     * TODO: support bindings
     */
    AskQuery createAskQuery(ParsedQuery parsedQuery);

    /**
     * TODO: support bindings
     */
    ConstructQuery createConstructQuery(String queryString, ParsedQuery parsedQuery);

    /**
     * TODO: support bindings
     */
    DescribeQuery createDescribeQuery(ParsedQuery parsedQuery);
}
