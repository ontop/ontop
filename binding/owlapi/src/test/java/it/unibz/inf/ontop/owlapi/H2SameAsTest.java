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

import com.google.common.collect.ImmutableSet;
import it.unibz.inf.ontop.injection.OntopSQLOWLAPIConfiguration;
import it.unibz.inf.ontop.owlapi.OntopOWLFactory;
import it.unibz.inf.ontop.owlapi.OntopOWLReasoner;
import it.unibz.inf.ontop.owlapi.connection.OWLConnection;
import it.unibz.inf.ontop.owlapi.connection.OWLStatement;
import it.unibz.inf.ontop.owlapi.resultset.OWLBindingSet;
import it.unibz.inf.ontop.owlapi.resultset.TupleOWLResultSet;
import org.junit.After;
import org.junit.Before;
import org.junit.Ignore;
import org.junit.Test;
import org.semanticweb.owlapi.io.ToStringRenderer;
import org.semanticweb.owlapi.model.OWLObject;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.File;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;
import java.util.*;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;

/***
 * Test same as using h2 simple database on wellbores
 */
public class H2SameAsTest {

	private OWLConnection conn;

	private final Logger log = LoggerFactory.getLogger(this.getClass());

	private static final String owlfile = "src/test/resources/sameAs/wellbores.owl";
	private static final String obdafile = "src/test/resources/sameAs/wellbores.obda";

	private OntopOWLReasoner reasoner;
	private Connection sqlConnection;

	private static final String JDBC_URL =  "jdbc:h2:mem:wellboresNoDuplicates";
	private static final String JDBC_USER =  "sa";
	private static final String JDBC_PASSWORD =  "";

