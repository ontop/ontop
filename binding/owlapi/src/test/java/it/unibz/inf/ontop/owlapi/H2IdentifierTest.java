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
import org.semanticweb.owlapi.model.OWLIndividual;

import java.io.File;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;
import java.util.Scanner;

import static junit.framework.TestCase.assertEquals;
import static junit.framework.TestCase.assertTrue;

/***
 * Tests that oracle identifiers for tables and columns are treated
 * correctly. Especially, that the unquoted identifers are treated as uppercase, and
 * that the case of quoted identifiers is not changed
 */
public class H2IdentifierTest {

	static final String owlFile = "src/test/resources/identifiers/identifiers.owl";
	static final String obdaFile = "src/test/resources/identifiers/identifiers-h2.obda";
	static final String propertyFile = "src/test/resources/identifiers/identifiers-h2.properties";

	private OntopOWLReasoner reasoner;
	private OWLConnection conn;
	Connection sqlConnection;


	@Before
	public void setUp() throws Exception {

		sqlConnection = DriverManager.getConnection("jdbc:h2:mem:countries","sa", "");
		java.sql.Statement s = sqlConnection.createStatement();

		try {
			String text = new Scanner( new File("src/test/resources/identifiers/create-h2.sql") ).useDelimiter("\\A").next();
			s.execute(text);
			//Server.startWebServer(sqlConnection);

		} catch(SQLException sqle) {
			System.out.println("Exception in creating db from script");
		}

		s.close();

		OntopSQLOWLAPIConfiguration config = OntopSQLOWLAPIConfiguration.defaultBuilder()
				.ontologyFile(owlFile)
				.nativeOntopMappingFile(obdaFile)
				.propertyFile(propertyFile)
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
	 * Test use of lowercase, unquoted table, schema and column identifiers (also in target)
	 * @throws Exception
	 */
	@Test
	public void testLowercaseUnquoted() throws Exception {
		String query = "PREFIX : <http://www.semanticweb.org/ontologies/2013/7/untitled-ontology-150#> SELECT ?x WHERE {?x a :Country} ORDER BY ?x";
		String val = runQueryReturnIndividual(query);
		assertEquals("<http://www.semanticweb.org/ontologies/2013/7/untitled-ontology-150#Country-Argentina>", val);
	}


	/**
	 * Test use of lowercase, unquoted table and column identifiers (also in target) with uppercase table identifiers
	 * @throws Exception
	 */
	@Test
	public void testUpperCaseTableUnquoted() throws Exception {
		String query = "PREFIX : <http://www.semanticweb.org/ontologies/2013/7/untitled-ontology-150#> SELECT ?x WHERE {?x a :Country2} ORDER BY ?x";
		String val =  runQueryReturnIndividual(query);
		assertEquals("<http://www.semanticweb.org/ontologies/2013/7/untitled-ontology-150#Country2-Argentina>", val);
	}
	
	/**
	 * Test use of lowercase, quoted alias in a view definition 
	 * @throws Exception
	 */
	@Test
	public void testLowerCaseColumnViewDefQuoted() throws Exception {
		String query = "PREFIX : <http://www.semanticweb.org/ontologies/2013/7/untitled-ontology-150#> SELECT ?x WHERE {?x a :Country4} ORDER BY ?x";
		String val =  runQueryReturnIndividual(query);
		assertEquals("<http://www.semanticweb.org/ontologies/2013/7/untitled-ontology-150#Country4-1010>", val);
	}

	/**
	 * Test use of lowercase, unquoted alias in a view definition 
	 * @throws Exception
	 */
	@Test
	public void testLowerCaseColumnViewDefUnquoted() throws Exception {
		String query = "PREFIX : <http://www.semanticweb.org/ontologies/2013/7/untitled-ontology-150#> SELECT ?x WHERE {?x a :Country5} ORDER BY ?x";
		String val =  runQueryReturnIndividual(query);
		assertEquals("<http://www.semanticweb.org/ontologies/2013/7/untitled-ontology-150#Country5-1010>", val);
	}
	
	/**
	 * Test access to lowercase table name, mixed case column name, and constant alias 
	 * @throws Exception
	 */
	@Test
	public void testLowerCaseTable() throws Exception {
		String query = "PREFIX : <http://www.semanticweb.org/ontologies/2013/7/untitled-ontology-150#> SELECT ?x WHERE {?x a :Country3} ORDER BY ?x";
		String val =  runQueryReturnIndividual(query);
		assertEquals("<http://www.semanticweb.org/ontologies/2013/7/untitled-ontology-150#Country3-BladeRunner-2020-Constant>", val);
	}

	private String runQueryReturnIndividual(String query) throws OWLException, SQLException {
		OWLStatement st = conn.createStatement();
		String retval;
		try {
			TupleOWLResultSet rs = st.executeSelectQuery(query);

			assertTrue(rs.hasNext());
            final OWLBindingSet bindingSet = rs.next();
            OWLIndividual ind1 = bindingSet.getOWLIndividual("x");
			retval = ind1.toString();

		} catch (Exception e) {
			throw e;
		} finally {
			conn.close();
			reasoner.dispose();
		}
		return retval;
	}

	@Test
	public void testLowerCaseTableWithSymbol() throws Exception {
		String query = "PREFIX : <http://www.semanticweb.org/ontologies/2013/7/untitled-ontology-150#> SELECT ?x WHERE {?x a :NoCountry} ORDER BY ?x";
		String val =  runQueryReturnIndividual(query);
		assertEquals("<http://www.semanticweb.org/ontologies/2013/7/untitled-ontology-150#CountryNo-Atlantis>", val);
	}
}

