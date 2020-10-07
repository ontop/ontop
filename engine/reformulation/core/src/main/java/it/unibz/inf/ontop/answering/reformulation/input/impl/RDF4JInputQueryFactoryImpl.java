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
    public SelectQuery createSelectQuery(String queryString, ParsedQuery parsedQuery, BindingSet bindings) {
        return new RDF4JSelectQuery(parsedQuery, queryString, bindings);
    }

    @Override
    public AskQuery createAskQuery(String queryString, ParsedQuery parsedQuery, BindingSet bindings) {
        return new RDF4JAskQuery(parsedQuery, queryString, bindings);
    }

    @Override
    public ConstructQuery createConstructQuery(String queryString, ParsedQuery parsedQuery, BindingSet bindings) {
        return new RDF4JConstructQuery(queryString, parsedQuery, bindings);
    }

    @Override
    public DescribeQuery createDescribeQuery(String queryString, ParsedQuery parsedQuery, BindingSet bindings) {
        return new RDF4JDescribeQuery(parsedQuery, queryString, bindings);
    }
}
