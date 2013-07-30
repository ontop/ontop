/*
 * Copyright (C) 2009-2013, Free University of Bozen Bolzano
 * This source code is available under the terms of the Affero General Public
 * License v3.
 * 
 * Please see LICENSE.txt for full license terms, including the availability of
 * proprietary exceptions.
 */
package it.unibz.krdb.obda.owlrefplatform.owlapi3;

import it.unibz.krdb.obda.model.GraphResultSet;
import it.unibz.krdb.obda.model.OBDAException;
import it.unibz.krdb.obda.model.Predicate;
import it.unibz.krdb.obda.model.TupleResultSet;
import it.unibz.krdb.obda.ontology.Assertion;
import it.unibz.krdb.obda.ontology.ClassAssertion;
import it.unibz.krdb.obda.ontology.DataPropertyAssertion;
import it.unibz.krdb.obda.ontology.Description;
import it.unibz.krdb.obda.ontology.ObjectPropertyAssertion;
import it.unibz.krdb.obda.owlapi3.OWLAPI3ABoxIterator;
import it.unibz.krdb.obda.owlapi3.OWLConnection;
import it.unibz.krdb.obda.owlapi3.OWLResultSet;
import it.unibz.krdb.obda.owlapi3.OWLStatement;
import it.unibz.krdb.obda.owlrefplatform.core.QuestStatement;
import it.unibz.krdb.obda.owlrefplatform.core.translator.SparqlAlgebraToDatalogTranslator;
import it.unibz.krdb.obda.sesame.SesameRDFIterator;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.io.Reader;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;
import java.util.Set;

import org.openrdf.rio.RDFFormat;
import org.openrdf.rio.RDFParser;
import org.openrdf.rio.Rio;
import org.semanticweb.owlapi.apibinding.OWLManager;
import org.semanticweb.owlapi.model.IRI;
import org.semanticweb.owlapi.model.OWLAxiom;
import org.semanticweb.owlapi.model.OWLClass;
import org.semanticweb.owlapi.model.OWLClassAssertionAxiom;
import org.semanticweb.owlapi.model.OWLDataFactory;
import org.semanticweb.owlapi.model.OWLDataProperty;
import org.semanticweb.owlapi.model.OWLDataPropertyAssertionAxiom;
import org.semanticweb.owlapi.model.OWLException;
import org.semanticweb.owlapi.model.OWLIndividual;
import org.semanticweb.owlapi.model.OWLLiteral;
import org.semanticweb.owlapi.model.OWLObjectProperty;
import org.semanticweb.owlapi.model.OWLObjectPropertyAssertionAxiom;
import org.semanticweb.owlapi.model.OWLOntology;
import org.semanticweb.owlapi.model.OWLOntologyCreationException;
import org.semanticweb.owlapi.model.OWLOntologyManager;

import com.hp.hpl.jena.query.Query;
import com.hp.hpl.jena.query.QueryFactory;

public class QuestOWLStatement implements OWLStatement {

	private QuestStatement st;
	private QuestOWLConnection conn;

	public QuestOWLStatement(QuestStatement st, QuestOWLConnection conn) {
		this.conn = conn;
		this.st = st;
	}
	
	
	
	public QuestStatement getQuestStatement() {
		return st;
	}

	@Override
	public void cancel() throws OWLException {
		try {
			st.cancel();
		} catch (OBDAException e) {
			OWLException owlException = new OWLException(e) {
			};
			owlException.setStackTrace(e.getStackTrace());
			throw owlException;
		}
	}

	@Override
	public void close() throws OWLException {
		try {
			st.close();
		} catch (OBDAException e) {
			OWLException owlException = new OWLException(e) {
			};
			owlException.setStackTrace(e.getStackTrace());
			throw owlException;
		}
	}

	@Override
	public OWLResultSet executeTuple(String query) throws OWLException {
		try {
			return new QuestOWLResultSet((TupleResultSet) st.execute(query), this);
		} catch (OBDAException e) {
			OWLException owlException = new OWLException(e) {
			};
			owlException.setStackTrace(e.getStackTrace());
			throw owlException;
		}
	}
	
