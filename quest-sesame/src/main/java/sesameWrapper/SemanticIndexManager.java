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

import it.unibz.krdb.obda.ontology.Ontology;
import it.unibz.krdb.obda.owlapi3.OWLAPI3ABoxIterator;
import it.unibz.krdb.obda.owlrefplatform.core.abox.EquivalentTriplePredicateIterator;
import it.unibz.krdb.obda.owlrefplatform.core.abox.RDBMSSIRepositoryManager;
import it.unibz.krdb.obda.owlrefplatform.core.dagjgrapht.TBoxReasoner;
import it.unibz.krdb.obda.owlrefplatform.core.dagjgrapht.TBoxReasonerImpl;
import it.unibz.krdb.obda.owlrefplatform.owlapi3.QuestOWL;
import it.unibz.krdb.obda.sesame.SesameRDFIterator;

import java.io.BufferedReader;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.IOException;
import java.sql.Connection;
import java.sql.SQLException;

import org.openrdf.rio.RDFHandlerException;
import org.openrdf.rio.RDFParseException;
import org.openrdf.rio.turtle.TurtleParser;
import org.semanticweb.owlapi.model.OWLOntology;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/***
 * An utility to setup and maintain a semantic index repository independently
 * from a Quest instance.
 * 
 * @author mariano
 * 
 */
public class SemanticIndexManager {

	Connection conn = null;

	private TBoxReasoner reasoner;

	private RDBMSSIRepositoryManager dataRepository = null;

	private final Logger log = LoggerFactory.getLogger(this.getClass());

	public SemanticIndexManager(OWLOntology tbox, Connection connection) throws Exception {
		conn = connection;
		Ontology ontologyClosure = QuestOWL.loadOntologies(tbox);

		TBoxReasoner ontoReasoner = new TBoxReasonerImpl(ontologyClosure);
		// generate a new TBox with a simpler vocabulary
		reasoner = TBoxReasonerImpl.getEquivalenceSimplifiedReasoner(ontoReasoner);
			
		dataRepository = new RDBMSSIRepositoryManager();
		dataRepository.setTBox(reasoner);

		log.debug("TBox has been processed. Ready to ");
	}

	public void restoreRepository() throws SQLException {
		dataRepository.loadMetadata(conn);

		log.debug("Semantic Index metadata was found and restored from the DB");
	}

	public void setupRepository(boolean drop) throws SQLException {

		dataRepository.createDBSchema(conn, drop);
		dataRepository.insertMetadata(conn);

		log.debug("Semantic Index repository has been setup.");

	}
	
	public void dropRepository() throws SQLException {
		dataRepository.dropDBSchema(conn);
	}

	public void updateMetadata() throws SQLException {
		dataRepository.insertMetadata(conn);
		log.debug("Updated metadata in the repository");
	}

	public int insertData(OWLOntology ontology, int commitInterval, int batchSize) throws SQLException {

		OWLAPI3ABoxIterator aBoxIter = new OWLAPI3ABoxIterator(ontology.getOWLOntologyManager().getImportsClosure(ontology));
		EquivalentTriplePredicateIterator newData = new EquivalentTriplePredicateIterator(aBoxIter, reasoner);
		int result = dataRepository.insertData(conn, newData, commitInterval, batchSize);

		log.info("Loaded {} items into the DB.", result);

		return result;
	}

	public int insertDataNTriple(final String ntripleFile, final String baseURI, final int commitInterval, final int batchSize)
			throws SQLException, RDFParseException, RDFHandlerException, FileNotFoundException, IOException {

		final TurtleParser parser = new TurtleParser();
		// NTriplesParser parser = new NTriplesParser();

		final SesameRDFIterator aBoxIter = new SesameRDFIterator();
		
		parser.setRDFHandler(aBoxIter);
		
		final EquivalentTriplePredicateIterator newData = new EquivalentTriplePredicateIterator(aBoxIter, reasoner);


		Thread t = new Thread() {
			@Override
			public void run() {

				try {
					parser.parse(new BufferedReader(new FileReader(ntripleFile)), baseURI);
				} catch (RDFParseException e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				} catch (RDFHandlerException e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				} catch (FileNotFoundException e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				} catch (IOException e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				}
			}
		};

		final int[] val = new int[1];

		Thread t2 = new Thread() {

			int result;

			@Override
			public void run() {
				try {
					val[0] = dataRepository.insertData(conn, newData, commitInterval, batchSize);
				} catch (SQLException e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				}
				log.info("Loaded {} items into the DB.", result);

			}

		};

		t.start();
		t2.start();
		try {
			t.join();

			t2.join();
		} catch (InterruptedException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		// EquivalentTriplePredicateIterator newData = new
		// EquivalentTriplePredicateIterator(aBoxIter, equivalenceMaps);

		return val[0];
	}

}
