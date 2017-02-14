package it.unibz.inf.ontop.answering.input.impl;

import com.google.inject.Inject;
import it.unibz.inf.ontop.answering.input.*;
import org.eclipse.rdf4j.query.parser.ParsedQuery;


public class RDF4JInputQueryFactoryImpl implements RDF4JInputQueryFactory {

    @Inject
    private RDF4JInputQueryFactoryImpl() {
    }

    @Override
    public SelectQuery createSelectQuery(ParsedQuery parsedQuery) {
        return new RDF4JSelectQuery(parsedQuery);
    }

    @Override
    public AskQuery createAskQuery(ParsedQuery parsedQuery) {
        return new RDF4JAskQuery(parsedQuery);
    }

    @Override
    public ConstructQuery createConstructQuery(String queryString, ParsedQuery parsedQuery) {
        return new RDF4JConstructQuery(queryString, parsedQuery);
    }

    @Override
    public DescribeQuery createDescribeQuery(ParsedQuery parsedQuery) {
        return new RDF4JDescribeQuery(parsedQuery);
    }
}
