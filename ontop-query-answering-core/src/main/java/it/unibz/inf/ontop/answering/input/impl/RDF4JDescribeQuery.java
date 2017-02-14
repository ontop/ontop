package it.unibz.inf.ontop.answering.input.impl;

import it.unibz.inf.ontop.answering.input.DescribeQuery;
import it.unibz.inf.ontop.model.GraphResultSet;
import org.eclipse.rdf4j.query.parser.ParsedQuery;


class RDF4JDescribeQuery extends RDF4JInputQuery<GraphResultSet> implements DescribeQuery {
    RDF4JDescribeQuery(ParsedQuery parsedQuery) {
        super(parsedQuery);
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
