package it.unibz.inf.ontop.answering.reformulation.input;

import org.eclipse.rdf4j.query.BindingSet;
import org.eclipse.rdf4j.query.parser.ParsedQuery;

/**
 * Depends on RDF4J
 */
public interface RDF4JInputQueryFactory {

    RDF4JSelectQuery createSelectQuery(String queryString, ParsedQuery parsedQuery, BindingSet bindings);

    RDF4JAskQuery createAskQuery(String queryString, ParsedQuery parsedQuery, BindingSet bindings);

    RDF4JConstructQuery createConstructQuery(String queryString, ParsedQuery parsedQuery, BindingSet bindings);

    RDF4JDescribeQuery createDescribeQuery(String queryString, ParsedQuery parsedQuery, BindingSet bindings);
}
