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
import it.unibz.inf.ontop.utils.SQLScriptRunner;
import org.junit.*;
import org.semanticweb.owlapi.model.OWLObject;

import java.io.BufferedReader;
import java.io.FileReader;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.Statement;

/***
 * A simple test that check if the system is able to handle Mappings for
 * classes/roles and attributes even if there are no URI templates. i.e., the
 * database stores URI's directly.
 * 
 * We are going to create an H2 DB, the .sql file is fixed. We will map directly
 * there and then query on top.
 */
public class ReverseURIH2Test {

	private static OWLConnection conn;
	private static OntopOWLReasoner reasoner;
	private static Connection sqlConnection;

	private static final String owlfile = "src/test/resources/reverseuri/reverse-uri-test.owl";
	private static final String obdafile = "src/test/resources/reverseuri/reverse-uri-test.obda";

	private static final String url = "jdbc:h2:mem:questrepository;";
	private static final String username = "fish";
	private static final String password = "fish";

	@BeforeClass
	public static void setUp() throws Exception {
		sqlConnection = DriverManager
				.getConnection(url, username, password);

		FileReader reader = new FileReader("src/test/resources/reverseuri/reverse-uri-test.sql");
		BufferedReader in = new BufferedReader(reader);
		SQLScriptRunner runner = new SQLScriptRunner(sqlConnection, true, false);
		runner.runScript(in);

		// Creating a new instance of the reasoner
		OntopOWLFactory factory = OntopOWLFactory.defaultFactory();
		OntopSQLOWLAPIConfiguration config = OntopSQLOWLAPIConfiguration.defaultBuilder()
				.nativeOntopMappingFile(obdafile)
				.ontologyFile(owlfile)
				.jdbcUrl(url)
				.jdbcUser(username)
				.jdbcPassword(password)
				.build();
		reasoner = factory.createReasoner(config);
		conn = reasoner.getConnection();
	}

	@AfterClass
	public static void tearDown() throws Exception {

		FileReader reader = new FileReader("src/test/resources/reverseuri/reverse-uri-test.sql.drop");
		BufferedReader in = new BufferedReader(reader);
		SQLScriptRunner runner = new SQLScriptRunner(sqlConnection, true, false);
		runner.runScript(in);

		conn.close();
		reasoner.dispose();
		if (!sqlConnection.isClosed()) {
			try (Statement s = sqlConnection.createStatement()) {
				s.execute("DROP ALL OBJECTS DELETE FILES");
			}
			finally {
				sqlConnection.close();
			}
		}
	}

	private void runTests(String query, int numberOfResults) throws Exception {
		try (OWLStatement st = conn.createStatement()) {

			TupleOWLResultSet rs = st.executeSelectQuery(query);
			/*
			 * boolean hasNext = rs.hasNext();
			 */
			// assertTrue(rs.hasNext());
			int count = 0;
			while (rs.hasNext()) {
				final OWLBindingSet bindingSet = rs.next();
				OWLObject ind1 = bindingSet.getOWLObject("x");
				System.out.println("Result " + ind1.toString());
				count += 1;
			}
			Assert.assertEquals(numberOfResults, count);

			/*
			 * assertEquals("<uri1>", ind1.toString()); assertEquals("<uri1>",
			 * ind2.toString()); assertEquals("\"value1\"", val.toString());
			 */

		}
	}

	/**
	 * Test use of two aliases to same table
	 * 
	 * @throws Exception
	 */
	@Test
	public void testSingleColum2() throws Exception {
		String query = "PREFIX : <http://www.ontop.org/> SELECT ?x WHERE {<http://www.ontop.org/test-Cote%20D%27ivore> a ?x}";
		runTests(query, 1);
	}

	/**
	 * Test use of two aliases to same table
	 * 
	 * @throws Exception
	 */
	@Test
	public void testSingleColum() throws Exception {
		String query = "PREFIX : <http://www.ontop.org/> SELECT ?x WHERE {<http://www.ontop.org/test-John%20Smith> a ?x}";
		runTests(query, 1);
	}

	/**
	 * Test use of two aliases to same table
	 * 
	 * @throws Exception
	 */
	@Test
	public void testTwoColum2() throws Exception {
		String query = "PREFIX : <http://www.ontop.org/> SELECT ?x WHERE {<http://www.ontop.org/test-Cote%20D%27ivore-Cote%20D%27ivore> a ?x}";
		runTests(query, 1);
	}

	/**
	 * Test use of two aliases to same table
	 * 
	 * @throws Exception
	 */
	@Test
	public void testTwoColum2Value() throws Exception {
		String query = "PREFIX : <http://www.ontop.org/> SELECT ?x WHERE {<http://www.ontop.org/test-John%20Smith-John%20Smith%202> a ?x}";
		runTests(query, 1);
	}

	/**
	 * Test use of two aliases to same table
	 * 
	 * @throws Exception
	 */
	@Test
	public void testTwoColum22Vaule() throws Exception {
		String query = "PREFIX : <http://www.ontop.org/> SELECT ?x WHERE {<http://www.ontop.org/test-Cote%20D%27ivore-Cote%20D%27ivore%202> a ?x}";
		runTests(query, 1);
	}

	/**
	 * Test use of two aliases to same table
	 * 
	 * @throws Exception
	 */
	@Test
	public void testTwoColum() throws Exception {
		String query = "PREFIX : <http://www.ontop.org/> SELECT ?x WHERE {<http://www.ontop.org/test-John%20Smith-John%20Smith> a ?x}";
		runTests(query, 1);
	}

	/**
	 * Test use of two aliases to same table
	 * 
	 * @throws Exception
	 */
	@Test
	public void testThreeColum2() throws Exception {
		String query = "PREFIX : <http://www.ontop.org/> SELECT ?x WHERE {<http://www.ontop.org/test-Cote%20D%27ivore-Cote%20D%27ivore-Cote%20D%27ivore> a ?x}";
		runTests(query, 1);
	}

	/**
	 * Test use of two aliases to same table
	 * 
	 * @throws Exception
	 */
	@Test
	public void testThreeColum() throws Exception {
		String query = "PREFIX : <http://www.ontop.org/> SELECT ?x WHERE {<http://www.ontop.org/test-John%20Smith-John%20Smith-John%20Smith> a ?x}";
		runTests(query, 1);
	}

	/**
	 * Test use of two aliases to same table
	 * 
	 * @throws Exception
	 */
	@Test
	public void testThreeColum23Value() throws Exception {
		String query = "PREFIX : <http://www.ontop.org/> SELECT ?x WHERE {<http://www.ontop.org/test-Cote%20D%27ivore-Cote%20D%27ivore%202-Cote%20D%27ivore%203> a ?x}";
		runTests(query, 1);
	}

	/**
	 * Test use of two aliases to same table
	 * 
	 * @throws Exception
	 */
	@Test
	public void testThreeColum3Value() throws Exception {
		String query = "PREFIX : <http://www.ontop.org/> SELECT ?x WHERE {<http://www.ontop.org/test-John%20Smith-John%20Smith%202-John%20Smith%203> a ?x}";
		runTests(query, 1);
	}

}
