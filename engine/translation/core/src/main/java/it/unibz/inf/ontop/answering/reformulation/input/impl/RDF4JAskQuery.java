package it.unibz.inf.ontop.answering.reformulation.input.impl;

import it.unibz.inf.ontop.answering.reformulation.input.AskQuery;
import it.unibz.inf.ontop.answering.resultset.BooleanResultSet;
import org.eclipse.rdf4j.query.parser.ParsedQuery;


class RDF4JAskQuery extends RDF4JInputQuery<BooleanResultSet>  implements AskQuery {

    RDF4JAskQuery(ParsedQuery parsedQuery, String queryString) {
        super(parsedQuery, queryString);
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;

        RDF4JAskQuery that = (RDF4JAskQuery) o;

        return getParsedQuery().equals(that.getParsedQuery());
    }

    @Override
    public int hashCode() {
        return getParsedQuery().hashCode();
    }
}
