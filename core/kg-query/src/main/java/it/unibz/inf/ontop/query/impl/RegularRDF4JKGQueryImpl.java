package it.unibz.inf.ontop.query.impl;

import it.unibz.inf.ontop.query.resultset.OBDAResultSet;
import org.eclipse.rdf4j.query.BindingSet;
import org.eclipse.rdf4j.query.parser.ParsedQuery;

class RegularRDF4JKGQueryImpl<R extends OBDAResultSet> extends RDF4JKGQueryImpl<R> {

    protected final ParsedQuery parsedQuery;

    RegularRDF4JKGQueryImpl(ParsedQuery parsedQuery, String inputQueryString, BindingSet bindings) {
        super(inputQueryString, bindings);
        this.parsedQuery = parsedQuery;
    }

    @Override
    protected ParsedQuery transformParsedQuery() {
        return parsedQuery;
    }
}
