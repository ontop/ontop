package it.unibz.inf.ontop.rdf4j.query.impl;

import com.google.common.collect.ImmutableMultimap;
import it.unibz.inf.ontop.query.AskQuery;
import it.unibz.inf.ontop.query.RDF4JQueryFactory;
import it.unibz.inf.ontop.exception.OntopConnectionException;
import it.unibz.inf.ontop.exception.OntopQueryAnsweringException;
import it.unibz.inf.ontop.query.resultset.BooleanResultSet;
import it.unibz.inf.ontop.answering.connection.OntopConnection;
import it.unibz.inf.ontop.answering.connection.OntopStatement;
import it.unibz.inf.ontop.injection.OntopSystemSettings;
import org.eclipse.rdf4j.query.BooleanQuery;
import org.eclipse.rdf4j.query.QueryEvaluationException;

import org.eclipse.rdf4j.query.parser.ParsedBooleanQuery;

public class OntopBooleanQuery extends AbstractOntopQuery<ParsedBooleanQuery> implements BooleanQuery {


	private final RDF4JQueryFactory factory;

	public OntopBooleanQuery(String queryString, ParsedBooleanQuery q, String baseIRI, OntopConnection ontopConnection,
                             ImmutableMultimap<String, String> httpHeaders,
                             RDF4JQueryFactory inputQueryFactory, OntopSystemSettings settings) {
        super(queryString, baseIRI, q, ontopConnection, httpHeaders, settings);
		this.factory = inputQueryFactory;
    }

    @Override
	public boolean evaluate() throws QueryEvaluationException {
		AskQuery query = factory.createAskQuery(getQueryString(), getParsedQuery(), bindings);

		try (OntopStatement stm = conn.createStatement()) {
			if (queryTimeout > 0)
				stm.setQueryTimeout(queryTimeout);
			try (BooleanResultSet rs = stm.execute(query, getHttpHeaders())) {
				return rs.getValue();
			}
		} catch (OntopConnectionException | OntopQueryAnsweringException e) {
			throw new QueryEvaluationException(e);
		}
	}
}