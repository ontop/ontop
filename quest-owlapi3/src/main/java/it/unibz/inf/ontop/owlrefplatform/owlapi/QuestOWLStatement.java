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

import it.unibz.inf.ontop.answering.input.*;
import it.unibz.inf.ontop.exception.*;
import it.unibz.inf.ontop.model.BooleanResultSet;
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
 */
public class QuestOWLStatement implements OntopOWLStatement {
	private OntopStatement st;
	private final InputQueryFactory inputQueryFactory;
	private OntopOWLConnection conn;

	public QuestOWLStatement(OntopStatement st, OntopOWLConnection conn, InputQueryFactory inputQueryFactory) {
		this.conn = conn;
		this.st = st;
		this.inputQueryFactory = inputQueryFactory;
	}

	public void cancel() throws OntopOWLException {
		try {
			st.cancel();
		} catch (OntopConnectionException e) {
			throw new OntopOWLException(e);
		}
	}

	@Override
	public void close() throws OntopOWLException {
		try {
			st.close();
		} catch (OntopConnectionException e) {
			throw new OntopOWLException(e);
		}
	}

	@Override
	public QuestOWLResultSet executeSelectQuery(String inputQuery) throws OntopOWLException {
		try {
			SelectQuery query = inputQueryFactory.createSelectQuery(inputQuery);
			TupleResultSet resultSet = st.execute(query);

			return new QuestOWLResultSet(resultSet, this);

		} catch (OntopQueryEngineException e) {
			throw new OntopOWLException(e);
		}
	}

	@Override
	public QuestOWLResultSet executeAskQuery(String inputQuery) throws OntopOWLException {
		try {
			AskQuery query = inputQueryFactory.createAskQuery(inputQuery);
			BooleanResultSet resultSet = st.execute(query);

			return new QuestOWLResultSet(resultSet, this);

		} catch (OntopQueryEngineException e) {
			throw new OntopOWLException(e);
		}
	}

	@Override
	public QuestOWLResultSet executeTuple(String inputQuery) throws OntopOWLException {
		try {
			TupleSPARQLQuery<? extends TupleResultSet> query = inputQueryFactory.createTupleQuery(inputQuery);
			TupleResultSet resultSet = st.execute(query);

			return new QuestOWLResultSet(resultSet, this);

		} catch (OntopQueryEngineException e) {
			throw new OntopOWLException(e);
		}
	}

	@Override
	public List<OWLAxiom> executeConstructQuery(String inputQuery) throws OntopOWLException {
		try {
			ConstructQuery query = inputQueryFactory.createConstructQuery(inputQuery);
			return executeGraph(query);
		} catch (OntopQueryEngineException e) {
			throw new OntopOWLException(e);
		}
	}

	@Override
	public List<OWLAxiom> executeDescribeQuery(String inputQuery) throws OntopOWLException {
		try {
			DescribeQuery query = inputQueryFactory.createDescribeQuery(inputQuery);
			return executeGraph(query);
		} catch (OntopQueryEngineException e) {
			throw new OntopOWLException(e);
		}
	}

	@Override
	public List<OWLAxiom> executeGraph(String inputQuery) throws OntopOWLException {
		try {
			GraphSPARQLQuery query = inputQueryFactory.createGraphQuery(inputQuery);
			return executeGraph(query);
		} catch (OntopQueryEngineException e) {
			throw new OntopOWLException(e);
		}
	}

	private List<OWLAxiom> executeGraph(GraphSPARQLQuery query)
			throws OntopQueryEvaluationException, OntopConnectionException, OntopReformulationException,
			OntopResultConversionException {

		GraphResultSet resultSet = st.execute(query);
		return createOWLIndividualAxioms(resultSet);
	}

	public OntopOWLConnection getConnection() throws OntopOWLException {
		return conn;
	}

	public int getFetchSize() throws OntopOWLException {
		try {
			return st.getFetchSize();
		} catch (OntopConnectionException e) {
			throw new OntopOWLException(e);
		}
	}

	public int getMaxRows() throws OntopOWLException {
		try {
			return st.getMaxRows();
		} catch (OntopConnectionException e) {
			throw new OntopOWLException(e);
		}
	}

	public void getMoreResults() throws OntopOWLException {
		try {
			st.getMoreResults();
		} catch (OntopConnectionException e) {
			throw new OntopOWLException(e);
		}
	}

	public int getQueryTimeout() throws OntopOWLException {
		try {
			return st.getQueryTimeout();
		} catch (OntopConnectionException e) {
			throw new OntopOWLException(e);
		}
	}

	public void setFetchSize(int rows) throws OntopOWLException {
		try {
			st.setFetchSize(rows);
		} catch (OntopConnectionException e) {
			throw new OntopOWLException(e);
		}
	}

	public void setMaxRows(int max) throws OntopOWLException {
		try {
			st.setMaxRows(max);
		} catch (OntopConnectionException e) {
			throw new OntopOWLException(e);
		}
	}

	public boolean isClosed() throws OntopOWLException {
		try {
			return st.isClosed();
		} catch (OntopConnectionException e) {
			throw new OntopOWLException(e);
		}
	}

	public void setQueryTimeout(int seconds) throws OntopOWLException {
		try {
			st.setQueryTimeout(seconds);
		} catch (OntopConnectionException e) {
			throw new OntopOWLException(e);
		}
	}

	public long getTupleCount(String query) throws OntopOWLException {
		try {
			return st.getTupleCount(parseQueryString(query));
		} catch (Exception e) {
			throw new OntopOWLException(e);
		}
	}

	public String getRewritingRendering(String query) throws OntopOWLException {
		try {
			return st.getRewritingRendering(parseQueryString(query));
		} 
		catch (Exception e) {
			throw new OntopOWLException(e);
		}
	}

	public ExecutableQuery getExecutableQuery(String query) throws OntopOWLException {
		try {
			return st.getExecutableQuery(parseQueryString(query));
		} catch (OntopReformulationException e) {
			throw new OntopOWLException(e);
		}
	}

	private List<OWLAxiom> createOWLIndividualAxioms(GraphResultSet resultSet)
			throws OntopConnectionException, OntopResultConversionException {
		
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

	/**
	 * In contexts where we don't know the precise type
	 */
	private InputQuery parseQueryString(String queryString) throws OntopOWLException {
		try {
			return inputQueryFactory.createSPARQLQuery(queryString);
		} catch (OntopInvalidInputQueryException | OntopUnsupportedInputQueryException e) {
			throw new OntopOWLException(e);
		}
	}

}
