package sesameWrapper;

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

import it.unibz.krdb.obda.model.GraphResultSet;
import it.unibz.krdb.obda.model.OBDAException;
import it.unibz.krdb.obda.ontology.Assertion;
import it.unibz.krdb.obda.owlrefplatform.core.QuestDBConnection;
import it.unibz.krdb.obda.owlrefplatform.core.QuestDBStatement;
import it.unibz.krdb.obda.sesame.SesameStatement;

import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;

import org.openrdf.model.Statement;
import org.openrdf.query.GraphQuery;
import org.openrdf.query.GraphQueryResult;
import org.openrdf.query.MalformedQueryException;
import org.openrdf.query.QueryEvaluationException;
import org.openrdf.query.impl.GraphQueryResultImpl;
import org.openrdf.rio.RDFHandler;
import org.openrdf.rio.RDFHandlerException;


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

		SesameStatement stm = new SesameStatement(assertion);
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
			
			return new GraphQueryResultImpl(namespaces, results.iterator());
			
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
}
