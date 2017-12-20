package it.unibz.inf.ontop.owlapi;

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

import it.unibz.inf.ontop.injection.OntopSQLOWLAPIConfiguration;
import it.unibz.inf.ontop.owlapi.connection.OWLConnection;
import it.unibz.inf.ontop.owlapi.validation.QuestOWLEmptyEntitiesChecker;
import it.unibz.inf.ontop.spec.ontology.ClassifiedTBox;
import org.apache.commons.rdf.api.IRI;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.BufferedReader;
import java.io.FileReader;
import java.io.IOException;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

import static org.junit.Assert.assertEquals;

/**
 * Use the class EmptiesAboxCheck to test the return of empty concepts and
 * roles, based on the mappings. Given ontology, which is connected to a
 * database via mappings, generate a suitable set of queries that test if there
 * are empty concepts, concepts that are no populated to anything.
 */
public class QuestOWLEmptyEntitiesCheckerTest {

	private OWLConnection conn;
	private Connection connection;

	Logger log = LoggerFactory.getLogger(this.getClass());
	private ClassifiedTBox onto;

	final String owlfile = "src/test/resources/test/emptiesDatabase.owl";
	final String obdafile = "src/test/resources/test/emptiesDatabase.obda";

	// final String owlFileName =
	// "src/main/resources/testcases-scenarios/virtual-mode/stockexchange/simplecq/stockexchange.owl";
	// final String obdaFileName =
	// "src/main/resources/testcases-scenarios/virtual-mode/stockexchange/simplecq/stockexchange-mysql.obda";

	private List<IRI> emptyConcepts = new ArrayList<>();
	private List<IRI> emptyRoles = new ArrayList<>();

	private OntopOWLReasoner reasoner;

	@Before
	public void setUp() throws Exception {

		String url = "jdbc:h2:mem:questjunitdb;";
		String username = "sa";
		String password = "";

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
		onto = OWL2QLTranslatorTest.loadOntologyFromFileAndClassify(owlfile);

		// Creating a new instance of the reasoner
		OntopOWLFactory factory = OntopOWLFactory.defaultFactory();
        OntopSQLOWLAPIConfiguration config = OntopSQLOWLAPIConfiguration.defaultBuilder()
				.nativeOntopMappingFile(obdafile)
				.ontologyFile(owlfile)
				.jdbcUrl(url)
				.jdbcUser(username)
				.jdbcPassword(password)
				.enableTestMode()
				.build();
        reasoner = factory.createReasoner(config);
		// Now we are ready for querying
		conn = reasoner.getConnection();
	}

	@After
	public void tearDown() throws Exception {
			dropTables();
			reasoner.dispose();
			connection.close();
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
	 */
	@Test
	public void testEmptyConcepts() {

		QuestOWLEmptyEntitiesChecker empties = new QuestOWLEmptyEntitiesChecker(onto, conn);
		Iterator<IRI> iterator = empties.iEmptyConcepts();
		while (iterator.hasNext()){
			emptyConcepts.add(iterator.next());
		}

		log.info("Empty concept/s: " + emptyConcepts);
		assertEquals(1, emptyConcepts.size());
		assertEquals(1, empties.getEConceptsSize());
	}

	/**
	 * Test numbers of empty roles
	 */
	@Test
	public void testEmptyRoles() {
		QuestOWLEmptyEntitiesChecker empties = new QuestOWLEmptyEntitiesChecker(onto, conn);
		Iterator<IRI> iterator = empties.iEmptyRoles();
		while (iterator.hasNext()){
			emptyRoles.add(iterator.next());
		}

		log.info("Empty role/s: " + emptyRoles);
		assertEquals(2, emptyRoles.size());
		assertEquals(2, empties.getERolesSize());
	}
}
