package it.unibz.inf.ontop.answering.reformulation.input.impl;

import it.unibz.inf.ontop.answering.reformulation.input.DescribeQuery;
import it.unibz.inf.ontop.answering.resultset.SimpleGraphResultSet;
import org.eclipse.rdf4j.query.parser.ParsedQuery;


class RDF4JDescribeQuery extends RDF4JInputQuery<SimpleGraphResultSet> implements DescribeQuery {
    RDF4JDescribeQuery(ParsedQuery parsedQuery, String queryString) {
        super(parsedQuery, queryString);
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;

        RDF4JDescribeQuery that = (RDF4JDescribeQuery) o;

        return getParsedQuery().equals(that.getParsedQuery());
    }

    @Override
    public int hashCode() {
        return getParsedQuery().hashCode();
    }
}
