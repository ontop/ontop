package org.semanticweb.ontop.sesame;

import java.util.List;

import org.openrdf.model.Value;
import org.openrdf.query.BindingSet;
import org.openrdf.query.Dataset;
import org.openrdf.query.MalformedQueryException;
import org.openrdf.query.QueryEvaluationException;
import org.openrdf.query.TupleQuery;
import org.openrdf.query.TupleQueryResult;
import org.openrdf.query.TupleQueryResultHandler;
import org.openrdf.query.TupleQueryResultHandlerException;
import org.semanticweb.ontop.model.OBDAException;
import org.semanticweb.ontop.model.TupleResultSet;
import org.semanticweb.ontop.owlrefplatform.core.QuestDBConnection;
import org.semanticweb.ontop.owlrefplatform.core.QuestDBStatement;

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



import org.openrdf.model.Value;
import org.openrdf.query.BindingSet;
import org.openrdf.query.Dataset;
import org.openrdf.query.MalformedQueryException;
import org.openrdf.query.QueryEvaluationException;
import org.openrdf.query.TupleQuery;
import org.openrdf.query.TupleQueryResult;
import org.openrdf.query.TupleQueryResultHandler;
import org.openrdf.query.TupleQueryResultHandlerException;

public class SesameTupleQuery extends SesameAbstractQuery implements TupleQuery {
	
	public SesameTupleQuery(String queryString, String baseURI, QuestDBConnection conn) 
			throws MalformedQueryException {
        super(queryString, conn);
	}
	
	// needed by TupleQuery interface
	public TupleQueryResult evaluate() throws QueryEvaluationException {
		TupleResultSet res = null;
		QuestDBStatement stm = null;
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
					throw new QueryEvaluationException("SesameTupleQuery timed out. More than " + this.queryTimeout + " seconds passed", e);
				} else 
					throw e;
			}
			
			List<String> signature = res.getSignature();
			return new SesameTupleQueryResult(res, signature);

		} catch (OBDAException e) {
			e.printStackTrace();
			throw new QueryEvaluationException(e);
		}
	}

	// needed by TupleQuery interface
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