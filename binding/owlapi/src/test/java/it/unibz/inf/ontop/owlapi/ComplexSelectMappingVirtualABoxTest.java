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

import it.unibz.inf.ontop.answering.reformulation.impl.SQLExecutableQuery;
import it.unibz.inf.ontop.injection.OntopSQLOWLAPIConfiguration;
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
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.BufferedReader;
import java.io.FileReader;
import java.io.IOException;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.Properties;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import static it.unibz.inf.ontop.injection.OntopReformulationSettings.SQL_GENERATE_REPLACE;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;

/**
 * Test for CONCAT and REPLACE in SQL query
 */

public class ComplexSelectMappingVirtualABoxTest  {
	private Connection conn;

	String query = null;
	
	Logger log = LoggerFactory.getLogger(this.getClass());

	final String owlfile = "src/test/resources/test/complexmapping.owl";
	final String obdafile = "src/test/resources/test/complexmapping.obda";

	private static final String url = "jdbc:h2:mem:questjunitdb";
	private static final String username = "sa";
	private static final String password = "";

	@Before
	public void setUp() throws Exception {
		
		
		/*
		 * Initializing and H2 database with the stock exchange data
		 */
		// String driver = "org.h2.Driver";

		conn = DriverManager.getConnection(url, username, password);
		Statement st = conn.createStatement();

		FileReader reader = new FileReader("src/test/resources/test/complexmapping-create-h2.sql");
		BufferedReader in = new BufferedReader(reader);
		StringBuilder bf = new StringBuilder();
		String line = in.readLine();
		while (line != null) {
			bf.append(line);
			line = in.readLine();
		}

		st.executeUpdate(bf.toString());
		conn.commit();
	}

	@After
	public void tearDown() throws Exception {
	
			dropTables();
			conn.close();
		
	}

	private void dropTables() throws SQLException, IOException {

		Statement st = conn.createStatement();

		FileReader reader = new FileReader("src/test/resources/test/simplemapping-drop-h2.sql");
		BufferedReader in = new BufferedReader(reader);
		StringBuilder bf = new StringBuilder();
		String line = in.readLine();
		while (line != null) {
			bf.append(line);
			line = in.readLine();
		}

		st.executeUpdate(bf.toString());
		st.close();
		conn.commit();
	}

//   test for self join count the number of occurrences
	private String runTests() throws Exception {
		return runTests(new Properties());
	}
	private String runTests(Properties p) throws Exception {

        OntopOWLFactory factory = OntopOWLFactory.defaultFactory();
        OntopSQLOWLAPIConfiguration config = OntopSQLOWLAPIConfiguration.defaultBuilder()
				.nativeOntopMappingFile(obdafile)
				.ontologyFile(owlfile)
				.properties(p)
				.jdbcUrl(url)
				.jdbcUser(username)
				.jdbcPassword(password)
				.enableDefaultDatatypeInference(true)
				.enableTestMode()
				.build();
        OntopOWLReasoner reasoner = factory.createReasoner(config);


		// Now we are ready for querying
		OntopOWLConnection conn = reasoner.getConnection();
		OntopOWLStatement st = conn.createStatement();

        OWLLiteral val;
		try {
			String sql = ((SQLExecutableQuery)st.getExecutableQuery(this.query)).getSQL();
			Pattern pat = Pattern.compile("TABLE1 ");
		    Matcher m = pat.matcher(sql);
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

		} catch (Exception e) {
			throw e;
		} finally {
			try {

			} catch (Exception e) {
				st.close();
				throw e;
			}
			conn.close();
			reasoner.dispose();
		}
        return ToStringRenderer.getInstance().getRendering(val);
	}

    @Test
	public void testReplace() throws Exception {

		Properties p = new Properties();
        p.put(SQL_GENERATE_REPLACE, false);

		this.query = "PREFIX : <http://it.unibz.inf/obda/test/simple#> SELECT * WHERE { ?x :U ?z. }";

        String val = runTests(p);
        assertEquals("\"value1\"^^xsd:string", val);

	}

    @Test
    public void testReplaceValue() throws Exception {

		this.query = "PREFIX : <http://it.unibz.inf/obda/test/simple#> SELECT * WHERE { ?x :U4 ?z . }";

        String val = runTests();
        assertEquals("\"ualue1\"^^xsd:string", val);
    }

    @Test
	public void testConcat() throws Exception {

		this.query = "PREFIX : <http://it.unibz.inf/obda/test/simple#> SELECT * WHERE { ?x :U2 ?z. }";

        String val = runTests();
        assertEquals("\"NO value1\"^^xsd:string", val);
	}

    @Test
	public void testDoubleConcat() throws Exception {

		this.query = "PREFIX : <http://it.unibz.inf/obda/test/simple#> SELECT * WHERE { ?x :U2 ?z; :U3 ?w. }";

        String val = runTests();
        assertEquals("\"NO value1\"^^xsd:string", val);
	}

    @Test
    public void testConcat2() throws Exception {

        this.query = "PREFIX : <http://it.unibz.inf/obda/test/simple#> SELECT * WHERE { ?x :U5 ?z. }";

        String val = runTests();
        assertEquals("\"value1test\"^^xsd:string", val);
    }

    @Test
    public void testConcat3() throws Exception {

        this.query = "PREFIX : <http://it.unibz.inf/obda/test/simple#> SELECT * WHERE { ?x :U6 ?z. }";

        String val = runTests();
        assertEquals("\"value1test\"^^xsd:string", val);
    }

    @Test
    public void testConcat4() throws Exception {
		Properties p = new Properties();

        this.query = "PREFIX : <http://it.unibz.inf/obda/test/simple#> SELECT * WHERE { ?x :U7 ?z. }";

        String val = runTests(p);
        assertEquals("\"value1touri 1\"^^xsd:string", val);
    }

    @Test
    public void testConcat5() throws Exception {

        this.query = "PREFIX : <http://it.unibz.inf/obda/test/simple#> SELECT * WHERE { ?x :U8 ?z. }";

        String val = runTests();
        assertEquals("\"value1test\"^^xsd:string", val);
    }

    @Test
    public void testConcatAndReplaceUri() throws Exception {

        this.query = "PREFIX : <http://it.unibz.inf/obda/test/simple#> SELECT * WHERE { ?x :U9 ?z. }";

        String val = runTests();
        assertEquals("\"value1\"^^xsd:string", val);
    }


	
}
