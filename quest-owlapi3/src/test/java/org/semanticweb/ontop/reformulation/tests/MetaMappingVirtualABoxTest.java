package org.semanticweb.ontop.reformulation.tests;

/*
 * #%L
 * ontop-quest-owlapi3
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

import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.io.IOException;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.Properties;

import com.google.common.base.Joiner;
import com.google.common.io.CharStreams;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import static org.junit.Assert.*;

import org.semanticweb.ontop.owlrefplatform.core.QuestConstants;
import org.semanticweb.ontop.owlrefplatform.core.QuestPreferences;
import org.semanticweb.ontop.owlrefplatform.owlapi3.QuestOWL;
import org.semanticweb.ontop.owlrefplatform.owlapi3.QuestOWLConnection;
import org.semanticweb.ontop.owlrefplatform.owlapi3.QuestOWLFactory;
import org.semanticweb.ontop.owlrefplatform.owlapi3.QuestOWLResultSet;
import org.semanticweb.ontop.owlrefplatform.owlapi3.QuestOWLStatement;
import org.semanticweb.owlapi.apibinding.OWLManager;
import org.semanticweb.owlapi.model.OWLIndividual;
import org.semanticweb.owlapi.model.OWLLiteral;
import org.semanticweb.owlapi.model.OWLOntology;
import org.semanticweb.owlapi.model.OWLOntologyManager;
import org.semanticweb.owlapi.reasoner.SimpleConfiguration;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;


/**
 * This test is adapted from {@link org.semanticweb.ontop.reformulation.tests.SimpleMappingVirtualABoxTest}.
 *
 * A simple test that check if the system is able to handle Mappings for
 * classes/roles and attributes even if there are no URI templates. i.e., the
 * database stores URI's directly.
 * 
 * We are going to create an H2 DB, the .sql file is fixed. We will map directly
 * there and then query on top.
 */
public class MetaMappingVirtualABoxTest{


	private Connection conn;

	Logger log = LoggerFactory.getLogger(this.getClass());
	private OWLOntology ontology;

	final String owlfile = "src/test/resources/test/metamapping.owl";
	final String obdaFileName = "src/test/resources/test/metamapping.obda";

	@Before
	public void setUp() throws Exception {
		
		
		/*
		 * Initializing and H2 database with the stock exchange data
		 */
		// String driver = "org.h2.Driver";
		String url = "jdbc:h2:mem:questjunitdb_meta;DATABASE_TO_UPPER=FALSE";
		String username = "sa";
		String password = "";

		conn = DriverManager.getConnection(url, username, password);
		Statement st = conn.createStatement();

        String sql = Joiner.on("\n").join(
                CharStreams.readLines(new FileReader("src/test/resources/test/metamapping-create-h2.sql")));


        st.executeUpdate(sql);
		conn.commit();

		// Loading the OWL file
		OWLOntologyManager manager = OWLManager.createOWLOntologyManager();
		ontology = manager.loadOntologyFromOntologyDocument((new File(owlfile)));
		
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

		st.executeUpdate(bf.toString());
		st.close();
		conn.commit();
	}

	private void runTests(QuestPreferences p) throws Exception {

		// Creating a new instance of the reasoner
		QuestOWLFactory factory = new QuestOWLFactory(new File(obdaFileName), p);
		QuestOWL reasoner = (QuestOWL) factory.createReasoner(ontology, new SimpleConfiguration());

		// Now we are ready for querying
		QuestOWLConnection conn = reasoner.getConnection();
		QuestOWLStatement st = conn.createStatement();

		String query1 = "PREFIX : <http://it.unibz.krdb/obda/test/simple#> SELECT * WHERE { ?x a :A_1 }";
		String query2 = "PREFIX : <http://it.unibz.krdb/obda/test/simple#> SELECT * WHERE { ?x :P_1 ?y }";
		try {

			QuestOWLResultSet rs = st.executeTuple(query1);
			assertTrue(rs.nextRow());
			OWLIndividual ind = rs.getOWLIndividual("x");
			//OWLIndividual ind2 = rs.getOWLIndividual("y");
			//OWLLiteral val = rs.getOWLLiteral("z");
			assertEquals("<uri1>", ind.toString());
			//assertEquals("<uri1>", ind2.toString());
			//assertEquals("\"value1\"", val.toString());
			
			rs = st.executeTuple(query2);
			assertTrue(rs.nextRow());
			OWLIndividual ind1 = rs.getOWLIndividual("x");
			//OWLIndividual ind2 = rs.getOWLIndividual("y");
			OWLLiteral val = rs.getOWLLiteral("y");
			assertEquals("<uri1>", ind1.toString());
			//assertEquals("<uri1>", ind2.toString());
			assertEquals("\"A\"", val.toString());
			

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
	}

	@Test
	public void testViEqSig() throws Exception {

		Properties p = new Properties();
		p.setProperty(QuestPreferences.ABOX_MODE, QuestConstants.VIRTUAL);
		p.setProperty(QuestPreferences.OPTIMIZE_EQUIVALENCES, "true");
		p.setProperty(QuestPreferences.OPTIMIZE_TBOX_SIGMA, "true");
		QuestPreferences preferences = new QuestPreferences();

		runTests(preferences);
	}
	
	@Test
	public void testClassicEqSig() throws Exception {

		Properties p = new Properties();
		p.setProperty(QuestPreferences.ABOX_MODE, QuestConstants.CLASSIC);
		p.setProperty(QuestPreferences.OPTIMIZE_EQUIVALENCES, "true");
		p.setProperty(QuestPreferences.OPTIMIZE_TBOX_SIGMA, "true");
		QuestPreferences preferences = new QuestPreferences();

		runTests(preferences);
	}


}
