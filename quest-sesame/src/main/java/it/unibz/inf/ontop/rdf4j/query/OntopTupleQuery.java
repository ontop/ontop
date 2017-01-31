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

import it.unibz.inf.ontop.model.OBDAException;
import it.unibz.inf.ontop.model.TupleResultSet;

import it.unibz.inf.ontop.owlrefplatform.core.OntopConnection;
import it.unibz.inf.ontop.owlrefplatform.core.OntopStatement;
import org.eclipse.rdf4j.query.MalformedQueryException;
import org.eclipse.rdf4j.query.QueryEvaluationException;
import org.eclipse.rdf4j.query.TupleQuery;
import org.eclipse.rdf4j.query.TupleQueryResult;
import org.eclipse.rdf4j.query.TupleQueryResultHandler;
import org.eclipse.rdf4j.query.TupleQueryResultHandlerException;

import java.util.List;


public class OntopTupleQuery extends AbstractOntopQuery implements TupleQuery {

	public OntopTupleQuery(String queryString, String baseURI, OntopConnection conn)
			throws MalformedQueryException {
        super(queryString, conn);
	}
	
	@Override
	public TupleQueryResult evaluate() throws QueryEvaluationException {
		TupleResultSet res = null;
		OntopStatement stm = null;
		long start = System.currentTimeMillis();
		try {
			stm = conn.createStatement();
			if(this.queryTimeout > 0)
				stm.setQueryTimeout(this.queryTimeout);
			try {
				res = (TupleResultSet) stm.execute(getQueryString());
			} catch (OBDAException e) {
				long end = System.currentTimeMillis();
				if (this.queryTimeout > 0 && (end - start) >= this.queryTimeout * 1000){
					throw new QueryEvaluationException("OntopTupleQuery timed out. More than " + this.queryTimeout + " seconds passed", e);
				} else 
					throw e;
			}
			
			List<String> signature = res.getSignature();
			return new OntopTupleQueryResult(res, signature);

		} catch (OBDAException e) {
			e.printStackTrace();
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