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
import it.unibz.inf.ontop.owlapi.resultset.OWLBindingSet;
import it.unibz.inf.ontop.owlapi.resultset.TupleOWLResultSet;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.semanticweb.owlapi.model.OWLException;
import org.semanticweb.owlapi.model.OWLLiteral;

import java.io.File;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;
import java.util.Scanner;

import static junit.framework.TestCase.assertEquals;
import static junit.framework.TestCase.assertTrue;

/***
 * Test use of  VALUES ?sx { <http://www.safepec.org#Ship>}
 * and  use of FILTER (?sx = <http://www.safepec.org#Ship> )
 */
public class H2SimpleFilterAndValuesTest {
	final String owlFile = "src/test/resources/filter/datatypes.owl";
	final String obdaFile = "src/test/resources/filter/filter-h2.obda";
	private static final String JDBC_URL =  "jdbc:h2:mem:datatype";
	private static final String JDBC_USER =  "sa";
	private static final String JDBC_PASSWORD =  "";

	private OntopOWLReasoner reasoner;
	private OWLConnection conn;
	Connection sqlConnection;


	@Before
	public void setUp() throws Exception {

		sqlConnection = DriverManager.getConnection(JDBC_URL,JDBC_USER, JDBC_PASSWORD);
		java.sql.Statement s = sqlConnection.createStatement();

		try {
			String text = new Scanner( new File("src/test/resources/filter/h2-datatypes.sql") ).useDelimiter("\\A").next();
			s.execute(text);
			//Server.startWebServer(sqlConnection);

		} catch(SQLException sqle) {
			System.out.println("Exception in creating db from script");
		}

		s.close();

		OntopSQLOWLAPIConfiguration config = OntopSQLOWLAPIConfiguration.defaultBuilder()
				.ontologyFile(owlFile)
				.nativeOntopMappingFile(obdaFile)
				.jdbcUrl(JDBC_URL)
				.jdbcUser(JDBC_USER)
				.jdbcPassword(JDBC_PASSWORD)
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


	private String runQueryReturnLiteral(String query) throws OWLException, SQLException {
		OWLStatement st = conn.createStatement();
		String retval;
		try {
			TupleOWLResultSet rs = st.executeSelectQuery(query);

			assertTrue(rs.hasNext());
            final OWLBindingSet bindingSet = rs.next();
            OWLLiteral ind1 = bindingSet.getOWLLiteral("y");
			retval = ind1.toString();

		} catch (Exception e) {
			throw e;
		} finally {
			conn.close();
			reasoner.dispose();
		}
		return retval;
	}



    /**
	 * Test use of Filter and Values with class or property
	 * @throws Exception
	 */


	@Test
	public void testFilterClass() throws Exception {
		String query =  "PREFIX : <http://ontop.inf.unibz.it/test/datatypes#> SELECT ?x ?y\n" +
				"WHERE {\n" +
				"   ?z a ?x; :hasDate ?y\n" +
				"   FILTER ( ?x = :Row ) .\n" +
				"}";
		String val = runQueryReturnLiteral(query);
		assertEquals("\"2013-03-18\"^^xsd:date", val);
	}

	@Test
	public void testValuesClass() throws Exception {
		String query =  "PREFIX : <http://ontop.inf.unibz.it/test/datatypes#> SELECT ?x ?y\n" +
				"WHERE {\n" +
				"   ?z a ?x; :hasDate ?y\n" +
				"   VALUES ?x { :Row } .\n" +
				"}";
		String val = runQueryReturnLiteral(query);
		assertEquals("\"2013-03-18\"^^xsd:date", val);
	}

	@Test
	public void testFilterProperty() throws Exception {
		String query =  "PREFIX : <http://ontop.inf.unibz.it/test/datatypes#> SELECT ?x ?y\n" +
				"WHERE {\n" +
				"   ?z a :Row; ?x ?y\n" +
				"   FILTER ( ?x = :hasDate ) .\n" +
				"}";
		String val = runQueryReturnLiteral(query);
		assertEquals("\"2013-03-18\"^^xsd:date", val);
	}

	@Test
	public void testValuesProperty() throws Exception {
		String query =  "PREFIX : <http://ontop.inf.unibz.it/test/datatypes#> SELECT ?x ?y\n" +
				"WHERE {\n" +
				"   ?z a :Row; ?x ?y\n" +
				"   VALUES ?x { :hasDate } .\n" +
				"}";
		String val = runQueryReturnLiteral(query);
		assertEquals("\"2013-03-18\"^^xsd:date", val);
	}


	@Test
	public void testValuesProperty2() throws Exception {
		String query = "PREFIX : <http://ontop.inf.unibz.it/test/datatypes#> SELECT * WHERE"
				+ "{ ?x a  :Row .  ?x ?v ?y . VALUES ?v { :hasSmallInt } }";


		String val = runQueryReturnLiteral(query);
		assertEquals("\"1\"^^xsd:integer", val);
	}
}

