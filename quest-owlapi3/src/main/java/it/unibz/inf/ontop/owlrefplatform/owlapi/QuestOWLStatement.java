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

import it.unibz.inf.ontop.model.GraphResultSet;
import it.unibz.inf.ontop.model.OBDAException;
import it.unibz.inf.ontop.model.TupleResultSet;
import it.unibz.inf.ontop.ontology.Assertion;
import it.unibz.inf.ontop.ontology.ClassAssertion;
import it.unibz.inf.ontop.ontology.DataPropertyAssertion;
import it.unibz.inf.ontop.ontology.ObjectPropertyAssertion;
import it.unibz.inf.ontop.owlapi.OWLAPIABoxIterator;
import it.unibz.inf.ontop.owlapi.OWLAPIIndividualTranslator;
import it.unibz.inf.ontop.owlapi.OntopOWLException;
import it.unibz.inf.ontop.owlrefplatform.core.ExecutableQuery;
import it.unibz.inf.ontop.owlrefplatform.core.IQuestStatement;
import it.unibz.inf.ontop.owlrefplatform.core.SQLExecutableQuery;
import it.unibz.inf.ontop.owlrefplatform.core.queryevaluation.SPARQLQueryUtility;
import it.unibz.inf.ontop.rdf4j.RDF4JRDFIterator;
import org.eclipse.rdf4j.query.parser.ParsedQuery;
import org.eclipse.rdf4j.rio.ParserConfig;
import org.eclipse.rdf4j.rio.RDFFormat;
import org.eclipse.rdf4j.rio.RDFParser;
import org.eclipse.rdf4j.rio.Rio;
import org.eclipse.rdf4j.rio.helpers.BasicParserSettings;
import org.semanticweb.owlapi.apibinding.OWLManager;
import org.semanticweb.owlapi.model.*;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.io.Reader;
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
public class QuestOWLStatement implements IQuestOWLStatement {
	private IQuestStatement st;
	private QuestOWLConnection conn;

	public QuestOWLStatement(IQuestStatement st, QuestOWLConnection conn) {
		this.conn = conn;
		this.st = st;
	}

	public void cancel() throws OWLException {
		try {
			st.cancel();
		} catch (OBDAException e) {
			throw new OntopOWLException(e);
		}
	}

	@Override
	public void close() throws OWLException {
		try {
			st.close();
		} catch (OBDAException e) {
			throw new OntopOWLException(e);
		}
	}

	public QuestOWLResultSet executeTuple(String query) throws OWLException {
		if (SPARQLQueryUtility.isSelectQuery(query) || SPARQLQueryUtility.isAskQuery(query)) {
		try {
			TupleResultSet executedQuery = (TupleResultSet) st.execute(query);
			QuestOWLResultSet questOWLResultSet = new QuestOWLResultSet(executedQuery, this);

	 		
			return questOWLResultSet;
		} catch (OBDAException e) {
			throw new OntopOWLException(e);
		}} else {
			throw new RuntimeException("Query is not tuple query (SELECT / ASK).");
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
			throw new RuntimeException("Query is not graph query (CONSTRUCT / DESCRIBE).");
		}
	}

	public int executeUpdate(String query) throws OWLException {
		try {
			return st.executeUpdate(query);
		} catch (OBDAException e) {
			throw new OntopOWLException(e);
		}
	}

	public int insertData(File owlFile, int commitSize, int batchsize, String baseURI) throws Exception {

		Iterator<Assertion> aBoxIter = null;

		if (owlFile.getName().toLowerCase().endsWith(".owl")) {
			OWLOntologyManager manager = OWLManager.createOWLOntologyManager();
			OWLOntology ontology = manager.loadOntologyFromOntologyDocument(owlFile);
			Set<OWLOntology> set = manager.getImportsClosure(ontology);

			// Retrieves the ABox from the ontology file.

			aBoxIter = new OWLAPIABoxIterator(set, st.getQuestInstance().getVocabulary());
			return st.insertData(aBoxIter, commitSize, batchsize);
		} 
		else if (owlFile.getName().toLowerCase().endsWith(".ttl") || owlFile.getName().toLowerCase().endsWith(".nt")) {

			RDFParser rdfParser = null;

			if (owlFile.getName().toLowerCase().endsWith(".nt")) {
				rdfParser = Rio.createParser(RDFFormat.NTRIPLES);
			} else if (owlFile.getName().toLowerCase().endsWith(".ttl")) {
				rdfParser = Rio.createParser(RDFFormat.TURTLE);
			}

			ParserConfig config = rdfParser.getParserConfig();
			// To emulate DatatypeHandling.IGNORE 
			config.addNonFatalError(BasicParserSettings.FAIL_ON_UNKNOWN_DATATYPES);
			config.addNonFatalError(BasicParserSettings.VERIFY_DATATYPE_VALUES);
			config.addNonFatalError(BasicParserSettings.NORMALIZE_DATATYPE_VALUES);
//
//			rdfParser.setVerifyData(true);
//			rdfParser.setStopAtFirstError(true);

			boolean autoCommit = conn.getAutoCommit();
			conn.setAutoCommit(false);

			RDF4JRDFIterator rdfHandler = new RDF4JRDFIterator();
			rdfParser.setRDFHandler(rdfHandler);

			BufferedReader reader = new BufferedReader(new FileReader(owlFile));

			try {

				Thread insert = new Thread(new Insert(rdfParser, reader, baseURI));
				Process processor = new Process(rdfHandler, this.st, commitSize, batchsize);
				Thread process = new Thread(processor);

				// start threads
				insert.start();
				process.start();

				insert.join();
				process.join();

				return processor.getInsertCount();

			} catch (RuntimeException | InterruptedException e) {
				// System.out.println("exception, rolling back!");

				if (autoCommit) {
					conn.rollBack();
				}
				throw e;
			} finally {
				conn.setAutoCommit(autoCommit);
			}

		} else {
			throw new IllegalArgumentException("Only .owl, .ttl and .nt files are supported for load opertions.");
		}

	}

