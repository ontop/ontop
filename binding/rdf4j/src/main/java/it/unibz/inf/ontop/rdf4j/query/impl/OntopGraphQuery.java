package it.unibz.inf.ontop.rdf4j.query.impl;

import com.google.common.collect.ImmutableMultimap;
import it.unibz.inf.ontop.answering.reformulation.input.GraphSPARQLQuery;
import it.unibz.inf.ontop.answering.reformulation.input.RDF4JInputQueryFactory;
import it.unibz.inf.ontop.answering.resultset.SimpleGraphResultSet;
import it.unibz.inf.ontop.injection.OntopSystemSettings;

import java.util.Collections;

import it.unibz.inf.ontop.answering.connection.OntopConnection;
import it.unibz.inf.ontop.answering.connection.OntopStatement;
import it.unibz.inf.ontop.answering.reformulation.input.SPARQLQueryUtility;

import org.eclipse.rdf4j.common.iteration.CloseableIteratorIteration;
import org.eclipse.rdf4j.query.GraphQuery;
import org.eclipse.rdf4j.query.GraphQueryResult;
import org.eclipse.rdf4j.query.QueryEvaluationException;
import org.eclipse.rdf4j.query.impl.IteratingGraphQueryResult;
import org.eclipse.rdf4j.query.parser.ParsedQuery;
import org.eclipse.rdf4j.rio.RDFHandler;
import org.eclipse.rdf4j.rio.RDFHandlerException;


public class OntopGraphQuery extends AbstractOntopQuery implements GraphQuery {

	private final RDF4JInputQueryFactory inputQueryFactory;
	private final boolean isConstruct;

	public OntopGraphQuery(String queryString, ParsedQuery parsedQuery, String baseIRI, OntopConnection ontopConnection,
											   ImmutableMultimap<String, String> httpHeaders, RDF4JInputQueryFactory inputQueryFactory,
											   OntopSystemSettings settings) {
		super(queryString, baseIRI, parsedQuery, ontopConnection, httpHeaders, settings);
		// TODO: replace by something stronger (based on the parsed query)
		this.isConstruct = SPARQLQueryUtility.isConstructQuery(queryString);

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
			SimpleGraphResultSet res = stm.execute(query, getHttpHeaders());
			res.addStatementClosable(stm);
			return new IteratingGraphQueryResult(Collections.emptyMap(), new CloseableIteratorIteration<>(res.iterator()));
		} catch (Exception e) {
			throw new QueryEvaluationException(e);
		}
	}

	@Override
	public void evaluate(RDFHandler handler) throws QueryEvaluationException,
			        RDFHandlerException {
		try(GraphQueryResult result =  evaluate()) {
			handler.startRDF();
			while (result.hasNext())
				handler.handleStatement(result.next());
			handler.endRDF();
		}
	}
}
