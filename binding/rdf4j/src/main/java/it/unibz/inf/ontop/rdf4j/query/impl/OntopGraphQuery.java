package it.unibz.inf.ontop.rdf4j.query.impl;

import com.google.common.collect.ImmutableMultimap;
import it.unibz.inf.ontop.answering.connection.OntopConnection;
import it.unibz.inf.ontop.answering.connection.OntopStatement;
import it.unibz.inf.ontop.answering.reformulation.input.GraphSPARQLQuery;
import it.unibz.inf.ontop.answering.reformulation.input.RDF4JInputQueryFactory;
import it.unibz.inf.ontop.answering.resultset.GraphResultSet;
import it.unibz.inf.ontop.injection.OntopSystemSettings;
import it.unibz.inf.ontop.rdf4j.query.OntopCloseableStatementIteration;
import org.eclipse.rdf4j.query.GraphQuery;
import org.eclipse.rdf4j.query.GraphQueryResult;
import org.eclipse.rdf4j.query.QueryEvaluationException;
import org.eclipse.rdf4j.query.impl.IteratingGraphQueryResult;
import org.eclipse.rdf4j.query.parser.ParsedDescribeQuery;
import org.eclipse.rdf4j.query.parser.ParsedGraphQuery;
import org.eclipse.rdf4j.query.parser.ParsedQuery;
import org.eclipse.rdf4j.rio.RDFHandler;
import org.eclipse.rdf4j.rio.RDFHandlerException;

import java.util.Collections;
import java.util.Map;


public class OntopGraphQuery extends AbstractOntopQuery implements GraphQuery {

	private final RDF4JInputQueryFactory inputQueryFactory;
	private final boolean isConstruct;

	public OntopGraphQuery(String queryString, ParsedQuery parsedQuery, String baseIRI, OntopConnection ontopConnection,
						   ImmutableMultimap<String, String> httpHeaders, RDF4JInputQueryFactory inputQueryFactory,
						   OntopSystemSettings settings) {
		super(queryString, baseIRI, parsedQuery, ontopConnection, httpHeaders, settings);
		this.isConstruct = !(parsedQuery instanceof ParsedDescribeQuery);
		this.inputQueryFactory = inputQueryFactory;
	}

	@Override
	public GraphQueryResult evaluate() throws QueryEvaluationException {
		ParsedQuery parsedQuery = getParsedQuery();
	    GraphSPARQLQuery query =
    	    isConstruct
        	    ? inputQueryFactory.createConstructQuery(getQueryString(), parsedQuery, bindings)
            	: inputQueryFactory.createDescribeQuery(getQueryString(), parsedQuery, bindings);
		try
		{
			OntopStatement stm = conn.createStatement();
			if (this.queryTimeout > 0)
				stm.setQueryTimeout(this.queryTimeout);
			GraphResultSet res = stm.execute(query, getHttpHeaders());
			return new IteratingGraphQueryResult(Collections.emptyMap(), new OntopCloseableStatementIteration(res.iterator()));
		} catch (Exception e) {
			throw new QueryEvaluationException(e);
		}
	}

	@Override
	public void evaluate(RDFHandler handler) throws QueryEvaluationException,
			RDFHandlerException {
		try (GraphQueryResult result = evaluate()) {
			handler.startRDF();
			Map<String, String> namespaces = ((ParsedGraphQuery) getParsedQuery()).getQueryNamespaces();
			namespaces.forEach(handler::handleNamespace);
			result.forEach(handler::handleStatement);
			handler.endRDF();
		}
	}
}
