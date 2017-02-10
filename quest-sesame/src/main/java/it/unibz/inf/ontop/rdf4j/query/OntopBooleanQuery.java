package it.unibz.inf.ontop.rdf4j.query;

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

import it.unibz.inf.ontop.owlrefplatform.core.OntopConnection;
import it.unibz.inf.ontop.owlrefplatform.core.OntopStatement;
import org.eclipse.rdf4j.query.BooleanQuery;
import org.eclipse.rdf4j.query.MalformedQueryException;
import org.eclipse.rdf4j.query.QueryEvaluationException;

import it.unibz.inf.ontop.model.TupleResultSet;

public class OntopBooleanQuery extends AbstractOntopQuery implements BooleanQuery {

	/**
	 * TODO: validate the input query
	 */
	public OntopBooleanQuery(String queryString, String baseURI, OntopConnection conn) throws MalformedQueryException {
        super(queryString, conn);
		// check if valid query string
//		if (queryString.contains("ASK")) {
//		} else
//			throw new MalformedQueryException("Boolean Query expected!");
	}

	@Override
	public boolean evaluate() throws QueryEvaluationException {
		try (OntopStatement stm = conn.createStatement();
			 TupleResultSet rs = (TupleResultSet) stm.execute(getQueryString())) {

			boolean next = rs.nextRow();
			if (next){
				return true;
				
			}
			return false;
		} catch (Exception e) {
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