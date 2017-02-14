package it.unibz.inf.ontop.answering.input.impl;

import it.unibz.inf.ontop.answering.input.SelectQuery;
import it.unibz.inf.ontop.model.TupleResultSet;
import org.eclipse.rdf4j.query.parser.ParsedQuery;

class RDF4JSelectQuery extends RDF4JInputQuery<TupleResultSet> implements SelectQuery {

    /**
     * TODO: support bindings
     */
    RDF4JSelectQuery(ParsedQuery parsedQuery) {
        super(parsedQuery);
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;

        RDF4JSelectQuery that = (RDF4JSelectQuery) o;

        return getParsedQuery().equals(that.getParsedQuery());
    }

    @Override
    public int hashCode() {
        return getParsedQuery().hashCode();
    }
}