	@Before
	public void setUp() throws Exception {
		sqlConnection = DriverManager.getConnection(JDBC_URL,JDBC_USER, JDBC_PASSWORD);
		try (java.sql.Statement s = sqlConnection.createStatement()) {
			String text = new Scanner(new File("src/test/resources/sameAs/wellbore-h2.sql")).useDelimiter("\\A").next();
			s.execute(text);
		}
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
	

	
	private List<String> runTests(String query, boolean sameAs) throws Exception {

		// Creating a new instance of the reasoner
		OntopOWLFactory factory = OntopOWLFactory.defaultFactory();

		OntopSQLOWLAPIConfiguration config = OntopSQLOWLAPIConfiguration.defaultBuilder()
				.ontologyFile(owlfile)
				.nativeOntopMappingFile(obdafile)
				.sameAsMappings(sameAs)
				.jdbcUrl(JDBC_URL)
				.jdbcUser(JDBC_USER)
				.jdbcPassword(JDBC_PASSWORD)
				.enableTestMode()
				.build();

		reasoner =  factory.createReasoner(config);

		// Now we are ready for querying
		conn = reasoner.getConnection();

		ArrayList<String> retVal = new ArrayList<>();
		try (OWLStatement st = conn.createStatement()) {
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
		}
		finally {
			conn.close();
			reasoner.dispose();
		}
		return retVal;

	}



    /**
	 * Test use of sameAs
     * the expected results
	 *  <pre> 
     * 911 'Amerigo'
     * 911 Aleksi
     * 1 Aleksi
     * 1 'Amerigo'
     * 992 'Luis'
     * 993 'Sagrada Familia'
     * 2 'Eljas'
	 * </pre>
	 * 
	 * @throws Exception
	 */
	@Test
	public void testSameAs1() throws Exception {
		String query =  "PREFIX : <http://ontop.inf.unibz.it/test/wellbore#> SELECT ?x ?y\n" +
                "WHERE {\n" +
                "   ?x  :hasName ?y . \n" +
                "}";

		Set<String> results = ImmutableSet.copyOf(runTests(query, true));
		Set<String> expectedResults = new HashSet<>();
		expectedResults.add("<http://ontop.inf.unibz.it/test/wellbore#spain-991>");
		expectedResults.add("\"Aleksi\"^^xsd:string");
		expectedResults.add("<http://ontop.inf.unibz.it/test/wellbore#finland-1>");
		expectedResults.add("\"Amerigo\"^^xsd:string");
		expectedResults.add("<http://ontop.inf.unibz.it/test/wellbore#finland-2>");
		expectedResults.add("\"Eljas\"^^xsd:string");
		expectedResults.add("<http://ontop.inf.unibz.it/test/wellbore#spain-992>");
		expectedResults.add("\"Luis\"^^xsd:string");
		expectedResults.add("<http://ontop.inf.unibz.it/test/wellbore#spain-993>");
		expectedResults.add("\"Sagrada Familia\"^^xsd:string");

		assertEquals(expectedResults.size(), results.size() );
		assertEquals(expectedResults, results);
	}

	@Test
	public void testNoSameAs1() throws Exception {
		String query =  "PREFIX : <http://ontop.inf.unibz.it/test/wellbore#>" +
				"PREFIX owl: <http://www.w3.org/2002/07/owl#>" +
				"SELECT ?x ?y WHERE { { ?x :hasName ?y .} UNION {?x owl:sameAs [ :hasName ?y]} }\n";

		// Bind (?n ?y)
		runTests(query, false);
	}

    /**
     * Test use of sameAs
     * the expected results
	 * <pre>
     * 911 'Amerigo' 13
     * 1 Aleksi 13
     * 2 'Eljas' 100
	 * Results as testNoSameAs2a()
	 * what we get is
	 * 911 'Amerigo' 13
	 * 911 Aleksi 13
	 * 1 'Amerigo' 13
	 * 1 Aleksi 13
	 * 2 'Eljas' 100
	 * </pre>
	 * Results as testNoSameAs2b()
     * @throws Exception
     */

    @Test
    public void testSameAs2() throws Exception {
        String query =  "PREFIX : <http://ontop.inf.unibz.it/test/wellbore#> SELECT ?x ?y ?z\n" +
                "WHERE {\n" +
                "   ?x  :hasName ?y . \n" +
                "   ?x  :hasValue ?z . \n " +
                "}";

		List<String> results = runTests(query, true);
		Set<String> expectedResults = new HashSet<>();
		expectedResults.add("\"Aleksi\"^^xsd:string");
		expectedResults.add("\"13\"^^xsd:integer");
		expectedResults.add("<http://ontop.inf.unibz.it/test/wellbore#finland-1>");
		expectedResults.add("<http://ontop.inf.unibz.it/test/wellbore#finland-2>");
		expectedResults.add("\"Eljas\"^^xsd:string");
		expectedResults.add("\"100\"^^xsd:integer");
//		assertEquals(expectedResults.size(), results.size());
		assertEquals(expectedResults, new HashSet<>(results));
    }

	@Test
	public void testNoSameAs2a() throws Exception {
		String query =  "PREFIX : <http://ontop.inf.unibz.it/test/wellbore#>" +
				"PREFIX owl: <http://www.w3.org/2002/07/owl#>" +
				"SELECT ?x ?y ?z WHERE { { ?x :hasName ?y .  ?x  :hasValue ?z . } UNION {?x owl:sameAs [ :hasName ?y ; :hasValue ?z ]} }\n";

		// Bind (?n ?y)
		List<String> results = runTests(query, false);
		assertEquals(9, results.size() );
	}

	@Test //missing the inverse
	public void testNoSameAs2b() throws Exception {
		String query =  "PREFIX : <http://ontop.inf.unibz.it/test/wellbore#>" +
				"PREFIX owl: <http://www.w3.org/2002/07/owl#>" +
				"SELECT ?x ?y ?z WHERE { { ?x :hasName ?y .  ?x  :hasValue ?z . } UNION {?x owl:sameAs [ :hasName ?y ] . ?x :hasValue ?z } UNION {?x :hasName ?y . ?x owl:sameAs [ :hasValue ?z ]}  UNION {?x owl:sameAs  [ :hasName ?y ]. ?x owl:sameAs [ :hasValue ?z ]} }\n";

		// Bind (?n ?y)
		List<String> results = runTests(query, false);
		assertEquals(12, results.size() );
	}

	@Test
	public void testSameAs3() throws Exception {
		String query =  "PREFIX : <http://ontop.inf.unibz.it/test/wellbore#> SELECT ?x ?y\n" +
				"WHERE {\n" +
				"   ?x :hasOwner ?y . \n" +
				"}";

		runTests(query, true);
	}


	@Test
    public void testNoSameAs3() throws Exception {
        String query =  "PREFIX : <http://ontop.inf.unibz.it/test/wellbore#>  \n" +
                "SELECT ?x ?y WHERE { { ?x :hasOwner ?y . } UNION {?x :hasOwner [owl:samesAs ?y]} UNION {?x owl:sameAs [:hasOwner ?y]  } UNION {?x owl:sameAs [ owl:hasOwner [owl:samesAs ?y]]} }";

        runTests(query, false);
    }













}

