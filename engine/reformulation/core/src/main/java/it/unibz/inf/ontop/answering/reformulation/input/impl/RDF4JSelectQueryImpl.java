package it.unibz.inf.ontop.answering.reformulation.input.impl;

import it.unibz.inf.ontop.answering.reformulation.input.RDF4JSelectQuery;
import it.unibz.inf.ontop.answering.reformulation.input.SelectQuery;
import it.unibz.inf.ontop.answering.resultset.TupleResultSet;
import org.eclipse.rdf4j.query.BindingSet;
import org.eclipse.rdf4j.query.parser.ParsedQuery;

class RDF4JSelectQueryImpl extends RegularRDF4JInputImpl<TupleResultSet> implements RDF4JSelectQuery {

    RDF4JSelectQueryImpl(ParsedQuery parsedQuery, String queryString, BindingSet bindings) {
        super(parsedQuery, queryString, bindings);
    }

    @Override
    public RDF4JSelectQuery newBindings(BindingSet newBindings) {
        return new RDF4JSelectQueryImpl(parsedQuery, getInputString(), newBindings);
    }
}
