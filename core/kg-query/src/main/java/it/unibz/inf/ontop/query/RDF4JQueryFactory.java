package it.unibz.inf.ontop.query;

import org.eclipse.rdf4j.query.BindingSet;
import org.eclipse.rdf4j.query.parser.*;

/**
 * Depends on RDF4J
 */
public interface RDF4JQueryFactory {

    RDF4JSelectQuery createSelectQuery(String queryString, ParsedTupleQuery parsedQuery, BindingSet bindings);

    RDF4JAskQuery createAskQuery(String queryString, ParsedBooleanQuery parsedQuery, BindingSet bindings);

    RDF4JConstructQuery createConstructQuery(String queryString, ParsedGraphQuery parsedQuery, BindingSet bindings);

    RDF4JDescribeQuery createDescribeQuery(String queryString, ParsedDescribeQuery parsedQuery, BindingSet bindings);

    RDF4JInsertOperation createInsertOperation(String queryString, ParsedUpdate parsedUpdate);
}
