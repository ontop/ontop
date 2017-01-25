package it.unibz.inf.ontop.reformulation.tests;

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

import it.unibz.inf.ontop.injection.QuestConfiguration;
import it.unibz.inf.ontop.injection.QuestCoreSettings;
import it.unibz.inf.ontop.owlrefplatform.core.QuestConstants;
import it.unibz.inf.ontop.owlrefplatform.owlapi.*;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.semanticweb.owlapi.io.ToStringRenderer;
import org.semanticweb.owlapi.model.OWLIndividual;
import org.semanticweb.owlapi.model.OWLLiteral;
import org.semanticweb.owlapi.reasoner.ReasonerInternalException;
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

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;

/***
 * Test use of REPLACE in where clause
 */
public class ComplexWhereMappingTest {

	private Connection conn;

	Logger log = LoggerFactory.getLogger(this.getClass());

	final String owlfile = "src/test/resources/test/complexmapping.owl";
	final String obdafile = "src/test/resources/test/complexwheremapping.obda";

	private static final String url = "jdbc:h2:mem:questjunitdb";
	private static final String username = "sa";
	private static final String password = "";

	@Before
	public void setUp() throws Exception {


		/*
		 * Initializing and H2 database
		 */
		// String driver = "org.h2.Driver";

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

	private void runTests(QuestConfiguration config) throws Exception {

        QuestOWLFactory factory = new QuestOWLFactory();
        QuestOWL reasoner = factory.createReasoner(config);

		// Now we are ready for querying
		QuestOWLConnection conn = reasoner.getConnection();
		QuestOWLStatement st = conn.createStatement();

		String query = "PREFIX : <http://it.unibz.inf/obda/test/simple#> SELECT * WHERE { ?x a :A; :P ?y; :U ?z; :P ?y; :U ?z; :P ?y ; :U ?z; :P ?y; :U ?z; :P ?y ; :U ?z; :P ?y; :U ?z; :P ?y ; :U ?z; :P ?y; :U ?z; :P ?y ; :U ?z; :P ?y; :U ?z; :P ?y ; :U ?z; :P ?y; :U ?z; :P ?y ; :U ?z; :P ?y; :U ?z; :P ?y ; :U ?z }";
		StringBuilder bf = new StringBuilder(query);
		try {
			QuestOWLResultSet rs = st.executeTuple(query);
			assertTrue(rs.nextRow());
			OWLIndividual ind1 = rs.getOWLIndividual("x");
			OWLIndividual ind2 = rs.getOWLIndividual("y");
			OWLLiteral val = rs.getOWLLiteral("z");
			assertEquals("<http://it.unibz.inf/obda/test/simple#uri1>", ind1.toString());
			assertEquals("<http://it.unibz.inf/obda/test/simple#uri1>", ind2.toString());
			assertEquals("\"value1\"^^xsd:string", ToStringRenderer.getInstance().getRendering(val));


		} catch (Exception e) {
			throw e;
		} finally {
			conn.close();
			reasoner.dispose();
		}
	}

    @Test
	public void testViEqSig() throws Exception {

		Properties p = new Properties();
		p.put(QuestCoreSettings.ABOX_MODE, QuestConstants.VIRTUAL);
		p.put(QuestCoreSettings.OPTIMIZE_EQUIVALENCES, "true");
		QuestConfiguration config = QuestConfiguration.defaultBuilder()
				.nativeOntopMappingFile(obdafile)
				.ontologyFile(owlfile)
				.properties(p)
				.jdbcUrl(url)
				.jdbcUser(username)
				.jdbcPassword(password)
				.build();

		runTests(config);
	}

    @Test(expected = ReasonerInternalException.class)
    public void testClassicEqSig() throws Exception {

		Properties p = new Properties();
		p.put(QuestCoreSettings.ABOX_MODE, QuestConstants.CLASSIC);
		p.put(QuestCoreSettings.OPTIMIZE_EQUIVALENCES, "true");
		p.put(QuestCoreSettings.OBTAIN_FROM_MAPPINGS, "true");
		QuestConfiguration config = QuestConfiguration.defaultBuilder()
				.ontologyFile(owlfile)
				.properties(p)
				.jdbcUrl(url)
				.jdbcUser(username)
				.jdbcPassword(password)
				.build();

		runTests(config);
	}

}
