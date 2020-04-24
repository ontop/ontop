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

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;

import static it.unibz.inf.ontop.utils.OWLAPITestingTools.executeFromFile;
import static junit.framework.TestCase.assertEquals;
import static junit.framework.TestCase.assertTrue;

/***
 * Test use of  VALUES ?sx { <http://www.safepec.org#Ship>}
 * and  use of FILTER (?sx = <http://www.safepec.org#Ship> )
 */
public class H2SimpleFilterAndValuesTest {

	private static final String owlFile = "src/test/resources/filter/datatypes.owl";
	private static final String obdaFile = "src/test/resources/filter/filter-h2.obda";
	private static final String JDBC_URL =  "jdbc:h2:mem:datatype";
	private static final String JDBC_USER =  "sa";
	private static final String JDBC_PASSWORD =  "";

	private OntopOWLReasoner reasoner;
	private OWLConnection conn;
	private Connection sqlConnection;

	@Before
	public void setUp() throws Exception {

		sqlConnection = DriverManager.getConnection(JDBC_URL,JDBC_USER, JDBC_PASSWORD);
		executeFromFile(sqlConnection, "src/test/resources/filter/h2-datatypes.sql");

		OntopSQLOWLAPIConfiguration config = OntopSQLOWLAPIConfiguration.defaultBuilder()
				.ontologyFile(owlFile)
				.nativeOntopMappingFile(obdaFile)
				.jdbcUrl(JDBC_URL)
				.jdbcUser(JDBC_USER)
				.jdbcPassword(JDBC_PASSWORD)
				.enableTestMode()
				.build();

		OntopOWLFactory factory = OntopOWLFactory.defaultFactory();
		reasoner = factory.createReasoner(config);
		conn = reasoner.getConnection();
	}

	@After
	public void tearDown() throws Exception {
		conn.close();
		reasoner.dispose();
		if (!sqlConnection.isClosed()) {
			try (java.sql.Statement s = sqlConnection.createStatement()) {
				s.execute("DROP ALL OBJECTS DELETE FILES");
			}
			finally {
				sqlConnection.close();
			}
		}
	}


	private String runQueryReturnLiteral(String query) throws OWLException, SQLException {
		try (OWLStatement st = conn.createStatement()) {
			TupleOWLResultSet rs = st.executeSelectQuery(query);
			assertTrue(rs.hasNext());
            final OWLBindingSet bindingSet = rs.next();
            OWLLiteral ind1 = bindingSet.getOWLLiteral("y");
			String retval = ind1.toString();
			return retval;
		}
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

