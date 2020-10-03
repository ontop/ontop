package it.unibz.inf.ontop.answering.reformulation.input;

import org.eclipse.rdf4j.query.BindingSet;
import org.eclipse.rdf4j.query.parser.ParsedQuery;

/**
 * Depends on RDF4J
 */
public interface RDF4JInputQueryFactory {

    SelectQuery createSelectQuery(String queryString, ParsedQuery parsedQuery, BindingSet bindings);

    AskQuery createAskQuery(String queryString, ParsedQuery parsedQuery, BindingSet bindings);

    ConstructQuery createConstructQuery(String queryString, ParsedQuery parsedQuery, BindingSet bindings);

    DescribeQuery createDescribeQuery(String queryString, ParsedQuery parsedQuery, BindingSet bindings);
}
