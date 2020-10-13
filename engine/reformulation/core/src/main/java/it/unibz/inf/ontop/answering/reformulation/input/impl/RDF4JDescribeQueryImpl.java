package it.unibz.inf.ontop.answering.reformulation.input.impl;

import it.unibz.inf.ontop.answering.reformulation.input.DescribeQuery;
import it.unibz.inf.ontop.answering.reformulation.input.RDF4JDescribeQuery;
import it.unibz.inf.ontop.answering.reformulation.input.RDF4JInputQuery;
import it.unibz.inf.ontop.answering.resultset.SimpleGraphResultSet;
import org.eclipse.rdf4j.query.BindingSet;
import org.eclipse.rdf4j.query.parser.ParsedQuery;


class RDF4JDescribeQueryImpl extends RDF4JInputQueryImpl<SimpleGraphResultSet> implements RDF4JDescribeQuery {

    RDF4JDescribeQueryImpl(ParsedQuery parsedQuery, String queryString, BindingSet bindings) {
        super(parsedQuery, queryString, bindings);
    }

    @Override
    public RDF4JInputQuery<SimpleGraphResultSet> newBindings(BindingSet newBindings) {
        return new RDF4JDescribeQueryImpl(parsedQuery, getInputString(), newBindings);
    }
}
