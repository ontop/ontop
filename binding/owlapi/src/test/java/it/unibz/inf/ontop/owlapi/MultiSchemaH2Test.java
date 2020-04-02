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
import it.unibz.inf.ontop.owlapi.connection.OWLStatement;
import it.unibz.inf.ontop.owlapi.resultset.TupleOWLResultSet;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;

import java.io.File;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;
import java.util.Scanner;

import static junit.framework.TestCase.assertTrue;

/***
 * A simple test that check if the system is able to handle Mappings for
 * classes/roles and attributes even if there are no URI templates. i.e., the
 * database stores URI's directly.
 * 
 * We are going to create an H2 DB, the .sql file is fixed. We will map directly
 * there and then query on top.
 */
public class MultiSchemaH2Test  {


    static final String owlFile =
            "src/test/resources/multischema/multischemah2.owl";
    static final String obdaFile =
            "src/test/resources/multischema/multischemah2.obda";

	private OntopOWLReasoner reasoner;
	private OWLConnection conn;
	private String url = "jdbc:h2:mem:questrepository";
	private String username =  "fish";
	private String password = "fish";

	Connection sqlConnection;

	@Before
	public void setUp() throws Exception {

		sqlConnection = DriverManager.getConnection(url,username, password);
		java.sql.Statement s = sqlConnection.createStatement();

		try {
			String text = new Scanner( new File("src/test/resources/multischema/stockexchange-h2Schema.sql") ).useDelimiter("\\A").next();
			s.execute(text);
		}
		catch(SQLException sqle) {
			System.out.println("Exception in creating db from script");
		}

		s.close();

		OntopSQLOWLAPIConfiguration config = OntopSQLOWLAPIConfiguration.defaultBuilder()
				.ontologyFile(owlFile)
				.nativeOntopMappingFile(obdaFile)
				.jdbcUrl(url)
				.jdbcUser(username)
				.jdbcPassword(password)
				.enableTestMode()
				.build();

		/*
		 * Create the instance of Quest OWL reasoner.
		 */
		OntopOWLFactory factory = OntopOWLFactory.defaultFactory();

		reasoner = factory.createReasoner(config);
		conn = reasoner.getConnection();



	}

	@After
	public void tearDown() throws Exception {
		try {
			dropTables();
			conn.close();
		} catch (Exception e) {
			System.out.println(e.getMessage());
		}
	}

	private void dropTables() throws Exception {

		conn.close();
		reasoner.dispose();
		if (!sqlConnection.isClosed()) {
			java.sql.Statement s = sqlConnection.createStatement();
			try {
				s.execute("DROP ALL OBJECTS DELETE FILES");
			} catch (SQLException sqle) {
				System.out.println("Table not found, not dropping");
			} finally {
				s.close();
				sqlConnection.close();
			}
		}
	}

	/**
	 * Test use of two aliases to same table
	 * @throws Exception
	 */
	@Test
	public void testOneSchema() throws Exception {
		String query = "PREFIX : <http://www.owl-ontologies.com/Ontology1207768242.owl#> SELECT ?x WHERE {?x a :Address}";
		checkThereIsAtLeastOneResult(query);
	}
	@Test
	public void testTableOneSchema() throws Exception {
		String query = "PREFIX : <http://www.owl-ontologies.com/Ontology1207768242.owl#> SELECT ?x WHERE {?x a :Broker}";
		checkThereIsAtLeastOneResult(query);
	}

	@Test
	public void testAliasOneSchema() throws Exception {
		String query = "PREFIX : <http://www.owl-ontologies.com/Ontology1207768242.owl#> SELECT ?x WHERE {?x a :Worker}";
		checkThereIsAtLeastOneResult(query);
	}

	@Test
	public void testSchemaWhere() throws Exception {
		String query = "PREFIX : <http://www.owl-ontologies.com/Ontology1207768242.owl#> SELECT ?x ?r WHERE { ?x :isBroker ?r }";
		checkThereIsAtLeastOneResult(query);
	}

	@Test
	public void testMultischema() throws Exception {
		String query = "PREFIX : <http://www.owl-ontologies.com/Ontology1207768242.owl#> SELECT ?x WHERE { ?x :hasFile ?r }";
		checkThereIsAtLeastOneResult(query);
	}

	private void checkThereIsAtLeastOneResult(String query) throws Exception {
		OWLStatement st = conn.createStatement();
		try {
			TupleOWLResultSet rs = st.executeSelectQuery(query);
			assertTrue(rs.hasNext());

		} catch (Exception e) {
			throw e;
		} finally {
			try {

			} catch (Exception e) {
				st.close();
				assertTrue(false);
			}
			conn.close();
			reasoner.dispose();
		}
	}
}
