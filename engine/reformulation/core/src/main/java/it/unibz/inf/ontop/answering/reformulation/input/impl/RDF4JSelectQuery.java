package it.unibz.inf.ontop.answering.reformulation.input.impl;

import it.unibz.inf.ontop.answering.reformulation.input.SelectQuery;
import it.unibz.inf.ontop.answering.resultset.TupleResultSet;
import org.eclipse.rdf4j.query.parser.ParsedQuery;

class RDF4JSelectQuery extends RDF4JInputQuery<TupleResultSet> implements SelectQuery {

    /**
     * TODO: support bindings
     */
    RDF4JSelectQuery(ParsedQuery parsedQuery, String queryString) {
        super(parsedQuery, queryString);
    }
}
