package it.unibz.inf.ontop.query.impl;

import com.google.inject.Inject;
import it.unibz.inf.ontop.query.*;
import it.unibz.inf.ontop.injection.OntopKGQuerySettings;
import org.eclipse.rdf4j.query.BindingSet;
import org.eclipse.rdf4j.query.parser.*;


public class RDF4JQueryFactoryImpl implements RDF4JQueryFactory {

    private final OntopKGQuerySettings settings;

    @Inject
    private RDF4JQueryFactoryImpl(OntopKGQuerySettings settings) {
        this.settings = settings;
    }

    @Override
    public RDF4JSelectQuery createSelectQuery(String queryString, ParsedTupleQuery parsedQuery, BindingSet bindings) {
        return new RDF4JSelectQueryImpl(parsedQuery, queryString, bindings);
    }

    @Override
    public RDF4JAskQuery createAskQuery(String queryString, ParsedBooleanQuery parsedQuery, BindingSet bindings) {
        return new RDF4JAskQueryImpl(parsedQuery, queryString, bindings);
    }

    @Override
    public RDF4JConstructQuery createConstructQuery(String queryString, ParsedGraphQuery parsedQuery, BindingSet bindings) {
        return new RDF4JConstructQueryImpl(queryString, parsedQuery, bindings);
    }

    @Override
    public RDF4JDescribeQuery createDescribeQuery(String queryString, ParsedDescribeQuery parsedQuery, BindingSet bindings) {
        return new RDF4JDescribeQueryImpl(parsedQuery, queryString, bindings, settings.isFixedObjectIncludedInDescribe());
    }

    @Override
    public RDF4JInsertOperation createInsertOperation(String queryString, ParsedUpdate parsedUpdate) {
        return new RDF4JInsertOperationImpl(parsedUpdate, queryString);
    }
}