	public void createIndexes() throws Exception {
		st.createIndexes();
	}
	
	public void dropIndexes() throws Exception {
		st.dropIndexes();

	}

	@Override
	public List<OWLAxiom> executeGraph(String query) throws OWLException {
		try {
			GraphResultSet resultSet = (GraphResultSet) st.execute(query);
			return createOWLIndividualAxioms(resultSet);
		} catch (Exception e) {
			throw new OWLOntologyCreationException(e);
		}
	}
	
	@Override
	public int executeUpdate(String query) throws OWLException {
		try {
			return st.executeUpdate(query);
		} catch (OBDAException e) {
			OWLException owlException = new OWLException(e) {
			};
			owlException.setStackTrace(e.getStackTrace());
			throw owlException;
		}
	}
	
	public int insertData(File owlFile, int commitSize, int batchsize) throws Exception {
		int ins = insertData(owlFile, commitSize, batchsize, null);
		return ins;
	}
	
	public int insertData(File owlFile, int commitSize, int batchsize, String baseURI) throws Exception {

		Iterator<Assertion> aBoxIter = null;

		if (owlFile.getName().toLowerCase().endsWith(".owl")) {
			OWLOntologyManager manager = OWLManager.createOWLOntologyManager();
			OWLOntology ontology = manager
					.loadOntologyFromOntologyDocument(owlFile);
			Set<OWLOntology> set = manager.getImportsClosure(ontology);

			// Retrieves the ABox from the ontology file.

			aBoxIter = new OWLAPI3ABoxIterator(set,
					new HashMap<Predicate, Description>());
			return st.insertData(aBoxIter, commitSize, batchsize);
		} else if (owlFile.getName().toLowerCase().endsWith(".ttl")
				|| owlFile.getName().toLowerCase().endsWith(".nt")) {

			RDFParser rdfParser = null;
			
			if (owlFile.getName().toLowerCase().endsWith(".nt") ) {
				rdfParser = Rio.createParser(RDFFormat.NTRIPLES);
			} else if (owlFile.getName().toLowerCase().endsWith(".ttl")) {
				rdfParser = Rio.createParser(RDFFormat.TURTLE);
			}

			rdfParser.setVerifyData(true);
			rdfParser.setStopAtFirstError(true);
			rdfParser.setDatatypeHandling(RDFParser.DatatypeHandling.IGNORE);

			boolean autoCommit = conn.getAutoCommit();
			conn.setAutoCommit(false);

			SesameRDFIterator rdfHandler = new SesameRDFIterator();
			rdfParser.setRDFHandler(rdfHandler);
			
			BufferedReader reader = new BufferedReader(new FileReader(owlFile));

			try {
				


				Thread insert = new Thread(new Insert(rdfParser,
						reader, baseURI));
				Process processor = new Process(rdfHandler, this.st, commitSize, batchsize);
				Thread process = new Thread(processor);

				// start threads
				insert.start();
				process.start();

				insert.join();
				process.join();

				return processor.getInsertCount();
				

			} catch (RuntimeException e) {
				// System.out.println("exception, rolling back!");

				if (autoCommit) {
					conn.rollBack();
				}
				throw e;
			} catch (OBDAException e) {

				if (autoCommit) {
					conn.rollBack();
				}
				throw e;
				} catch (InterruptedException e) {
				if (autoCommit) {
					conn.rollBack();
				}

				throw e;
			} finally {
				conn.setAutoCommit(autoCommit);				
			}

		} else {
			throw new IllegalArgumentException(
					"Only .owl, .ttl and .nt files are supported for load opertions.");
		}

	}

	
	private class Insert implements Runnable {
		private RDFParser rdfParser;
		private Reader inputStreamOrReader;
		private String baseURI;

		public Insert(RDFParser rdfParser, Reader inputStreamOrReader,
				String baseURI) {
			this.rdfParser = rdfParser;
			this.inputStreamOrReader = inputStreamOrReader;
			this.baseURI = baseURI;
		}

		public void run() {
			try {
				rdfParser.parse(inputStreamOrReader, baseURI);
			} catch (Exception e) {
				throw new RuntimeException(e);
			}
		}

	}

