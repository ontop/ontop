package it.unibz.inf.ontop.owlapi.si;

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

import it.unibz.inf.ontop.si.repository.SIRepositoryManager;
import it.unibz.inf.ontop.spec.ontology.ImmutableOntologyVocabulary;
import it.unibz.inf.ontop.spec.ontology.Ontology;
import it.unibz.inf.ontop.owlapi.utils.OWLAPIABoxIterator;
import it.unibz.inf.ontop.si.repository.impl.RDBMSSIRepositoryManager;
import it.unibz.inf.ontop.spec.ontology.TBoxReasoner;
import it.unibz.inf.ontop.spec.ontology.impl.TBoxReasonerImpl;
import it.unibz.inf.ontop.owlapi.impl.QuestOWL;
import org.semanticweb.owlapi.model.OWLOntology;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.sql.Connection;
import java.sql.SQLException;

/***
 * An utility to setup and maintain a semantic index repository independently
 * from a Quest instance.
 * 
 * @author mariano
 * 
 */
public class SemanticIndexManager {

	private final Connection conn;

	private final TBoxReasoner reasoner;
	private final ImmutableOntologyVocabulary voc;

	private final SIRepositoryManager dataRepository;

	private final Logger log = LoggerFactory.getLogger(this.getClass());

	public SemanticIndexManager(OWLOntology tbox, Connection connection) throws Exception {
		conn = connection;
		Ontology ontologyClosure = QuestOWL.loadOntologies(tbox);
		voc = ontologyClosure.getVocabulary();

		reasoner = TBoxReasonerImpl.create(ontologyClosure, true);
			
		dataRepository = new RDBMSSIRepositoryManager(reasoner, ontologyClosure.getVocabulary());
		dataRepository.generateMetadata(); // generate just in case

		log.debug("TBox has been processed. Ready to ");
	}

	public void restoreRepository() throws SQLException {
		dataRepository.loadMetadata(conn);

		log.debug("Semantic Index metadata was found and restored from the DB");
	}

	public void setupRepository(boolean drop) throws SQLException {

		if (drop) {
			log.debug("Droping existing tables");
			try {
				dataRepository.dropDBSchema(conn);
			}
			catch (SQLException e) {
				log.debug(e.getMessage(), e);
			}
		}

		dataRepository.createDBSchemaAndInsertMetadata(conn);

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

		OWLAPIABoxIterator aBoxIter = new OWLAPIABoxIterator(ontology.getOWLOntologyManager().getImportsClosure(ontology), voc);
		int result = dataRepository.insertData(conn, aBoxIter, commitInterval, batchSize);

		log.info("Loaded {} items into the DB.", result);

		return result;
	}

}
