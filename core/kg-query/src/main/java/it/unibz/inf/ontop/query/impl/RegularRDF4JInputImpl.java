package it.unibz.inf.ontop.query.impl;

import it.unibz.inf.ontop.query.resultset.OBDAResultSet;
import it.unibz.inf.ontop.exception.OntopUnsupportedKGQueryException;
import org.eclipse.rdf4j.query.BindingSet;
import org.eclipse.rdf4j.query.parser.ParsedQuery;

class RegularRDF4JInputImpl<R extends OBDAResultSet> extends RDF4JInputQueryImpl<R> {

    protected final ParsedQuery parsedQuery;

    RegularRDF4JInputImpl(ParsedQuery parsedQuery, String inputQueryString, BindingSet bindings) {
        super(inputQueryString, bindings);
        this.parsedQuery = parsedQuery;
    }

    @Override
    protected ParsedQuery transformParsedQuery() throws OntopUnsupportedKGQueryException {
        return parsedQuery;
    }
}
