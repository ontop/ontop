package it.unibz.inf.ontop.answering.reformulation.input.impl;

import com.google.inject.Inject;
import it.unibz.inf.ontop.answering.reformulation.input.*;
import org.eclipse.rdf4j.query.BindingSet;
import org.eclipse.rdf4j.query.parser.ParsedQuery;


public class RDF4JInputQueryFactoryImpl implements RDF4JInputQueryFactory {

    @Inject
    private RDF4JInputQueryFactoryImpl() {
    }

    @Override
    public RDF4JSelectQuery createSelectQuery(String queryString, ParsedQuery parsedQuery, BindingSet bindings) {
        return new RDF4JSelectQueryImpl(parsedQuery, queryString, bindings);
    }

    @Override
    public RDF4JAskQuery createAskQuery(String queryString, ParsedQuery parsedQuery, BindingSet bindings) {
        return new RDF4JAskQueryImpl(parsedQuery, queryString, bindings);
    }

    @Override
    public RDF4JConstructQuery createConstructQuery(String queryString, ParsedQuery parsedQuery, BindingSet bindings) {
        return new RDF4JConstructQueryImpl(queryString, parsedQuery, bindings);
    }

    @Override
    public RDF4JDescribeQuery createDescribeQuery(String queryString, ParsedQuery parsedQuery, BindingSet bindings) {
        return new RDF4JDescribeQueryImpl(parsedQuery, queryString, bindings);
    }
}
