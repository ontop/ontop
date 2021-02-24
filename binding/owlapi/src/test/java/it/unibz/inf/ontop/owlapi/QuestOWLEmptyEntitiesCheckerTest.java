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
import it.unibz.inf.ontop.owlapi.validation.OntopOWLEmptyEntitiesChecker;
import it.unibz.inf.ontop.spec.ontology.ClassifiedTBox;
import org.apache.commons.rdf.api.IRI;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.sql.Connection;
import java.sql.DriverManager;
import java.util.ArrayList;
import java.util.List;

import static it.unibz.inf.ontop.utils.OWLAPITestingTools.executeFromFile;
import static org.junit.Assert.assertEquals;

/**
 * Use the class EmptiesAboxCheck to test the return of empty concepts and
 * roles, based on the mappings. Given ontology, which is connected to a
 * database via mappings, generate a suitable set of queries that test if there
 * are empty concepts, concepts that are no populated to anything.
 */
public class QuestOWLEmptyEntitiesCheckerTest {

	private final Logger log = LoggerFactory.getLogger(this.getClass());

	private static final String owlfile = "src/test/resources/test/emptiesDatabase.owl";
	private static final String obdafile = "src/test/resources/test/emptiesDatabase.obda";

	private ClassifiedTBox onto;
	private OntopOWLReasoner reasoner;
	private OWLConnection conn;
	private Connection connection;

	@Before
	public void setUp() throws Exception {

		String url = "jdbc:h2:mem:questjunitdb;";
		String username = "sa";
		String password = "";

		connection = DriverManager.getConnection(url, username, password);
		executeFromFile(connection, "src/test/resources/test/emptiesDatabase-h2.sql");

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
		executeFromFile(connection, "src/test/resources/test/emptiesDatabase-drop-h2.sql");
		reasoner.dispose();
		connection.close();
	}

	/**
	 * Test numbers of empty concepts
	 */
	@Test
	public void testEmptyConcepts() {
		OntopOWLEmptyEntitiesChecker empties = new OntopOWLEmptyEntitiesChecker(onto, conn);
		List<IRI> emptyConcepts = new ArrayList<>();
		for (IRI iri : empties.emptyClasses()){
			emptyConcepts.add(iri);
		}

		log.info("Empty concept/s: " + emptyConcepts);
		assertEquals(1, emptyConcepts.size());
	}

	/**
	 * Test numbers of empty roles
	 */
	@Test
	public void testEmptyRoles() {
		OntopOWLEmptyEntitiesChecker empties = new OntopOWLEmptyEntitiesChecker(onto, conn);
		List<IRI> emptyRoles = new ArrayList<>();
		for (IRI iri : empties.emptyProperties()) {
			emptyRoles.add(iri);
		}

		log.info("Empty role/s: " + emptyRoles);
		assertEquals(2, emptyRoles.size());
	}
}
