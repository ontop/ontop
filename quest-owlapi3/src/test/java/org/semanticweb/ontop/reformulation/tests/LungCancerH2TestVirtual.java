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
import java.util.Iterator;
import java.util.List;
import java.util.Properties;

import junit.framework.TestCase;

import org.semanticweb.ontop.io.ModelIOManager;
import org.semanticweb.ontop.model.OBDADataFactory;
import org.semanticweb.ontop.model.OBDAModel;
import org.semanticweb.ontop.model.impl.OBDADataFactoryImpl;
import org.semanticweb.ontop.owlrefplatform.core.QuestConstants;
import org.semanticweb.ontop.owlrefplatform.core.QuestPreferences;
import org.semanticweb.ontop.owlrefplatform.owlapi3.QuestOWL;
import org.semanticweb.ontop.owlrefplatform.owlapi3.QuestOWLConnection;
import org.semanticweb.ontop.owlrefplatform.owlapi3.QuestOWLFactory;
import org.semanticweb.ontop.owlrefplatform.owlapi3.QuestOWLResultSet;
import org.semanticweb.ontop.owlrefplatform.owlapi3.QuestOWLStatement;
import org.semanticweb.owlapi.apibinding.OWLManager;
import org.semanticweb.owlapi.model.OWLAxiom;
import org.semanticweb.owlapi.model.OWLOntology;
import org.semanticweb.owlapi.model.OWLOntologyManager;
import org.semanticweb.owlapi.reasoner.SimpleConfiguration;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/***
 * A simple test that check if the system is able to handle Mappings for
 * classes/roles and attributes even if there are no URI templates. i.e., the
 * database stores URI's directly.
 * 
 * We are going to create an H2 DB, the .sql file is fixed. We will map directly
 * there and then query on top.
 */
public class LungCancerH2TestVirtual extends TestCase {

	// TODO We need to extend this test to import the contents of the mappings
	// into OWL and repeat everything taking form OWL

	private OBDADataFactory fac;
	private Connection conn;

	Logger log = LoggerFactory.getLogger(this.getClass());
	private OBDAModel obdaModel;
	private OWLOntology ontology;

	final String owlfile = "src/test/resources/test/lung-cancer3.owl";
	final String obdafile = "src/test/resources/test/lung-cancer3.obda";

	@Override
	public void setUp() throws Exception {
		
		
		/*
		 * Initializing and H2 database with the stock exchange data
		 */
		// String driver = "org.h2.Driver";
		String url = "jdbc:h2:mem:questjunitdb";
		String username = "sa";
		String password = "";

		fac = OBDADataFactoryImpl.getInstance();

		conn = DriverManager.getConnection(url, username, password);
		Statement st = conn.createStatement();

		FileReader reader = new FileReader("src/test/resources/test/lung-cancer3-create-h2.sql");
		BufferedReader in = new BufferedReader(reader);
		StringBuilder bf = new StringBuilder();
		String line = in.readLine();
		while (line != null) {
			bf.append(line + "\n");
			line = in.readLine();
		}

		st.executeUpdate(bf.toString());
		conn.commit();

		// Loading the OWL file
		OWLOntologyManager manager = OWLManager.createOWLOntologyManager();
		ontology = manager.loadOntologyFromOntologyDocument((new File(owlfile)));

		// Loading the OBDA data
		obdaModel = fac.getOBDAModel();
		
		ModelIOManager ioManager = new ModelIOManager(obdaModel);
		ioManager.load(obdafile);
		
	}

	@Override
	public void tearDown() throws Exception {

			dropTables();
			conn.close();
		
	}