	private class Process implements Runnable {
		private SesameRDFIterator iterator;
		private QuestStatement questStmt;
		
		int insertCount = -1;
		private int commitsize;
		private int batchsize;

		public Process(SesameRDFIterator iterator, QuestStatement qstm, int commitsize, int batchsize)
				throws OBDAException {
			this.iterator = iterator;
			this.questStmt = qstm;
			this.commitsize = commitsize;
			this.batchsize = batchsize;
		}

		public void run() {
			try {
				insertCount = questStmt.insertData(iterator, commitsize, batchsize);
			} catch (SQLException e) {
				throw new RuntimeException(e);
			}
		}
		
		public int getInsertCount() {
			return insertCount;
		}
	}
	
	@Override
	public OWLConnection getConnection() throws OWLException {
		return conn;
	}

	@Override
	public int getFetchSize() throws OWLException {
		try {
			return st.getFetchSize();
		} catch (OBDAException e) {
			OWLException owlException = new OWLException(e) {
			};
			owlException.setStackTrace(e.getStackTrace());
			throw owlException;
		}
	}

	@Override
	public int getMaxRows() throws OWLException {
		try {
			return st.getMaxRows();
		} catch (OBDAException e) {
			OWLException owlException = new OWLException(e) {
			};
			owlException.setStackTrace(e.getStackTrace());
			throw owlException;
		}
	}

	@Override
	public void getMoreResults() throws OWLException {
		try {
			st.cancel();
		} catch (OBDAException e) {
			OWLException owlException = new OWLException(e) {
			};
			owlException.setStackTrace(e.getStackTrace());
			throw owlException;
		}
	}

	@Override
	public OWLResultSet getResultSet() throws OWLException {
		try {
			return new QuestOWLResultSet(st.getResultSet(), this);
		} catch (OBDAException e) {
			OWLException owlException = new OWLException(e) {
			};
			owlException.setStackTrace(e.getStackTrace());
			throw owlException;
		}
	}

	@Override
	public int getQueryTimeout() throws OWLException {
		try {
			return st.getQueryTimeout();
		} catch (OBDAException e) {
			OWLException owlException = new OWLException(e) {
			};
			owlException.setStackTrace(e.getStackTrace());
			throw owlException;
		}
	}

	@Override
	public void setFetchSize(int rows) throws OWLException {
		try {
			st.setFetchSize(rows);
		} catch (OBDAException e) {
			OWLException owlException = new OWLException(e) {
			};
			owlException.setStackTrace(e.getStackTrace());
			throw owlException;
		}
	}

	@Override
	public void setMaxRows(int max) throws OWLException {
		try {
			st.setMaxRows(max);
		} catch (OBDAException e) {
			OWLException owlException = new OWLException(e) {
			};
			owlException.setStackTrace(e.getStackTrace());
			throw owlException;
		}
	}

	@Override
	public boolean isClosed() throws OWLException {
		try {
			return st.isClosed();
		} catch (OBDAException e) {
			OWLException owlException = new OWLException(e) {
			};
			owlException.setStackTrace(e.getStackTrace());
			throw owlException;
		}
	}

	@Override
	public void setQueryTimeout(int seconds) throws Exception {
		try {
			st.setQueryTimeout(seconds);
		} catch (OBDAException e) {
			OWLException owlException = new OWLException(e) {
			};
			owlException.setStackTrace(e.getStackTrace());
			throw owlException;
		}
	}

	@Override
	public int getTupleCount(String query) throws OWLException {
		try {
			return st.getTupleCount(query);
		} catch (Exception e) {
			OWLException owlException = new OWLException(e) {
			};
			owlException.setStackTrace(e.getStackTrace());
			throw owlException;
		}
	}

	public String getRewriting(String query) throws OWLException {
		try {
			Query jenaquery = QueryFactory.create(query);
			SparqlAlgebraToDatalogTranslator tr = new SparqlAlgebraToDatalogTranslator(this.st.questInstance.getUriTemplateMatcher());
			
			LinkedList<String> signatureContainer = new LinkedList<String>();
			tr.getSignature(jenaquery, signatureContainer);
			return st.getRewriting(jenaquery, signatureContainer);
		} catch (Exception e) {
			OWLException owlException = new OWLException(e) {
			};
			owlException.setStackTrace(e.getStackTrace());
			throw owlException;
		}
	}

