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

import java.sql.Connection;
import java.sql.DriverManager;
import java.util.ArrayList;

import static it.unibz.inf.ontop.utils.OWLAPITestingTools.executeFromFile;
import static org.junit.Assert.assertEquals;

/***
 * Test same as using h2 simple database on wellbores
 */
@Ignore
public class H2ComplexSameAsTest {

	private final Logger log = LoggerFactory.getLogger(this.getClass());

	private static final String owlfile = "src/test/resources/sameAs/wellbores-complex.owl";
	private static final String obdafile = "src/test/resources/sameAs/wellbores-complex.obda";
	private static final String propertyfile = "src/test/resources/sameAs/wellbores-complex.properties";

	private Connection sqlConnection;

	@Before
	public void setUp() throws Exception {
		sqlConnection = DriverManager.getConnection("jdbc:h2:mem:wellboresComplex", "sa", "");
		executeFromFile(sqlConnection, "src/test/resources/sameAs/wellbore-complex-h2.sql");
	}

	@After
	public void tearDown() throws Exception{
		if (!sqlConnection.isClosed()) {
			try (java.sql.Statement s = sqlConnection.createStatement()) {
				s.execute("DROP ALL OBJECTS DELETE FILES");
			}
			finally {
				sqlConnection.close();
			}
		}
	}



	private ArrayList<String> runTests(String query, boolean sameAs) throws Exception {

		// Creating a new instance of the reasoner
		OntopOWLFactory factory = OntopOWLFactory.defaultFactory();
		OntopSQLOWLAPIConfiguration config = OntopSQLOWLAPIConfiguration.defaultBuilder()
				.nativeOntopMappingFile(obdafile)
				.ontologyFile(owlfile)
				.propertyFile(propertyfile)
				.sameAsMappings(sameAs)
				.enableTestMode()
				.build();

		OntopOWLReasoner reasoner = factory.createReasoner(config);

		// Now we are ready for querying
		OWLConnection conn = reasoner.getConnection();

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
		finally {
			conn.close();
			reasoner.dispose();
		}
	}


	@Test
    public void testSameAs1() throws Exception {
		String query =  "PREFIX : <http://ontop.inf.unibz.it/test/wellbore#> " +
				"SELECT DISTINCT ?x\n" +
				"WHERE {\n" +
				"   ?x  a :Wellbore . \n" +
				"}";

		final ImmutableSet<String> results = ImmutableSet.copyOf(runTests(query, true));

		ImmutableSet<String> expectedResults =
				ImmutableSet.<String>builder()
						.add("<http://ontop.inf.unibz.it/test/wellbore/Katian>")
						.add("<http://ontop.inf.unibz.it/test/wellbore/Bill>")
						.add("<http://ontop.inf.unibz.it/test/wellbore#uri1-1>")
						.add("<http://ontop.inf.unibz.it/test/wellbore#uri1-2>")
						.add("<http://ontop.inf.unibz.it/test/wellbore#uri2-1>")
						.add("<http://ontop.inf.unibz.it/test/wellbore#uri2-2>")
						.add("<http://ontop.inf.unibz.it/test/wellbore#uri2-3>")
						.add("<http://ontop.inf.unibz.it/test/wellbore#uri3-1>")
						.add("<http://ontop.inf.unibz.it/test/wellbore#uri3-2>")
						.add("<http://ontop.inf.unibz.it/test/wellbore#uri3-3>")
						.add("<http://ontop.inf.unibz.it/test/wellbore#uri3-4>")
						.build();
		assertEquals(expectedResults.size(), results.size() );
		assertEquals(expectedResults, results);
    }

	@Test
	public void testNoSameAs1() throws Exception {
		String query =  "PREFIX : <http://ontop.inf.unibz.it/test/wellbore#> SELECT DISTINCT ?x\n" +
				"WHERE {\n" +
				" {  ?x  a :Wellbore . \n" +
				"} UNION {?x owl:sameAs [ a :Wellbore ] }} ";

		ArrayList<String> results = runTests(query, false);
		assertEquals(11, results.size() );
	}

	@Test
	public void testSameAs2() throws Exception {
		String query =  "PREFIX : <http://ontop.inf.unibz.it/test/wellbore#> SELECT DISTINCT ?x \n" +
                "WHERE {\n" +
                "   ?x  a :Wellbore . \n" +
                "   ?x  :hasName ?y . \n" +
                "}";

		ArrayList<String> results = runTests(query, true);
		assertEquals(9, results.size() );
	}

    @Test
    public void testSameAs3() throws Exception {
        String query =  "PREFIX : <http://ontop.inf.unibz.it/test/wellbore#> \n" +
                "SELECT DISTINCT * WHERE { ?x a :Wellbore .\n" +
                " ?x :hasName ?y .\n" +
                " ?x :isActive ?z .}\n";

		ArrayList<String> results = runTests(query, true);
		assertEquals(33, results.size() );
	}

	@Test
	public void testSameAs3b() throws Exception {
		String query =  "PREFIX : <http://ontop.inf.unibz.it/test/wellbore#> \n" +
				"SELECT DISTINCT * WHERE { ?x a :Wellbore .\n" +
				" ?x :isActive ?z .}\n";

		ArrayList<String> results = runTests(query, true);
		assertEquals(16, results.size() );
	}

    @Test
    public void testSameAs4() throws Exception {
        String query =  "PREFIX : <http://ontop.inf.unibz.it/test/wellbore#> \n" +
                "SELECT DISTINCT * WHERE { ?x a :Wellbore .\n" +
                " ?x :isHappy ?z .}\n";

		ArrayList<String> results = runTests(query, true);
		assertEquals(18, results.size() );
    }

    @Test
    public void testSameAs5() throws Exception {
        String query =  "PREFIX : <http://ontop.inf.unibz.it/test/wellbore#> \n" +
                "SELECT DISTINCT * WHERE { " +
                " ?x :hasOwner ?y .}\n";

		ArrayList<String> results = runTests(query, true);
		assertEquals(24, results.size() );
    }

	@Test
	public void testSameAs6() throws Exception {
		String query =  "PREFIX : <http://ontop.inf.unibz.it/test/wellbore#> \n" +
				"SELECT DISTINCT * WHERE { " +
				"?x  a :Wellbore ." +
				" ?x :hasOwner ?y .}\n";

		ArrayList<String> results = runTests(query, true);
		assertEquals(24, results.size() );
	}
}

