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
import it.unibz.inf.ontop.owlapi.OntopOWLFactory;
import it.unibz.inf.ontop.owlapi.OntopOWLReasoner;
import it.unibz.inf.ontop.owlapi.connection.OWLConnection;
import it.unibz.inf.ontop.owlapi.connection.OWLStatement;
import it.unibz.inf.ontop.owlapi.resultset.OWLBindingSet;
import it.unibz.inf.ontop.owlapi.resultset.TupleOWLResultSet;
import org.junit.After;
import org.junit.Before;
import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.ExpectedException;
import org.semanticweb.owlapi.model.OWLIndividual;
import org.semanticweb.owlapi.reasoner.IllegalConfigurationException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.BufferedReader;
import java.io.FileReader;
import java.io.IOException;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;
import java.sql.Statement;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;


/**
 * This test is adapted from SimpleMappingVirtualABoxTest.
 *
 * A simple test that check if the system is able to handle Mappings for
 * classes/roles and attributes even if there are no URI templates. i.e., the
 * database stores URI's directly.
 *
 * We are going to create an H2 DB, the .sql file is fixed. We will map directly
 * there and then query on top.
 */
public class MetaMappingVirtualABoxMissingColumnTest {

	private Connection conn;

	Logger log = LoggerFactory.getLogger(this.getClass());

	final String owlfile = "src/test/resources/test/metamapping.owl";
	final String obdafile = "src/test/resources/test/metamapping_broken.obda";

	String url = "jdbc:h2:mem:questjunitdb2_broken;DATABASE_TO_UPPER=FALSE";
	String username = "sa";
	String password = "";

	@Before
    public void setUp() throws Exception {

		
		/*
		 * Initializing and H2 database with the stock exchange data
		 */
		// String driver = "org.h2.Driver";
		// Roman: changed the database name to avoid conflict with other tests (in .obda as well)


		conn = DriverManager.getConnection(url, username, password);
		Statement st = conn.createStatement();

		FileReader reader = new FileReader("src/test/resources/test/metamapping-create-h2.sql");
		BufferedReader in = new BufferedReader(reader);
		StringBuilder bf = new StringBuilder();
		String line = in.readLine();
		while (line != null) {
			bf.append(line);
			line = in.readLine();
		}
		in.close();
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

		FileReader reader = new FileReader("src/test/resources/test/metamapping-drop-h2.sql");
		BufferedReader in = new BufferedReader(reader);
		StringBuilder bf = new StringBuilder();
		String line = in.readLine();
		while (line != null) {
			bf.append(line);
			line = in.readLine();
		}
		in.close();
		st.executeUpdate(bf.toString());
		st.close();
		conn.commit();
	}

	private void runTests() throws Exception {

        OntopOWLFactory factory = OntopOWLFactory.defaultFactory();
        OntopSQLOWLAPIConfiguration config = OntopSQLOWLAPIConfiguration.defaultBuilder()
				.nativeOntopMappingFile(obdafile)
				.ontologyFile(owlfile)
				.jdbcUrl(url)
				.jdbcUser(username)
				.jdbcPassword(password)
				.enableTestMode()
				.build();
       
		String query1 = "PREFIX : <http://it.unibz.inf/obda/test/simple#> SELECT * WHERE { ?x a :A_1 }";
        try (OntopOWLReasoner reasoner = factory.createReasoner(config);
			 // Now we are ready for querying
			 OWLConnection conn = reasoner.getConnection();
			 OWLStatement st = conn.createStatement();
			 TupleOWLResultSet rs1 = st.executeSelectQuery(query1);
        ) {
            assertTrue(rs1.hasNext());
            final OWLBindingSet bindingSet = rs1.next();
            OWLIndividual ind = bindingSet.getOWLIndividual("x");
			//OWLIndividual ind2 = rs.getOWLIndividual("y");
			//OWLLiteral val = rs.getOWLLiteral("z");
			assertEquals("<uri1>", ind.toString());
			//assertEquals("<uri1>", ind2.toString());
			//assertEquals("\"value1\"", val.toString());
		}

	}

    @Rule
    public ExpectedException expectedEx = ExpectedException.none();


    @Test
	public void testViEqSig() throws Exception {

        expectedEx.expect(IllegalConfigurationException.class);
        expectedEx.expectMessage(" The placeholder(s) \"code1\" in the target do(es) not occur in the body of the mapping");

		runTests();
	}


}
