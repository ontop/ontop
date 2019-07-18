package it.unibz.inf.ontop.reformulation.tests;

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

import it.unibz.inf.ontop.answering.resultset.MaterializedGraphResultSet;
import it.unibz.inf.ontop.injection.OntopSQLOWLAPIConfiguration;
import it.unibz.inf.ontop.materialization.OntopRDFMaterializer;
import it.unibz.inf.ontop.spec.ontology.Assertion;
import it.unibz.inf.ontop.spec.ontology.ClassAssertion;
import it.unibz.inf.ontop.spec.ontology.ObjectPropertyAssertion;
import junit.framework.TestCase;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.io.IOException;
import java.net.URISyntaxException;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;
import java.sql.Statement;

public class OntopOntologyMaterializerTest extends TestCase {

	private Connection jdbcconn = null;

	private static final Logger LOGGER =  LoggerFactory.getLogger(OntopOntologyMaterializerTest.class);

	String url = "jdbc:h2:mem:questjunitdb";
	String username = "sa";
	String password = "";

	public OntopOntologyMaterializerTest() {
    }

	@Override
	public void setUp() throws Exception {
		createTables();
	}

	private String readSQLFile(String file) throws IOException {
		BufferedReader reader = new BufferedReader(new FileReader(new File(file)));
		StringBuilder bf = new StringBuilder();
		String line = reader.readLine();
		while (line != null) {
			bf.append(line + "\n");
			line = reader.readLine();
		}
		return bf.toString();
	}

	private void createTables() throws IOException, SQLException, URISyntaxException {
		String createDDL = readSQLFile("src/test/resources/materializer/createMaterializeTest.sql");

		// Initializing and H2 database with the data		
		// String driver = "org.h2.Driver";
		
		jdbcconn = DriverManager.getConnection(url, username, password);
		Statement st = jdbcconn.createStatement();

		st.executeUpdate(createDDL);
		jdbcconn.commit();
	}

	@Override
	public void tearDown() throws Exception {

		dropTables();
//		conn.close();
		jdbcconn.close();
	}

	private void dropTables() throws SQLException, IOException {
		String dropDDL = readSQLFile("src/test/resources/materializer/dropMaterializeTest.sql");
		Statement st = jdbcconn.createStatement();
		st.executeUpdate(dropDDL);
		st.close();
		jdbcconn.commit();
	}

	public void testDataWithModel() throws Exception {
	
	    File f = new File("src/test/resources/materializer/MaterializeTest.obda");

		OntopSQLOWLAPIConfiguration configuration = OntopSQLOWLAPIConfiguration.defaultBuilder()
				.ontologyFile("src/test/resources/materializer/MaterializeTest.owl")
				.nativeOntopMappingFile(f)
				.jdbcUrl(url)
				.jdbcUser(username)
				.jdbcPassword(password)
				.enableTestMode()
				.build();
		
		OntopRDFMaterializer materializer = OntopRDFMaterializer.defaultMaterializer(configuration);

		try (MaterializedGraphResultSet resultSet = materializer.materialize()) {
			int classAss = 0, propAss = 0, objAss = 0;

			LOGGER.debug("Assertions:");
			while (resultSet.hasNext()) {
				Assertion assertion = resultSet.next();
				LOGGER.debug(assertion.toString());

				if (assertion instanceof ClassAssertion)
					classAss++;

				else if (assertion instanceof ObjectPropertyAssertion)
					objAss++;

				else // DataPropertyAssertion
					propAss++;
			}
			assertEquals(6, classAss); //2 classes * 3 data rows for T1
			assertEquals(40, propAss); //2 properties * 7 tables * 3 data rows each T2-T8 - 2 redundant
			assertEquals(3, objAss); //3 data rows for T9
		}
	}
	
	public void testDataWithModelAndOnto() throws Exception {

		// read model
		File f = new File("src/test/resources/materializer/MaterializeTest.obda");

		OntopSQLOWLAPIConfiguration configuration = OntopSQLOWLAPIConfiguration.defaultBuilder()
				.ontologyFile("src/test/resources/materializer/MaterializeTest.owl")
				.nativeOntopMappingFile(f)
				.jdbcUrl(url)
				.jdbcUser(username)
				.jdbcPassword(password)
				.enableTestMode()
				.build();

		// read onto
		// OWLOntology ontology = configuration.loadProvidedInputOntology();
		// Ontology onto = OWLAPITranslatorOWL2QL.translate(
		//		ontology.getOWLOntologyManager().getImportsClosure(ontology));
		// System.out.println(onto.tbox().getSubClassAxioms());
		// System.out.println(onto.tbox().getSubObjectPropertyAxioms());
		// System.out.println(onto.tbox().getSubDataPropertyAxioms());

		OntopRDFMaterializer materializer = OntopRDFMaterializer.defaultMaterializer(configuration);
		try (MaterializedGraphResultSet resultSet = materializer.materialize()) {

			int classAss = 0, propAss = 0, objAss = 0;
			while (resultSet.hasNext()) {
				Assertion assertion = resultSet.next();
				LOGGER.debug(assertion + "\n");
				if (assertion instanceof ClassAssertion)
					classAss++;

				else if (assertion instanceof ObjectPropertyAssertion)
					objAss++;

				else // DataPropertyAssertion
					propAss++;
			}
			assertEquals(6, classAss); //3 data rows x2 for subclass prop
			assertEquals(40, propAss); //8 tables * 3 data rows each x2 for subclass - 2 redundant
			assertEquals(3, objAss); //3 since no subprop for obj prop
		}
	}
}
