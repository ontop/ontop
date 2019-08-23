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

import it.unibz.inf.ontop.answering.reformulation.input.AskQuery;
import it.unibz.inf.ontop.answering.reformulation.input.RDF4JInputQueryFactory;
import it.unibz.inf.ontop.exception.OntopConnectionException;
import it.unibz.inf.ontop.exception.OntopQueryAnsweringException;
import it.unibz.inf.ontop.answering.resultset.BooleanResultSet;
import it.unibz.inf.ontop.answering.connection.OntopConnection;
import it.unibz.inf.ontop.answering.connection.OntopStatement;
import org.eclipse.rdf4j.query.BooleanQuery;
import org.eclipse.rdf4j.query.QueryEvaluationException;

import org.eclipse.rdf4j.query.parser.ParsedQuery;

public class OntopBooleanQuery extends AbstractOntopQuery implements BooleanQuery {


	private final RDF4JInputQueryFactory factory;

	public OntopBooleanQuery(String queryString, ParsedQuery q, String baseIRI, OntopConnection ontopConnection,
							 RDF4JInputQueryFactory inputQueryFactory) {
        super(queryString, baseIRI, q, ontopConnection);
		this.factory = inputQueryFactory;
    }

    @Override
	public boolean evaluate() throws QueryEvaluationException {
		AskQuery query = factory.createAskQuery(getQueryString(), getParsedQuery());

		try (OntopStatement stm = conn.createStatement();
			 BooleanResultSet rs = stm.execute(query)) {
			return rs.getValue();

		} catch (OntopConnectionException | OntopQueryAnsweringException e) {
			throw new QueryEvaluationException(e);
		}
	}

    @Override
    public void setMaxExecutionTime(int maxExecTime) {
        throw new UnsupportedOperationException();
    }

    @Override
    public int getMaxExecutionTime() {
        throw new UnsupportedOperationException();
    }
}