	private class Insert implements Runnable {
		private RDFParser rdfParser;
		private Reader inputStreamOrReader;
		private String baseURI;

		public Insert(RDFParser rdfParser, Reader inputStreamOrReader, String baseURI) {
			this.rdfParser = rdfParser;
			this.inputStreamOrReader = inputStreamOrReader;
			this.baseURI = baseURI;
		}

		@Override
		public void run() {
			try {
				rdfParser.parse(inputStreamOrReader, baseURI);
			} catch (Exception e) {
				throw new RuntimeException(e);
			}
		}

	}

	private class Process implements Runnable {
		private Iterator<Assertion> iterator;
		private IQuestStatement questStmt;

		int insertCount = -1;
		private int commitsize;
		private int batchsize;

		public Process(Iterator<Assertion> iterator, IQuestStatement qstm, int commitsize, int batchsize) throws OBDAException {
			this.iterator = iterator;
			this.questStmt = qstm;
			this.commitsize = commitsize;
			this.batchsize = batchsize;
		}

		@Override
		public void run() {
			try {
				insertCount = questStmt.insertData(iterator, commitsize, batchsize);
			} catch (OBDAException e) {
				throw new RuntimeException(e);
			}
		}

		public int getInsertCount() {
			return insertCount;
		}
	}

	public QuestOWLConnection getConnection() throws OWLException {
		return conn;
	}

	public int getFetchSize() throws OWLException {
		try {
			return st.getFetchSize();
		} catch (OBDAException e) {
			throw new OntopOWLException(e);
		}
	}

	public int getMaxRows() throws OWLException {
		try {
			return st.getMaxRows();
		} catch (OBDAException e) {
			throw new OntopOWLException(e);
		}
	}

	public void getMoreResults() throws OWLException {
		try {
			st.getMoreResults();
		} catch (OBDAException e) {
			throw new OntopOWLException(e);
		}
	}

	public QuestOWLResultSet getResultSet() throws OWLException {
		try {
			return new QuestOWLResultSet(st.getResultSet(), this);
		} catch (OBDAException e) {
			throw new OntopOWLException(e);
		}
	}

	public int getQueryTimeout() throws OWLException {
		try {
			return st.getQueryTimeout();
		} catch (OBDAException e) {
			throw new OntopOWLException(e);
		}
	}

	public void setFetchSize(int rows) throws OWLException {
		try {
			st.setFetchSize(rows);
		} catch (OBDAException e) {
			throw new OntopOWLException(e);
		}
	}

	public void setMaxRows(int max) throws OWLException {
		try {
			st.setMaxRows(max);
		} catch (OBDAException e) {
			throw new OntopOWLException(e);
		}
	}

	public boolean isClosed() throws OWLException {
		try {
			return st.isClosed();
		} catch (OBDAException e) {
			throw new OntopOWLException(e);
		}
	}

	public void setQueryTimeout(int seconds) throws Exception {
		try {
			st.setQueryTimeout(seconds);
		} catch (OBDAException e) {
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
			ParsedQuery pq = st.getQuestInstance().getEngine().getParsedQuery(query);
			return st.getQuestInstance().getEngine().getRewriting(pq);
		} 
		catch (Exception e) {
			throw new OntopOWLException(e);
		}
	}

	/**
	 * SQL-specific! Please use getExecutableQuery() instead!
	 */
	@Deprecated
	public String getUnfolding(String query) throws OWLException {
		ExecutableQuery executableQuery = getExecutableQuery(query);
		if (executableQuery instanceof SQLExecutableQuery) {
			return ((SQLExecutableQuery) executableQuery).getSQL();
		}
		else {
			throw new RuntimeException("This deprecated method (getUnfolding) presumes the use of SQL as a native query language. " +
					"Please use getExecutableQuery() instead.");
		}
	}

	public ExecutableQuery getExecutableQuery(String query) throws OWLException {
		try {
			ParsedQuery pq = st.getQuestInstance().getEngine().getParsedQuery(query);
			return st.getQuestInstance().getEngine().translateIntoNativeQuery(pq, Optional.empty());
		} catch (Exception e) {
			throw new OntopOWLException(e);
		}
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
