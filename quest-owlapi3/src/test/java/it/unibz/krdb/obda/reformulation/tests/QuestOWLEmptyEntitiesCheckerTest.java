package it.unibz.krdb.obda.reformulation.tests;

/*
 * #%L
 * ontop-test
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

import static org.junit.Assert.assertEquals;
import it.unibz.krdb.obda.io.ModelIOManager;
import it.unibz.krdb.obda.model.OBDADataFactory;
import it.unibz.krdb.obda.model.OBDAModel;
import it.unibz.krdb.obda.model.Predicate;
import it.unibz.krdb.obda.model.impl.OBDADataFactoryImpl;
import it.unibz.krdb.obda.owlrefplatform.core.QuestConstants;
import it.unibz.krdb.obda.owlrefplatform.core.QuestPreferences;
import it.unibz.krdb.obda.owlrefplatform.owlapi3.QuestOWLEmptyEntitiesChecker;
import it.unibz.krdb.obda.owlrefplatform.owlapi3.QuestOWL;
import it.unibz.krdb.obda.owlrefplatform.owlapi3.QuestOWLConnection;
import it.unibz.krdb.obda.owlrefplatform.owlapi3.QuestOWLFactory;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.io.IOException;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.ArrayList;
import java.util.List;

import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.semanticweb.owlapi.apibinding.OWLManager;
import org.semanticweb.owlapi.model.OWLOntology;
import org.semanticweb.owlapi.model.OWLOntologyManager;
import org.semanticweb.owlapi.reasoner.SimpleConfiguration;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Use the class EmptiesAboxCheck to test the return of empty concepts and
 * roles, based on the mappings. Given ontology, which is connected to a
 * database via mappings, generate a suitable set of queries that test if there
 * are empty concepts, concepts that are no populated to anything.
 */
public class QuestOWLEmptyEntitiesCheckerTest {

	private OBDADataFactory fac;
	private QuestOWLConnection conn;
	private Connection connection;

	Logger log = LoggerFactory.getLogger(this.getClass());
	private OBDAModel obdaModel;
	private OWLOntology ontology;

	final String owlfile = "src/test/resources/test/emptiesDatabase.owl";
	final String obdafile = "src/test/resources/test/emptiesDatabase.obda";

	// final String owlfile =
	// "src/main/resources/testcases-scenarios/virtual-mode/stockexchange/simplecq/stockexchange.owl";
	// final String obdafile =
	// "src/main/resources/testcases-scenarios/virtual-mode/stockexchange/simplecq/stockexchange-mysql.obda";

	private List<Predicate> emptyConcepts = new ArrayList<Predicate>();
	private List<Predicate> emptyRoles = new ArrayList<Predicate>();

	private QuestOWL reasoner;

	@Before
	public void setUp() throws Exception {

		String driver = "org.h2.Driver";
		String url = "jdbc:h2:mem:questjunitdb;";
		String username = "sa";
		String password = "";

		fac = OBDADataFactoryImpl.getInstance();

		connection = DriverManager.getConnection(url, username, password);
		Statement st = connection.createStatement();

		FileReader reader = new
				FileReader("src/test/resources/test/emptiesDatabase-h2.sql");
		BufferedReader in = new BufferedReader(reader);
		StringBuilder bf = new StringBuilder();
		String line = in.readLine();
		while (line != null) {
			bf.append(line);
			line = in.readLine();
		}

		st.executeUpdate(bf.toString());
		connection.commit();

		// Loading the OWL file
		OWLOntologyManager manager = OWLManager.createOWLOntologyManager();
		ontology = manager.loadOntologyFromOntologyDocument((new File(owlfile)));

		// Loading the OBDA data
		fac = OBDADataFactoryImpl.getInstance();
		obdaModel = fac.getOBDAModel();

		ModelIOManager ioManager = new ModelIOManager(obdaModel);
		ioManager.load(obdafile);

		QuestPreferences p = new QuestPreferences();
		p.setCurrentValueOf(QuestPreferences.ABOX_MODE, QuestConstants.VIRTUAL);
		p.setCurrentValueOf(QuestPreferences.OBTAIN_FULL_METADATA, QuestConstants.FALSE);
		// Creating a new instance of the reasoner
		QuestOWLFactory factory = new QuestOWLFactory();
		factory.setOBDAController(obdaModel);

		factory.setPreferenceHolder(p);

		reasoner = (QuestOWL) factory.createReasoner(ontology, new SimpleConfiguration());

		// Now we are ready for querying
		conn = reasoner.getConnection();

	}

	@After
	public void tearDown() throws Exception {
		try {
			dropTables();
			reasoner.dispose();
			connection.close();
		} catch (Exception e) {
			log.debug(e.getMessage());
		}

	}

	private void dropTables() throws SQLException, IOException {

		Statement st = connection.createStatement();

		FileReader reader = new FileReader("src/test/resources/test/emptiesDatabase-drop-h2.sql");
		BufferedReader in = new BufferedReader(reader);
		StringBuilder bf = new StringBuilder();
		String line = in.readLine();
		while (line != null) {
			bf.append(line);
			line = in.readLine();
		}

		st.executeUpdate(bf.toString());
		st.close();
		connection.commit();
	}

	/**
	 * Test numbers of empty concepts
	 * 
	 * @throws Exception
	 */
	@Test
	public void testEmptyConcepts() throws Exception {

		QuestOWLEmptyEntitiesChecker empties = new QuestOWLEmptyEntitiesChecker(ontology, conn);
		emptyConcepts = empties.getEmptyConcepts();
		log.info("Empty concept/s: " + emptyConcepts);
		assertEquals(1, emptyConcepts.size());

	}

	/**
	 * Test numbers of empty roles
	 * 
	 * @throws Exception
	 */
	@Test
	public void testEmptyRoles() throws Exception {
		QuestOWLEmptyEntitiesChecker empties = new QuestOWLEmptyEntitiesChecker(ontology, conn);
		emptyRoles = empties.getEmptyRoles();
		log.info("Empty role/s: " + emptyRoles);
		assertEquals(2, emptyRoles.size());

	}

	/**
	 * Test numbers of empty concepts and roles
	 * 
	 * @throws Exception
	 */
	@Test
	public void testEmpties() throws Exception {

		QuestOWLEmptyEntitiesChecker empties = new QuestOWLEmptyEntitiesChecker(ontology, conn);
		emptyConcepts = empties.getEmptyConcepts();
		log.info(empties.toString());
		log.info("Empty concept/s: " + emptyConcepts);
		assertEquals(1, emptyConcepts.size());
		emptyRoles = empties.getEmptyRoles();
		log.info("Empty role/s: " + emptyRoles);
		assertEquals(2, emptyRoles.size());

	}

}
