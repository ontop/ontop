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
import it.unibz.inf.ontop.owlapi.resultset.TupleOWLResultSet;
import junit.framework.TestCase;
import org.junit.Test;

import java.io.BufferedReader;
import java.io.FileReader;
import java.io.IOException;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.Properties;

/***
 */

public class TMappingConstantPositionsTest extends TestCase {
	private Connection conn;

	final String owlfile = "src/test/resources/test/tmapping-positions.owl";
	final String obdafile = "src/test/resources/test/tmapping-positions.obda";

	String url = "jdbc:h2:mem:questjunitdb";
	String username = "sa";
	String password = "";

	@Override
	public void setUp() throws Exception {
		
		
		/*
		 * Initializing and H2 database with the stock exchange data
		 */


		conn = DriverManager.getConnection(url, username, password);
		Statement st = conn.createStatement();

		FileReader reader = new FileReader("src/test/resources/test/tmapping-positions-create-h2.sql");
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

	@Override
	public void tearDown() throws Exception {
	
			dropTables();
			conn.close();
		
	}

	private void dropTables() throws SQLException, IOException {

		Statement st = conn.createStatement();

		FileReader reader = new FileReader("src/test/resources/test/tmapping-positions-drop-h2.sql");
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

	private void runTests(Properties p) throws Exception {

		// Creating a new instance of the reasoner
		OntopOWLFactory factory = OntopOWLFactory.defaultFactory();
        OntopSQLOWLAPIConfiguration config = OntopSQLOWLAPIConfiguration.defaultBuilder()
				.nativeOntopMappingFile(obdafile)
				.ontologyFile(owlfile)
				.properties(p)
				.jdbcUrl(url)
				.jdbcUser(username)
				.jdbcPassword(password)
				.enableTestMode()
				.build();
        OntopOWLReasoner reasoner = factory.createReasoner(config);

		//System.out.println(reasoner.getQuestInstance().getUnfolder().getRules());
		
		// Now we are ready for querying
		OWLConnection conn = reasoner.getConnection();
		OWLStatement st = conn.createStatement();

		String query = "PREFIX : <http://it.unibz.inf/obda/test/simple#> SELECT * WHERE { ?x a :A. }";
		try {
			TupleOWLResultSet rs = st.executeSelectQuery(query);
			for (int i = 0; i < 3; i++){
				assertTrue(rs.hasNext());
				rs.next();
			}
			assertFalse(rs.hasNext());
		}
		catch (Exception e) {
			throw e;
		} 
		finally {
			st.close();
		}
	}

	@Test
	public void testViEqSig() throws Exception {

		Properties p = new Properties();
		// p.put(OPTIMIZE_EQUIVALENCES, "true");

		runTests(p);
	}


}