	private void dropTables() throws SQLException, IOException {

		Statement st = conn.createStatement();

		FileReader reader = new FileReader("src/test/resources/test/lung-cancer3-drop-h2.sql");
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

	private void runTests(Properties p) throws Exception {

		// Creating a new instance of the reasoner
		QuestOWLFactory factory = new QuestOWLFactory();
		factory.setOBDAController(obdaModel);

		factory.setPreferenceHolder(p);

		QuestOWL reasoner = (QuestOWL) factory.createReasoner(ontology, new SimpleConfiguration());

		// Now we are ready for querying
		QuestOWLConnection conn = reasoner.getConnection();
		QuestOWLStatement st = conn.createStatement();

		String query1 = "PREFIX : <http://example.org/> SELECT * WHERE { ?x :hasNeoplasm <http://example.org/db1/neoplasm/1> }";
		String query2 = "PREFIX : <http://example.org/> SELECT * WHERE { <http://example.org/db1/1> :hasNeoplasm ?y }";
		String query3 = "PREFIX : <http://example.org/> SELECT * WHERE { ?y :hasStage <http://example.org/stages/II> }";
		String query4 = "PREFIX : <http://example.org/> SELECT * WHERE { ?y :hasStage <http://example.org/stages/limited> }";
		
		String query5 = "PREFIX : <http://example.org/db2/neoplasm/> DESCRIBE :1";
		String query6 = "PREFIX : <http://example.org/> DESCRIBE ?y WHERE { ?x :hasCondition ?y . ?y a :Cancer . } "; 
		String query7 = "PREFIX : <http://example.org/> DESCRIBE <http://example.org/db1/1>";
		String query8 = "PREFIX : <http://example.org/> DESCRIBE ?x WHERE { ?x a <http://example.org/Person> . } "; 
		
		String query9 = "PREFIX : <http://example.org/> CONSTRUCT { ?x ?y ?z } WHERE {  { ?x :name \"Mary\" .   ?x ?y ?z  } UNION {  ?x2 :name \"Mary\" . ?x2 :hasCondition ?x .   ?x ?y ?z } } ";
			
		
		
		String query = "PREFIX : <http://example.org/> SELECT * WHERE { ?y :hasStage <http://example.org/stages/limi> }";
		
		try {
			executeQueryAssertResults(query, st, 1);
			
//			executeQueryAssertResults(query3, st, 1);
//			executeQueryAssertResults(query4, st, 2);
//			executeQueryAssertResults(query1, st, 2);
//			executeQueryAssertResults(query2, st, 1);
//			
//			
//			executeGraphQueryAssertResults(query5, st, 8);
//			executeGraphQueryAssertResults(query6, st, 34);
//			executeGraphQueryAssertResults(query7, st, 4);
//			executeGraphQueryAssertResults(query8, st, 16);
//			executeGraphQueryAssertResults(query9, st, 9);
			 // NOTE CHECK THE CONTENT OF THIS QUERY, it seems to return the correct number of results but incorrect content, compare to the SELECT version which is correct
			

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
	
	public void executeQueryAssertResults(String query, QuestOWLStatement st, int expectedRows) throws Exception {
		QuestOWLResultSet rs = st.executeTuple(query);
		int count = 0;
		while (rs.nextRow()) {
			count++;
			for (int i = 1; i <= rs.getColumnCount(); i++) {
				System.out.print(rs.getSignature().get(i-1));
				System.out.print("=" + rs.getOWLObject(i));
				System.out.print(" ");
			}
			System.out.println();
		}
		rs.close();
		assertEquals(expectedRows, count);
	}
	
	public void executeGraphQueryAssertResults(String query, QuestOWLStatement st, int expectedRows) throws Exception {
		List<OWLAxiom> rs = st.executeGraph(query);
		int count = 0;
		Iterator<OWLAxiom> axit = rs.iterator();
		while (axit.hasNext()) {
			System.out.println(axit.next());			
			count++;
		}		
		assertEquals(expectedRows, count);
	}

	public void testViEqSig() throws Exception {

		QuestPreferences p = new QuestPreferences();
		p.setCurrentValueOf(QuestPreferences.ABOX_MODE, QuestConstants.VIRTUAL);
		p.setCurrentValueOf(QuestPreferences.OPTIMIZE_EQUIVALENCES, "true");
		p.setCurrentValueOf(QuestPreferences.OPTIMIZE_TBOX_SIGMA, "true");

		runTests(p);
	}
	
//	public void testClassicEqSig() throws Exception {
//
//		QuestPreferences p = new QuestPreferences();
//		p.setCurrentValueOf(QuestPreferences.ABOX_MODE, QuestConstants.CLASSIC);
//		p.setCurrentValueOf(QuestPreferences.OPTIMIZE_EQUIVALENCES, "true");
//		p.setCurrentValueOf(QuestPreferences.OPTIMIZE_TBOX_SIGMA, "true");
//		p.setCurrentValueOf(QuestPreferences.OBTAIN_FROM_MAPPINGS, "true");
//
//		runTests(p);
//	}


}
