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

import org.semanticweb.ontop.model.OBDADataFactory;
import org.semanticweb.ontop.model.impl.OBDADataFactoryImpl;
import org.semanticweb.ontop.owlrefplatform.core.QuestConstants;
import org.semanticweb.ontop.owlrefplatform.core.QuestPreferences;
import org.semanticweb.ontop.owlrefplatform.owlapi3.*;
import org.semanticweb.owlapi.apibinding.OWLManager;
import org.semanticweb.owlapi.model.OWLException;
import org.semanticweb.owlapi.model.OWLObject;
import org.semanticweb.owlapi.model.OWLOntology;
import org.semanticweb.owlapi.model.OWLOntologyManager;
import org.semanticweb.owlapi.reasoner.SimpleConfiguration;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class PropertyCharacteristicTest extends TestCase {
	
	private QuestOWLConnection conn = null;
	private QuestOWLStatement stmt = null;
	private QuestOWL reasoner = null;
	
	private Connection jdbcconn = null;
	private Logger log = LoggerFactory.getLogger(this.getClass());
	private static OBDADataFactory ofac = OBDADataFactoryImpl.getInstance();
	
	private static 	QuestPreferences prefs;
	static {
		Properties p = new Properties();
		p.setProperty(QuestPreferences.ABOX_MODE, QuestConstants.VIRTUAL);
		p.setProperty(QuestPreferences.REWRITE, QuestConstants.TRUE);
		prefs = new QuestPreferences(p);
	}
	
	@Override
	public void setUp() throws Exception {
		createTables();
	}
	
	private String readSQLFile(String file) throws IOException {
		BufferedReader reader = new BufferedReader(new FileReader(file));
		StringBuilder bf = new StringBuilder();
		String line = reader.readLine();
		while (line != null) {
			bf.append(line + "\n");
			line = reader.readLine();
		}
		return bf.toString();
	}
	
	private void createTables() throws IOException, SQLException {
		String createDDL = readSQLFile("src/test/resources/property-characteristics/sqlcreate.sql");
		
		// Initializing and H2 database with the stock exchange data
		// String driver = "org.h2.Driver";
		String url = "jdbc:h2:mem:questjunitdb";
		String username = "sa";
		String password = "";

		ofac = OBDADataFactoryImpl.getInstance();

		jdbcconn = DriverManager.getConnection(url, username, password);
		Statement st = jdbcconn.createStatement();

		st.executeUpdate(createDDL);
		jdbcconn.commit();
	}

	@Override
	public void tearDown() throws Exception {
	
			dropTables();
			conn.close();
			jdbcconn.close();
		
	}

	private void dropTables() throws SQLException, IOException {
		String dropDDL = readSQLFile("src/test/resources/property-characteristics/drop.sql");
		Statement st = jdbcconn.createStatement();
		st.executeUpdate(dropDDL);
		st.close();
		jdbcconn.commit();
	}
	
	public void testNoProperty() throws Exception {
		final File owlFile = new File("src/test/resources/property-characteristics/noproperty.owl");
		final File obdaFile = new File("src/test/resources/property-characteristics/noproperty.obda");
		
		setupReasoner(owlFile, obdaFile);
		QuestOWLResultSet rs = executeQuery("" +
				"PREFIX : <http://www.semanticweb.org/johardi/ontologies/2013/3/Ontology1365668829973.owl#> \n" +
				"SELECT ?x ?y \n" +
				"WHERE { ?x :knows ?y . }"
				);
		final int count = countResult(rs, true);
		assertEquals(3, count);
	}
	
	public void testSymmetricProperty() throws Exception {
		final File owlFile = new File("src/test/resources/property-characteristics/symmetric.owl");
		final File obdaFile = new File("src/test/resources/property-characteristics/symmetric.obda");
		
		setupReasoner(owlFile, obdaFile);
		QuestOWLResultSet rs = executeQuery("" +
				"PREFIX : <http://www.semanticweb.org/johardi/ontologies/2013/3/Ontology1365668829973.owl#> \n" +
				"SELECT ?x ?y \n" +
				"WHERE { ?x :knows ?y . }"
				);
		final int count = countResult(rs, true);
		assertEquals(6, count);
	}
	
	private void setupReasoner(File owlFile, File obdaFile) throws Exception {
		OWLOntologyManager manager = OWLManager.createOWLOntologyManager();
		OWLOntology ontology = manager.loadOntologyFromOntologyDocument(owlFile);
		
		QuestOWLFactory questOWLFactory = new QuestOWLFactory(obdaFile, prefs);
		
		reasoner = (QuestOWL) questOWLFactory.createReasoner(ontology, new SimpleConfiguration());
	}
	
	private QuestOWLResultSet executeQuery(String sparql) throws Exception {
			conn = reasoner.getConnection();
			stmt = conn.createStatement();
			return stmt.executeTuple(sparql);
	}
	
	private int countResult(QuestOWLResultSet rs, boolean stdout) throws OWLException {
		int counter = 0;
		while (rs.nextRow()) {
			counter++;
			if (stdout) {
				for (int column = 1; column <= rs.getColumnCount(); column++) {
					OWLObject binding = rs.getOWLObject(column);
					System.out.print(binding.toString() + ", ");
				}
				System.out.print("\n");
			}
		}
		return counter;
	}
}
