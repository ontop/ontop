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
import it.unibz.inf.ontop.owlapi.connection.OWLConnection;
import it.unibz.inf.ontop.owlapi.connection.OWLStatement;
import it.unibz.inf.ontop.owlapi.resultset.OWLBindingSet;
import it.unibz.inf.ontop.owlapi.resultset.TupleOWLResultSet;
import junit.framework.TestCase;
import org.semanticweb.owlapi.io.ToStringRenderer;
import org.semanticweb.owlapi.model.OWLObject;
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

/***
 * A simple test that check if the system is able to handle Mappings for
 * classes/roles and attributes even if there are no URI templates. i.e., the
 * database stores URI's directly.
 * 
 * We are going to create an H2 DB, the .sql file is fixed. We will map directly
 * there and then query on top.
 */
public class SimpleMappingVirtualABoxTest extends TestCase {

	private Connection conn;

	Logger log = LoggerFactory.getLogger(this.getClass());

	final String owlfile = "src/test/resources/test/simplemapping.owl";
	final String obdafile = "src/test/resources/test/simplemapping.obda";

	private static final String url = "jdbc:h2:mem:questjunitdb";
	private static final String username = "sa";
	private static final String password = "";

	@Override
	public void setUp() throws Exception {
		/*
		 * Initializing and H2 database with the stock exchange data
		 */
		conn = DriverManager.getConnection(url, username, password);
		Statement st = conn.createStatement();

		FileReader reader = new FileReader("src/test/resources/test/simplemapping-create-h2.sql");
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

		FileReader reader = new FileReader("src/test/resources/test/simplemapping-drop-h2.sql");
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

		// Now we are ready for querying
		OWLConnection conn = reasoner.getConnection();
		OWLStatement st = conn.createStatement();

		String query = "PREFIX : <http://it.unibz.inf/obda/test/simple#> SELECT * WHERE { ?x a :A; :P ?y; :U ?z; :P ?y; :U ?z; :P ?y ; :U ?z; :P ?y; :U ?z; :P ?y ; :U ?z; :P ?y; :U ?z; :P ?y ; :U ?z; :P ?y; :U ?z; :P ?y ; :U ?z; :P ?y; :U ?z; :P ?y ; :U ?z; :P ?y; :U ?z; :P ?y ; :U ?z; :P ?y; :U ?z; :P ?y ; :U ?z }";
		try {
			
			/*
			 * Enable this if you want to test performance, it will run several cycles
			 */
//			long start = System.currentTimeMillis();
//			for (int i = 0; i < 3000; i++) {
//				QuestOntopOWLStatement sto = (QuestQuestOWLStatement)st;
//				String q = sto.getExecutableQuery(bf.insert(7, ' ').toString());
//			}
//			long end = System.currentTimeMillis();
//			long elapsed = end-start;
//			log.info("Elapsed time: {}", elapsed);
			TupleOWLResultSet rs = st.executeSelectQuery(query);
			assertTrue(rs.hasNext());
            final OWLBindingSet bindingSet = rs.next();
            OWLObject ind1 = bindingSet.getOWLObject("x");
			OWLObject ind2 = bindingSet.getOWLObject("y");
			OWLObject val = bindingSet.getOWLObject("z");
			assertEquals("<http://example.org/uri1>", ToStringRenderer.getInstance().getRendering(ind1));
			assertEquals("<http://example.org/uri1>", ToStringRenderer.getInstance().getRendering(ind2));
			assertEquals("\"value1\"^^xsd:string", ToStringRenderer.getInstance().getRendering(val));
			

		} catch (Exception e) {
			throw e;
		} finally {
			try {
				st.close();
			} catch (Exception e) {
				throw e;
			} finally {
				conn.close();
				reasoner.dispose();
			}
		}
	}

	public void testViEqSig() throws Exception {
		Properties p = new Properties();
		// p.setProperty(OPTIMIZE_EQUIVALENCES, "true");

		runTests(p);
	}
}