	public String getUnfolding(String query) throws OWLException {
		try {
			return st.getUnfolding(query);
		} catch (Exception e) {
			OWLException owlException = new OWLException(e) {
			};
			owlException.setStackTrace(e.getStackTrace());
			throw owlException;
		}
	}
		
	private List<OWLAxiom> createOWLIndividualAxioms(GraphResultSet resultSet) throws Exception {
		OWLOntologyManager manager = OWLManager.createOWLOntologyManager();
		OWLDataFactory factory = manager.getOWLDataFactory();
		List<OWLAxiom> axiomList = new ArrayList<OWLAxiom>();
		if (resultSet != null) {
		while (resultSet.hasNext()) {
			for (Assertion assertion : resultSet.next()) {
				if (assertion instanceof ClassAssertion) {
					String subjectIRI = ((ClassAssertion) assertion).getPredicate().toString();
					String classIRI = ((ClassAssertion) assertion).getObject().getValue();
					OWLAxiom classAxiom = createOWLClassAssertion(classIRI, subjectIRI, factory);
					axiomList.add(classAxiom);
				} else if (assertion instanceof ObjectPropertyAssertion) {
					String propertyIRI = ((ObjectPropertyAssertion) assertion).getPredicate().toString();
					String subjectIRI = ((ObjectPropertyAssertion) assertion).getFirstObject().getValue();
					String objectIRI = ((ObjectPropertyAssertion) assertion).getSecondObject().getValue();
					OWLAxiom objectPropertyAxiom = createOWLObjectPropertyAssertion(propertyIRI, subjectIRI, objectIRI, factory);
					axiomList.add(objectPropertyAxiom);
				}  else if (assertion instanceof DataPropertyAssertion) {
					String propertyIRI = ((DataPropertyAssertion) assertion).getPredicate().toString();
					String subjectIRI = ((DataPropertyAssertion) assertion).getObject().getValue();
					String objectValue = ((DataPropertyAssertion) assertion).getValue().getValue();
					OWLAxiom dataPropertyAxiom = createOWLDataPropertyAssertion(propertyIRI, subjectIRI, objectValue, factory);
					axiomList.add(dataPropertyAxiom);
				}
			}
		}		
		}
		return axiomList;
	}
	
	private OWLClassAssertionAxiom createOWLClassAssertion(String classIRI, String subjectIRI, OWLDataFactory factory) {
		OWLClass classExpression = factory.getOWLClass(IRI.create(classIRI));
		OWLIndividual individual = factory.getOWLNamedIndividual(IRI.create(subjectIRI));
		return factory.getOWLClassAssertionAxiom(classExpression, individual);
	}
	
	private OWLObjectPropertyAssertionAxiom createOWLObjectPropertyAssertion(String propertyIRI, String subjectIRI, String objectIRI, OWLDataFactory factory) {
		OWLObjectProperty propertyExpression = factory.getOWLObjectProperty(IRI.create(propertyIRI));
		OWLIndividual individual1 = factory.getOWLNamedIndividual(IRI.create(subjectIRI));
		OWLIndividual individual2 = factory.getOWLNamedIndividual(IRI.create(objectIRI));
		return factory.getOWLObjectPropertyAssertionAxiom(propertyExpression, individual1, individual2);
	}
	
	private OWLDataPropertyAssertionAxiom createOWLDataPropertyAssertion(String propertyIRI, String subjectIRI, String objectValue, OWLDataFactory factory) {
		OWLDataProperty propertyExpression = factory.getOWLDataProperty(IRI.create(propertyIRI));
		OWLIndividual individual1 = factory.getOWLNamedIndividual(IRI.create(subjectIRI));
		OWLLiteral individual2 = factory.getOWLLiteral(objectValue);
		return factory.getOWLDataPropertyAssertionAxiom(propertyExpression, individual1, individual2);
	}



	public void analyze() throws Exception {
		st.analyze();
		
	}
}
