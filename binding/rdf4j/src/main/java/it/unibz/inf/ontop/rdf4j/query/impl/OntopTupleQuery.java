package it.unibz.inf.ontop.rdf4j.query.impl;

/*
 * #%L
 * ontop-quest-sesame
 * %%
 * Copyright (C) 2009 - 2014 Free University of Bozen-Bolzano
 * %%
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 * 
 *      http://www.apache.org/licenses/LICENSE-2.0
 * 
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 * #L%
 */

import it.unibz.inf.ontop.answering.reformulation.input.RDF4JInputQueryFactory;
import it.unibz.inf.ontop.answering.reformulation.input.SelectQuery;
import it.unibz.inf.ontop.exception.OntopQueryAnsweringException;
import it.unibz.inf.ontop.answering.resultset.TupleResultSet;

import it.unibz.inf.ontop.answering.connection.OntopConnection;
import it.unibz.inf.ontop.answering.connection.OntopStatement;
import org.eclipse.rdf4j.query.QueryEvaluationException;
import org.eclipse.rdf4j.query.TupleQuery;
import org.eclipse.rdf4j.query.TupleQueryResult;
import org.eclipse.rdf4j.query.TupleQueryResultHandler;
import org.eclipse.rdf4j.query.TupleQueryResultHandlerException;
import org.eclipse.rdf4j.query.parser.ParsedQuery;

import java.util.List;


public class OntopTupleQuery extends AbstractOntopQuery implements TupleQuery {

	private final RDF4JInputQueryFactory factory;

	public OntopTupleQuery(String queryString, ParsedQuery parsedQuery, String baseIRI, OntopConnection conn,
						   RDF4JInputQueryFactory factory) {
		super(queryString, baseIRI, parsedQuery, conn);
		this.factory = factory;
	}

    @Override
	public TupleQueryResult evaluate() throws QueryEvaluationException {
		TupleResultSet res;
		OntopStatement stm;
		long start = System.currentTimeMillis();
		try {
			stm = conn.createStatement();
			if(this.queryTimeout > 0)
				stm.setQueryTimeout(this.queryTimeout);
			try {
				SelectQuery inputQuery = factory.createSelectQuery(getQueryString(), getParsedQuery());
				res = stm.execute(inputQuery);
			} catch (OntopQueryAnsweringException e) {
				long end = System.currentTimeMillis();
				if (this.queryTimeout > 0 && (end - start) >= this.queryTimeout * 1000){
					throw new QueryEvaluationException("OntopTupleQuery timed out. More than " + this.queryTimeout + " seconds passed", e);
				} else 
					throw e;
			}
			
			List<String> signature = res.getSignature();
			return new OntopTupleQueryResult(res, signature);

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

    @Override
    public void setMaxExecutionTime(int maxExecTime) {
        throw new UnsupportedOperationException("not implemented");
    }

    @Override
    public int getMaxExecutionTime() {
        throw new UnsupportedOperationException("not implemented");
    }
}