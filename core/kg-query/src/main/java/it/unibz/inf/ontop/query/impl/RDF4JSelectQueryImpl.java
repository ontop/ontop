package it.unibz.inf.ontop.query.impl;

import it.unibz.inf.ontop.query.RDF4JSelectQuery;
import it.unibz.inf.ontop.query.resultset.TupleResultSet;
import org.eclipse.rdf4j.query.BindingSet;
import org.eclipse.rdf4j.query.parser.ParsedQuery;

class RDF4JSelectQueryImpl extends RegularRDF4JKGQueryImpl<TupleResultSet> implements RDF4JSelectQuery {

    RDF4JSelectQueryImpl(ParsedQuery parsedQuery, String queryString, BindingSet bindings) {
        super(parsedQuery, queryString, bindings);
    }

    @Override
    public RDF4JSelectQuery newBindings(BindingSet newBindings) {
        return new RDF4JSelectQueryImpl(parsedQuery, getOriginalString(), newBindings);
    }
}
