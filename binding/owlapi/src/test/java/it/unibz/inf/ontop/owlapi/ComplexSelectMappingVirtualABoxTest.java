package it.unibz.inf.ontop.owlapi;

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

import it.unibz.inf.ontop.injection.OntopSQLOWLAPIConfiguration;
import it.unibz.inf.ontop.iq.IQ;
import it.unibz.inf.ontop.owlapi.connection.OntopOWLConnection;
import it.unibz.inf.ontop.owlapi.connection.OntopOWLStatement;
import it.unibz.inf.ontop.owlapi.resultset.OWLBindingSet;
import it.unibz.inf.ontop.owlapi.resultset.TupleOWLResultSet;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.semanticweb.owlapi.io.ToStringRenderer;
import org.semanticweb.owlapi.model.OWLIndividual;
import org.semanticweb.owlapi.model.OWLLiteral;

import java.sql.Connection;
import java.sql.DriverManager;
import java.util.Properties;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import static it.unibz.inf.ontop.utils.OWLAPITestingTools.executeFromFile;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;

/**
 * Test for CONCAT and REPLACE in SQL query
 */

public class ComplexSelectMappingVirtualABoxTest  {
	private Connection conn;

	final String owlfile = "src/test/resources/test/complexmapping.owl";
	final String obdafile = "src/test/resources/test/complexmapping.obda";

	private static final String url = "jdbc:h2:mem:questjunitdb";
	private static final String username = "sa";
	private static final String password = "";

	@Before
	public void setUp() throws Exception {
		conn = DriverManager.getConnection(url, username, password);
		executeFromFile(conn, "src/test/resources/test/complexmapping-create-h2.sql");
	}

	@After
	public void tearDown() throws Exception {
		executeFromFile(conn,"src/test/resources/test/simplemapping-drop-h2.sql");
		conn.close();
	}

//   test for self join count the number of occurrences
	private String runTests(String query) throws Exception {

        OntopOWLFactory factory = OntopOWLFactory.defaultFactory();
        OntopSQLOWLAPIConfiguration config = OntopSQLOWLAPIConfiguration.defaultBuilder()
				.nativeOntopMappingFile(obdafile)
				.ontologyFile(owlfile)
				.properties(new Properties())
				.jdbcUrl(url)
				.jdbcUser(username)
				.jdbcPassword(password)
				.enableDefaultDatatypeInference(true)
				.enableTestMode()
				.build();
        OntopOWLReasoner reasoner = factory.createReasoner(config);

		// Now we are ready for querying

        OWLLiteral val;
		try (OntopOWLConnection conn = reasoner.getConnection();
			 OntopOWLStatement st = conn.createStatement()) {
			IQ iq = st.getExecutableQuery(query);
			Pattern pat = Pattern.compile("TABLE1 ");
		    Matcher m = pat.matcher(iq.toString());
		    int num_joins = -1;
		    while (m.find()){
		    	num_joins +=1;
		    }
		    //System.out.println(sql);
			// Inapplicable because TABLE1 is used in the view name
			//assertEquals(num_joins, 0);
			TupleOWLResultSet rs = st.executeSelectQuery(query);
			assertTrue(rs.hasNext());
            final OWLBindingSet bindingSet = rs.next();
            OWLIndividual ind1 = bindingSet.getOWLIndividual("x");
			val = bindingSet.getOWLLiteral("z");
			assertEquals("<http://it.unibz.inf/obda/test/simple#uri%201>", ind1.toString());

			//assertEquals("\"value1\"", val.toString());
		}
		finally {
			reasoner.dispose();
		}
        return ToStringRenderer.getInstance().getRendering(val);
	}

    @Test
    public void testReplaceValue() throws Exception {

		String query = "PREFIX : <http://it.unibz.inf/obda/test/simple#> SELECT * WHERE { ?x :U4 ?z . }";

        String val = runTests(query);
        assertEquals("\"ualue1\"^^xsd:string", val);
    }

    @Test
	public void testConcat() throws Exception {

		String query = "PREFIX : <http://it.unibz.inf/obda/test/simple#> SELECT * WHERE { ?x :U2 ?z. }";

        String val = runTests(query);
        assertEquals("\"NO value1\"^^xsd:string", val);
	}

    @Test
	public void testDoubleConcat() throws Exception {

		String query = "PREFIX : <http://it.unibz.inf/obda/test/simple#> SELECT * WHERE { ?x :U2 ?z; :U3 ?w. }";

        String val = runTests(query);
        assertEquals("\"NO value1\"^^xsd:string", val);
	}

    @Test
    public void testConcat2() throws Exception {

		String query = "PREFIX : <http://it.unibz.inf/obda/test/simple#> SELECT * WHERE { ?x :U5 ?z. }";

        String val = runTests(query);
        assertEquals("\"value1test\"^^xsd:string", val);
    }

    @Test
    public void testConcat3() throws Exception {

        String query = "PREFIX : <http://it.unibz.inf/obda/test/simple#> SELECT * WHERE { ?x :U6 ?z. }";

        String val = runTests(query);
        assertEquals("\"value1test\"^^xsd:string", val);
    }

    @Test
    public void testConcat4() throws Exception {

        String query = "PREFIX : <http://it.unibz.inf/obda/test/simple#> SELECT * WHERE { ?x :U7 ?z. }";

        String val = runTests(query);
        assertEquals("\"value1touri 1\"^^xsd:string", val);
    }

    @Test
    public void testConcat5() throws Exception {

        String query = "PREFIX : <http://it.unibz.inf/obda/test/simple#> SELECT * WHERE { ?x :U8 ?z. }";

        String val = runTests(query);
        assertEquals("\"value1test\"^^xsd:string", val);
    }

    @Test
    public void testConcatAndReplaceUri() throws Exception {

        String query = "PREFIX : <http://it.unibz.inf/obda/test/simple#> SELECT * WHERE { ?x :U9 ?z. }";

        String val = runTests(query);
        assertEquals("\"value1\"^^xsd:string", val);
    }
}
