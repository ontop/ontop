package it.unibz.inf.ontop.rdf4j.query.impl;

import com.google.common.collect.ImmutableMultimap;
import it.unibz.inf.ontop.query.RDF4JQueryFactory;
import it.unibz.inf.ontop.query.SelectQuery;
import it.unibz.inf.ontop.exception.OntopQueryAnsweringException;
import it.unibz.inf.ontop.query.resultset.TupleResultSet;

import it.unibz.inf.ontop.answering.connection.OntopConnection;
import it.unibz.inf.ontop.answering.connection.OntopStatement;
import it.unibz.inf.ontop.injection.OntopSystemSettings;
import org.eclipse.rdf4j.query.QueryEvaluationException;
import org.eclipse.rdf4j.query.TupleQuery;
import org.eclipse.rdf4j.query.TupleQueryResult;
import org.eclipse.rdf4j.query.TupleQueryResultHandler;
import org.eclipse.rdf4j.query.TupleQueryResultHandlerException;
import org.eclipse.rdf4j.query.parser.ParsedTupleQuery;

import java.util.List;


public class OntopTupleQuery extends AbstractOntopQuery<ParsedTupleQuery> implements TupleQuery {

	private final RDF4JQueryFactory factory;

	public OntopTupleQuery(String queryString, ParsedTupleQuery parsedQuery, String baseIRI, OntopConnection conn,
                           ImmutableMultimap<String, String> httpHeaders, RDF4JQueryFactory factory, OntopSystemSettings settings) {
		super(queryString, baseIRI, parsedQuery, conn, httpHeaders, settings);
		this.factory = factory;
	}

    @Override
	public TupleQueryResult evaluate() throws QueryEvaluationException {
		long start = System.currentTimeMillis();

		try {
			OntopStatement stm = conn.createStatement();
			if (queryTimeout > 0)
				stm.setQueryTimeout(queryTimeout);
			TupleResultSet res;
			try {
				SelectQuery inputQuery = factory.createSelectQuery(getQueryString(), getParsedQuery(), bindings);
				res = stm.execute(inputQuery, getHttpHeaders());
			}
			catch (OntopQueryAnsweringException e) {
				long end = System.currentTimeMillis();
				if (queryTimeout > 0 && (end - start) >= queryTimeout * 1000L) {
					throw new QueryEvaluationException("OntopTupleQuery timed out. More than " + queryTimeout + " seconds passed", e);
				}
				else {
					throw e;
				}
			}

			List<String> signature = res.getSignature();
			return new OntopTupleQueryResult(res, signature, getSalt());
		}
		catch (QueryEvaluationException e) {
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