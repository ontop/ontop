package it.unibz.inf.ontop.sesame;

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

import it.unibz.inf.ontop.model.GraphResultSet;
import it.unibz.inf.ontop.model.OBDAException;
import it.unibz.inf.ontop.ontology.Assertion;
import it.unibz.inf.ontop.owlrefplatform.core.QuestDBConnection;
import it.unibz.inf.ontop.owlrefplatform.core.QuestDBStatement;

import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;

import org.eclipse.rdf4j.model.Statement;
import org.eclipse.rdf4j.query.GraphQuery;
import org.eclipse.rdf4j.query.GraphQueryResult;
import org.eclipse.rdf4j.query.MalformedQueryException;
import org.eclipse.rdf4j.query.QueryEvaluationException;
import org.eclipse.rdf4j.query.algebra.evaluation.iterator.CollectionIteration;
import org.eclipse.rdf4j.query.impl.GraphQueryResultImpl;
import org.eclipse.rdf4j.rio.RDFHandler;
import org.eclipse.rdf4j.rio.RDFHandlerException;


public class SesameGraphQuery extends SesameAbstractQuery implements GraphQuery {

	private String baseURI;

	public SesameGraphQuery(String queryString, String baseURI,
			QuestDBConnection conn) throws MalformedQueryException {
        super(queryString, conn);
		if (queryString.toLowerCase().contains("construct") || queryString.toLowerCase().contains("describe")) {
			this.baseURI = baseURI;
		} else
			throw new MalformedQueryException("Graph query expected!");
	}

	private Statement createStatement(Assertion assertion) {

		Statement stm = SesameHelper.createStatement(assertion);
		if (stm.getSubject()!=null && stm.getPredicate()!=null && stm.getObject()!=null)
			return stm;
		else 
			return null;
	}

    @Override
	public GraphQueryResult evaluate() throws QueryEvaluationException {
		GraphResultSet res = null;
		QuestDBStatement stm = null;
		try {
			stm = conn.createStatement();
			res = (GraphResultSet) stm.execute(getQueryString());
			
			Map<String, String> namespaces = new HashMap<String, String>();
			List<Statement> results = new LinkedList<Statement>();
			if (res != null) {
				while (res.hasNext()) {
					List<Assertion> chunk = res.next();
					for (Assertion as : chunk) {
						Statement st = createStatement(as);
						if (st!=null)
							results.add(st);
					}
				}
			}
			
			//return new GraphQueryResultImpl(namespaces, results.iterator());
            return new GraphQueryResultImpl(namespaces, new CollectionIteration<>(results));
			
		} catch (OBDAException e) {
			throw new QueryEvaluationException(e);
		}
		finally{
			try {
				if (res != null)
				res.close();
			} catch (OBDAException e1) {
				e1.printStackTrace();
			}
			try {
				if (stm != null)
						stm.close();
			} catch (OBDAException e) {
				e.printStackTrace();
			}
		}
	}

        @Override
	public void evaluate(RDFHandler handler) throws QueryEvaluationException,
			RDFHandlerException {
		GraphQueryResult result =  evaluate();
		handler.startRDF();
		while (result.hasNext())
			handler.handleStatement(result.next());
		handler.endRDF();

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
