package it.unibz.inf.ontop.owlrefplatform.owlapi;

/*
 * #%L
 * ontop-quest-owlapi
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

import it.unibz.inf.ontop.exception.OntopConnectionException;
import it.unibz.inf.ontop.model.GraphResultSet;
import it.unibz.inf.ontop.model.TupleResultSet;
import it.unibz.inf.ontop.ontology.Assertion;
import it.unibz.inf.ontop.ontology.ClassAssertion;
import it.unibz.inf.ontop.ontology.DataPropertyAssertion;
import it.unibz.inf.ontop.ontology.ObjectPropertyAssertion;
import it.unibz.inf.ontop.owlapi.OWLAPIIndividualTranslator;
import it.unibz.inf.ontop.owlapi.OntopOWLException;
import it.unibz.inf.ontop.owlrefplatform.core.ExecutableQuery;
import it.unibz.inf.ontop.owlrefplatform.core.OntopStatement;
import it.unibz.inf.ontop.owlrefplatform.core.queryevaluation.SPARQLQueryUtility;
import org.semanticweb.owlapi.model.*;

import java.util.*;

/***
 * A Statement to execute queries over a QuestOWLConnection. The logic of this
 * statement is equivalent to that of JDBC's Statements.
 * 
 * <p>
 * <strong>Performance</strong> Note that you should not create multiple
 * statements over the same connection to execute parallel queries (see
 * {@link QuestOWLConnection}). Multiple statements over the same connection are
 * not going to be very useful until we support updates (then statements will
 * allow to implement transactions in the same way as JDBC Statements).
 * 
 * @author Mariano Rodriguez Muro <mariano.muro@gmail.com>
 *
 * Used by the OWLAPI.
 *
 * TODO: rename it (not now) QuestOWLStatementImpl.
 *
 */
// DISABLED TEMPORARILY FOR MERGING PURPOSES (NOT BREAKING CLIENTS WITH this ugly name IQquestOWLStatement)
//public class QuestOWLStatement implements IQuestOWLStatement {
public class QuestOWLStatement implements OntopOWLStatement {
	private OntopStatement st;
	private OntopOWLConnection conn;

	public QuestOWLStatement(OntopStatement st, OntopOWLConnection conn) {
		this.conn = conn;
		this.st = st;
	}

	public void cancel() throws OWLException {
		try {
			st.cancel();
		} catch (OntopConnectionException e) {
			throw new OntopOWLException(e);
		}
	}

	@Override
	public void close() throws OWLException {
		try {
			st.close();
		} catch (OntopConnectionException e) {
			throw new OntopOWLException(e);
		}
	}

	public QuestOWLResultSet executeTuple(String query) throws OWLException {
		if (SPARQLQueryUtility.isSelectQuery(query) || SPARQLQueryUtility.isAskQuery(query)) {
		try {
			TupleResultSet executedQuery = (TupleResultSet) st.execute(query);
			QuestOWLResultSet questOWLResultSet = new QuestOWLResultSet(executedQuery, this);

	 		
			return questOWLResultSet;
		} catch (Exception e) {
			throw new OntopOWLException(e);
		}} else {
			throw new OWLException("Query is not tuple query (SELECT / ASK).");
		}
	}

	public List<OWLAxiom> executeGraph(String query) throws OWLException {
		if (SPARQLQueryUtility.isConstructQuery(query) || SPARQLQueryUtility.isDescribeQuery(query)) {
		try {
			GraphResultSet resultSet = (GraphResultSet) st.execute(query);
			return createOWLIndividualAxioms(resultSet);
		} catch (Exception e) {
			throw new OWLOntologyCreationException(e);
		}} else {
			throw new OWLException("Query is not graph query (CONSTRUCT / DESCRIBE).");
		}
	}

	public OntopOWLConnection getConnection() throws OWLException {
		return conn;
	}

	public int getFetchSize() throws OWLException {
		try {
			return st.getFetchSize();
		} catch (OntopConnectionException e) {
			throw new OntopOWLException(e);
		}
	}

	public int getMaxRows() throws OWLException {
		try {
			return st.getMaxRows();
		} catch (OntopConnectionException e) {
			throw new OntopOWLException(e);
		}
	}

	public void getMoreResults() throws OWLException {
		try {
			st.getMoreResults();
		} catch (OntopConnectionException e) {
			throw new OntopOWLException(e);
		}
	}

	public int getQueryTimeout() throws OWLException {
		try {
			return st.getQueryTimeout();
		} catch (OntopConnectionException e) {
			throw new OntopOWLException(e);
		}
	}

	public void setFetchSize(int rows) throws OWLException {
		try {
			st.setFetchSize(rows);
		} catch (OntopConnectionException e) {
			throw new OntopOWLException(e);
		}
	}

	public void setMaxRows(int max) throws OWLException {
		try {
			st.setMaxRows(max);
		} catch (OntopConnectionException e) {
			throw new OntopOWLException(e);
		}
	}

	public boolean isClosed() throws OWLException {
		try {
			return st.isClosed();
		} catch (OntopConnectionException e) {
			throw new OntopOWLException(e);
		}
	}

	public void setQueryTimeout(int seconds) throws OWLException {
		try {
			st.setQueryTimeout(seconds);
		} catch (OntopConnectionException e) {
			throw new OntopOWLException(e);
		}
	}

	public long getTupleCount(String query) throws OWLException {
		try {
			return st.getTupleCount(query);
		} catch (Exception e) {
			throw new OntopOWLException(e);
		}
	}

	public String getRewriting(String query) throws OWLException {
		try {
			return st.getRewriting(query);
		} 
		catch (Exception e) {
			throw new OntopOWLException(e);
		}
	}

	public ExecutableQuery getExecutableQuery(String query) throws OWLException {
		return st.getExecutableQuery(query);
	}

	private List<OWLAxiom> createOWLIndividualAxioms(GraphResultSet resultSet) throws Exception {
		
		OWLAPIIndividualTranslator translator = new OWLAPIIndividualTranslator();
		
		List<OWLAxiom> axiomList = new ArrayList<OWLAxiom>();
		if (resultSet != null) {
			while (resultSet.hasNext()) {
				for (Assertion assertion : resultSet.next()) {
					if (assertion instanceof ClassAssertion) {
						OWLAxiom classAxiom = translator.translate((ClassAssertion) assertion);
						axiomList.add(classAxiom);
					} 
					else if (assertion instanceof ObjectPropertyAssertion) {
						OWLAxiom objectPropertyAxiom = translator.translate((ObjectPropertyAssertion) assertion);
						axiomList.add(objectPropertyAxiom);
					}
					else if (assertion instanceof DataPropertyAssertion) {
						OWLAxiom objectPropertyAxiom = translator.translate((DataPropertyAssertion) assertion);
						axiomList.add(objectPropertyAxiom);							
					} 
				}
			}
		}
		return axiomList;
	}
}
