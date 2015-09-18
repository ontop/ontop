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

import junit.framework.TestCase;

import org.junit.Test;
import org.semanticweb.ontop.owlrefplatform.core.QuestConstants;
import org.semanticweb.ontop.owlrefplatform.core.QuestPreferences;
import org.semanticweb.ontop.owlrefplatform.owlapi3.*;
import org.semanticweb.owlapi.apibinding.OWLManager;
import org.semanticweb.owlapi.model.OWLOntology;
import org.semanticweb.owlapi.model.OWLOntologyManager;
import org.semanticweb.owlapi.reasoner.SimpleConfiguration;

/***
 */

public class TMappingConstantPositionsTest extends TestCase {
	private Connection conn;

	private OWLOntology ontology;

	final String owlfile = "src/test/resources/test/tmapping-positions.owl";
	final String obdafile = "src/test/resources/test/tmapping-positions.obda";

	@Override
	public void setUp() throws Exception {
		
		
		/*
		 * Initializing and H2 database with the stock exchange data
		 */
		// String driver = "org.h2.Driver";
		String url = "jdbc:h2:mem:questjunitdb";
		String username = "sa";
		String password = "";

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

		// Loading the OWL file
		OWLOntologyManager manager = OWLManager.createOWLOntologyManager();
		ontology = manager.loadOntologyFromOntologyDocument((new File(owlfile)));
		
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

	private void runTests(QuestPreferences p) throws Exception {

		// Creating a new instance of the reasoner
		QuestOWLFactory factory = new QuestOWLFactory(new File(obdafile), p);

		QuestOWL reasoner = factory.createReasoner(ontology, new SimpleConfiguration());

		System.out.println(reasoner.getQuestInstance().getQuestUnfolder().getRules());
		
		// Now we are ready for querying
		QuestOWLConnection conn = reasoner.getConnection();
		QuestOWLStatement st = conn.createStatement();

		String query = "PREFIX : <http://it.unibz.krdb/obda/test/simple#> SELECT * WHERE { ?x a :A. }";
		try {
			QuestOWLResultSet rs = st.executeTuple(query);
			assertTrue(rs.nextRow());
			assertTrue(rs.nextRow());
			assertTrue(rs.nextRow());
			assertFalse(rs.nextRow());
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
		p.put(QuestPreferences.ABOX_MODE, QuestConstants.VIRTUAL);
		p.put(QuestPreferences.OPTIMIZE_EQUIVALENCES, "true");
		p.put(QuestPreferences.OPTIMIZE_TBOX_SIGMA, "true");

		runTests(new QuestPreferences(p));
	}
	
	@Test
	public void testClassicEqSig() throws Exception {

		Properties p = new Properties();
		p.put(QuestPreferences.ABOX_MODE, QuestConstants.CLASSIC);
		p.put(QuestPreferences.OPTIMIZE_EQUIVALENCES, "true");
		p.put(QuestPreferences.OPTIMIZE_TBOX_SIGMA, "true");
		p.put(QuestPreferences.OBTAIN_FROM_MAPPINGS, "true");

		runTests(new QuestPreferences(p));
	}


}
