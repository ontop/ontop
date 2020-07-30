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
import org.semanticweb.owlapi.io.ToStringRenderer;
import org.semanticweb.owlapi.model.OWLObject;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.sql.Connection;
import java.sql.DriverManager;
import java.util.ArrayList;

import static it.unibz.inf.ontop.utils.OWLAPITestingTools.executeFromFile;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;

/***
 * Test use of canonical iri in h2 simple database on wellbores
 */
public class H2NoDuplicatesCanonicalIRITest {

	private final Logger log = LoggerFactory.getLogger(this.getClass());

	private static final String owlfile = "src/test/resources/sameAs/wellbores-same-as-can.owl";
	private static final String obdafile = "src/test/resources/sameAs/wellbores-same-as-can.obda";

	private OntopOWLReasoner reasoner;
	private Connection sqlConnection;
	private OWLConnection conn;

	private static final String JDBC_URL =  "jdbc:h2:mem:wellboresNoDuplicates";
	private static final String JDBC_USER =  "sa";
	private static final String JDBC_PASSWORD =  "";

	@Before
	public void setUp() throws Exception {

		sqlConnection = DriverManager.getConnection(JDBC_URL,JDBC_USER, JDBC_PASSWORD);
		executeFromFile(sqlConnection,"src/test/resources/sameAs/wellbores-same-as-can.sql");

		OntopSQLOWLAPIConfiguration config = OntopSQLOWLAPIConfiguration.defaultBuilder()
				.ontologyFile(owlfile)
				.nativeOntopMappingFile(obdafile)
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
	public void tearDown() throws Exception{
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

	private ArrayList<String> runTests(String query) throws Exception {
		try (OWLStatement st = conn.createStatement()) {
			ArrayList<String> retVal = new ArrayList<>();
			TupleOWLResultSet rs = st.executeSelectQuery(query);
			while(rs.hasNext()) {
                final OWLBindingSet bindingSet = rs.next();
                for (String s : rs.getSignature()) {
					OWLObject binding = bindingSet.getOWLObject(s);
					String rendering = ToStringRenderer.getInstance().getRendering(binding);
					retVal.add(rendering);
					log.debug((s + ":  " + rendering));
				}
			}
			return retVal;
		}
	}



	@Test
    public void testCanIRI1() throws Exception {

        String query =  "PREFIX : <http://ontop.inf.unibz.it/test/wellbore#> Select DISTINCT ?x ?y  WHERE{\n" +
				"?x a :Wellbore .\n" +
				"?x :inWell ?y .\n" +
				"}\n";

		ArrayList<String> results = runTests(query);
		ArrayList<String> expectedResults = new ArrayList<>();

		assertEquals(20, results.size());
    }

	@Test
	public void testCanIRI2() throws Exception {
		String query =  "PREFIX : <http://ontop.inf.unibz.it/test/wellbore#> Select DISTINCT ?x WHERE{\n" +
				"?x a :Wellbore .\n" +
				"}\n";

		ArrayList<String> results = runTests(query);

		assertEquals(5, results.size());
	}

	@Test
	public void testCanIRI3() throws Exception {
		String query =  "PREFIX : <http://ontop.inf.unibz.it/test/wellbore#> Select DISTINCT ?x ?y ?z WHERE{\n" +
				"?x a :Wellbore .\n" +
				"?x :inWell ?y .\n" +
				"?x :name ?z .\n" +
				"}\n";

		ArrayList<String> results = runTests(query);

		assertEquals(72, results.size() );
	}

	@Test
	public void testCanIRI4() throws Exception {
		String query =  "PREFIX : <http://ontop.inf.unibz.it/test/wellbore#> Select DISTINCT ?x ?y WHERE{\n" +
				"?x a :Well .\n" +
				"?x :hasWellbore ?y .\n" +
				"}\n";

		ArrayList<String> results = runTests(query);

		assertEquals(8, results.size() );
	}

	@Test
	public void testCanIRI5() throws Exception {
		String query =  "PREFIX : <http://ontop.inf.unibz.it/test/wellbore#> Select * WHERE{\n" +
				"?x :inWell ?y .\n" +
				"}\n";

		ArrayList<String> results = runTests(query);

		assertEquals(20, results.size() );
	}

	@Test
	public void testCanIRI6() throws Exception {
		String query =  "PREFIX : <http://ontop.inf.unibz.it/test/wellbore#> Select DISTINCT ?x WHERE{\n" +
				"?x a :Well .\n" +
				"}\n";

		ArrayList<String> results = runTests(query);

		assertEquals(4, results.size() );
	}

	@Test
	public void testCanIRI7() throws Exception {
		String query =  "PREFIX : <http://ontop.inf.unibz.it/test/wellbore#> Select * WHERE{\n" +
				"?x :hasWellbore ?y .\n" +
				"}\n";

		ArrayList<String> results = runTests(query);

		assertEquals(8, results.size() );
	}

}

