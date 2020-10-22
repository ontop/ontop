package it.unibz.inf.ontop.rdf4j.query.impl;

import com.google.common.collect.ImmutableMultimap;
import it.unibz.inf.ontop.answering.reformulation.input.RDF4JInputQueryFactory;
import it.unibz.inf.ontop.answering.reformulation.input.SelectQuery;
import it.unibz.inf.ontop.exception.OntopQueryAnsweringException;
import it.unibz.inf.ontop.answering.resultset.TupleResultSet;

import it.unibz.inf.ontop.answering.connection.OntopConnection;
import it.unibz.inf.ontop.answering.connection.OntopStatement;
import it.unibz.inf.ontop.injection.OntopSystemSettings;
import org.eclipse.rdf4j.query.QueryEvaluationException;
import org.eclipse.rdf4j.query.TupleQuery;
import org.eclipse.rdf4j.query.TupleQueryResult;
import org.eclipse.rdf4j.query.TupleQueryResultHandler;
import org.eclipse.rdf4j.query.TupleQueryResultHandlerException;
import org.eclipse.rdf4j.query.parser.ParsedQuery;

import java.security.SecureRandom;
import java.util.List;


public class OntopTupleQuery extends AbstractOntopQuery implements TupleQuery {

	private final RDF4JInputQueryFactory factory;

	public OntopTupleQuery(String queryString, ParsedQuery parsedQuery, String baseIRI, OntopConnection conn,
						   ImmutableMultimap<String, String> httpHeaders, RDF4JInputQueryFactory factory, OntopSystemSettings settings) {
		super(queryString, baseIRI, parsedQuery, conn, httpHeaders, settings);
		this.factory = factory;
	}

    @Override
	public TupleQueryResult evaluate() throws QueryEvaluationException {
		TupleResultSet res;
		OntopStatement stm;
		long start = System.currentTimeMillis();

		SecureRandom random = new SecureRandom();
		byte[] salt = new byte[20];
		random.nextBytes(salt);

		try {
			stm = conn.createStatement();
			if(this.queryTimeout > 0)
				stm.setQueryTimeout(this.queryTimeout);
			try {
				SelectQuery inputQuery = factory.createSelectQuery(getQueryString(), getParsedQuery(), bindings);
				res = stm.execute(inputQuery, getHttpHeaders());
			} catch (OntopQueryAnsweringException e) {
				long end = System.currentTimeMillis();
				if (this.queryTimeout > 0 && (end - start) >= this.queryTimeout * 1000){
					throw new QueryEvaluationException("OntopTupleQuery timed out. More than " + this.queryTimeout + " seconds passed", e);
				} else 
					throw e;
			}
			
			List<String> signature = res.getSignature();
			return new OntopTupleQueryResult(res, signature, salt);

		} catch (QueryEvaluationException e) {
			throw e;
		}
		catch (Exception e) {
			throw new QueryEvaluationException(e);
		}
	}

    @Override
	public void evaluate(TupleQueryResultHandler handler) 
			throws QueryEvaluationException, TupleQueryResultHandlerException {
		TupleQueryResult result = evaluate();
		handler.startQueryResult(result.getBindingNames());
		while (result.hasNext()) {
			handler.handleSolution(result.next());
		}
		handler.endQueryResult();
	}
}