package it.unibz.inf.ontop.rdf4j.query.impl;

import com.google.common.collect.ImmutableMultimap;
import it.unibz.inf.ontop.answering.connection.OntopConnection;
import it.unibz.inf.ontop.answering.connection.OntopStatement;
import it.unibz.inf.ontop.query.GraphSPARQLQuery;
import it.unibz.inf.ontop.query.RDF4JQueryFactory;
import it.unibz.inf.ontop.query.resultset.GraphResultSet;
import it.unibz.inf.ontop.injection.OntopSystemSettings;
import it.unibz.inf.ontop.rdf4j.query.OntopCloseableStatementIteration;
import org.eclipse.rdf4j.query.GraphQuery;
import org.eclipse.rdf4j.query.GraphQueryResult;
import org.eclipse.rdf4j.query.QueryEvaluationException;
import org.eclipse.rdf4j.query.impl.IteratingGraphQueryResult;
import org.eclipse.rdf4j.query.parser.ParsedDescribeQuery;
import org.eclipse.rdf4j.query.parser.ParsedGraphQuery;
import org.eclipse.rdf4j.rio.RDFHandler;
import org.eclipse.rdf4j.rio.RDFHandlerException;

import java.util.Collections;
import java.util.Map;


public class OntopGraphQuery extends AbstractOntopQuery<ParsedGraphQuery> implements GraphQuery {

	private final RDF4JQueryFactory inputQueryFactory;

	public OntopGraphQuery(String queryString, ParsedGraphQuery parsedQuery, String baseIRI, OntopConnection ontopConnection,
						   ImmutableMultimap<String, String> httpHeaders, RDF4JQueryFactory inputQueryFactory,
						   OntopSystemSettings settings) {
		super(queryString, baseIRI, parsedQuery, ontopConnection, httpHeaders, settings);
		this.inputQueryFactory = inputQueryFactory;
	}

	@Override
	public GraphQueryResult evaluate() throws QueryEvaluationException {
		ParsedGraphQuery parsedQuery = getParsedQuery();
	    GraphSPARQLQuery query =
				!(parsedQuery instanceof ParsedDescribeQuery)
        	    ? inputQueryFactory.createConstructQuery(getQueryString(), parsedQuery, bindings)
            	: inputQueryFactory.createDescribeQuery(getQueryString(), (ParsedDescribeQuery) parsedQuery, bindings);
		try
		{
			OntopStatement stm = conn.createStatement();
			if (queryTimeout > 0)
				stm.setQueryTimeout(queryTimeout);
			GraphResultSet res = stm.execute(query, getHttpHeaders());
			return new IteratingGraphQueryResult(Collections.emptyMap(), new OntopCloseableStatementIteration(res.iterator(), getSalt()));
		}
		catch (Exception e) {
			throw new QueryEvaluationException(e);
		}
	}

	@Override
	public void evaluate(RDFHandler handler) throws QueryEvaluationException,
			RDFHandlerException {
		try (GraphQueryResult result = evaluate()) {
			handler.startRDF();
			Map<String, String> namespaces = getParsedQuery().getQueryNamespaces();
			namespaces.forEach(handler::handleNamespace);
			result.forEach(handler::handleStatement);
			handler.endRDF();
		}
	}
}
