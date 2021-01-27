package it.unibz.inf.ontop.answering.reformulation.input.impl;

import com.google.inject.Inject;
import it.unibz.inf.ontop.answering.reformulation.input.*;
import it.unibz.inf.ontop.exception.OntopInvalidInputQueryException;
import it.unibz.inf.ontop.exception.OntopUnsupportedInputQueryException;
import org.eclipse.rdf4j.query.MalformedQueryException;
import org.eclipse.rdf4j.query.QueryLanguage;
import org.eclipse.rdf4j.query.impl.MapBindingSet;
import org.eclipse.rdf4j.query.parser.*;


public class InputQueryFactoryImpl implements InputQueryFactory {

    private final RDF4JInputQueryFactory rdf4jFactory;

    @Inject
    private InputQueryFactoryImpl(RDF4JInputQueryFactory rdf4jFactory) {
        this.rdf4jFactory = rdf4jFactory;
    }

    @Override
    public SelectQuery createSelectQuery(String queryString) throws OntopInvalidInputQueryException {
        ParsedQuery parsedQuery = parseQueryString(queryString);

        if (parsedQuery instanceof ParsedTupleQuery)
            return rdf4jFactory.createSelectQuery(queryString, parsedQuery, new MapBindingSet());
        else
            throw new OntopInvalidInputQueryException("Not a valid SELECT query: " + queryString);
    }

    @Override
    public AskQuery createAskQuery(String queryString) throws OntopInvalidInputQueryException {
        ParsedQuery parsedQuery = parseQueryString(queryString);

        if (parsedQuery instanceof ParsedBooleanQuery)
            return rdf4jFactory.createAskQuery(queryString, parsedQuery, new MapBindingSet());
        else
            throw new OntopInvalidInputQueryException("Not a valid ASK query: " + queryString);
    }

    @Override
    public ConstructQuery createConstructQuery(String queryString) throws OntopInvalidInputQueryException {
        ParsedQuery parsedQuery = parseQueryString(queryString);

        if ((parsedQuery instanceof ParsedGraphQuery) && !(parsedQuery instanceof ParsedDescribeQuery))
            return rdf4jFactory.createConstructQuery(queryString, parsedQuery, new MapBindingSet());
        else
            throw new OntopInvalidInputQueryException("Not a valid CONSTRUCT query: " + queryString);
    }

    @Override
    public DescribeQuery createDescribeQuery(String queryString) throws OntopInvalidInputQueryException {
        ParsedQuery parsedQuery = parseQueryString(queryString);

        if (parsedQuery instanceof ParsedDescribeQuery)
            return rdf4jFactory.createDescribeQuery(queryString, parsedQuery, new MapBindingSet());
        else
            throw new OntopInvalidInputQueryException("Not a valid DESCRIBE query: " + queryString);
    }

    @Override
    public SPARQLQuery createSPARQLQuery(String queryString)
            throws OntopInvalidInputQueryException, OntopUnsupportedInputQueryException {
        ParsedQuery parsedQuery = parseQueryString(queryString);

        if (parsedQuery instanceof ParsedTupleQuery)
            return rdf4jFactory.createSelectQuery(queryString, parsedQuery, new MapBindingSet());
        else if (parsedQuery instanceof ParsedBooleanQuery)
            return rdf4jFactory.createAskQuery(queryString, parsedQuery, new MapBindingSet());
        else if (parsedQuery instanceof ParsedDescribeQuery)
            return rdf4jFactory.createDescribeQuery(queryString, parsedQuery, new MapBindingSet());
        else if (parsedQuery instanceof ParsedGraphQuery)
            return rdf4jFactory.createConstructQuery(queryString, parsedQuery, new MapBindingSet());
        else
            throw new OntopUnsupportedInputQueryException("Unsupported query: " + queryString);
    }

    @Override
    public GraphSPARQLQuery createGraphQuery(String queryString) throws OntopInvalidInputQueryException,
            OntopUnsupportedInputQueryException {
        ParsedQuery parsedQuery = parseQueryString(queryString);

        if (parsedQuery instanceof ParsedDescribeQuery)
            return rdf4jFactory.createDescribeQuery(queryString, parsedQuery, new MapBindingSet());
        else if (parsedQuery instanceof ParsedGraphQuery)
            return rdf4jFactory.createConstructQuery(queryString, parsedQuery, new MapBindingSet());
        else
            throw new OntopUnsupportedInputQueryException("Unsupported query: " + queryString);
    }

    private static ParsedQuery parseQueryString(String queryString) throws OntopInvalidInputQueryException {
        try {
            return QueryParserUtil.parseQuery(QueryLanguage.SPARQL, queryString, null);
        } catch (MalformedQueryException e) {
            throw new OntopInvalidInputQueryException(e);
        }
    }
}
