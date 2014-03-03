package it.unibz.krdb.obda.reformulation.tests;

/*
 * #%L
 * ontop-quest-owlapi3
 * %%
 * Copyright (C) 2009 - 2013 Free University of Bozen-Bolzano
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

import it.unibz.krdb.obda.io.ModelIOManager;
import it.unibz.krdb.obda.model.OBDADataFactory;
import it.unibz.krdb.obda.model.OBDAModel;
import it.unibz.krdb.obda.model.impl.OBDADataFactoryImpl;
import it.unibz.krdb.obda.owlrefplatform.core.QuestConstants;
import it.unibz.krdb.obda.owlrefplatform.core.QuestPreferences;
import it.unibz.krdb.obda.owlrefplatform.owlapi3.QuestOWL;
import it.unibz.krdb.obda.owlrefplatform.owlapi3.QuestOWLConnection;
import it.unibz.krdb.obda.owlrefplatform.owlapi3.QuestOWLFactory;
import it.unibz.krdb.obda.owlrefplatform.owlapi3.QuestOWLResultSet;
import it.unibz.krdb.obda.owlrefplatform.owlapi3.QuestOWLStatement;

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

import org.semanticweb.owlapi.apibinding.OWLManager;
import org.semanticweb.owlapi.model.OWLAnonymousIndividual;
import org.semanticweb.owlapi.model.OWLAxiom;
import org.semanticweb.owlapi.model.OWLOntology;
import org.semanticweb.owlapi.model.OWLOntologyManager;
import org.semanticweb.owlapi.reasoner.SimpleConfiguration;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class AggregatesTest extends TestCase {

	private OBDADataFactory fac;
	private Connection conn;

	Logger log = LoggerFactory.getLogger(this.getClass());
	private OBDAModel obdaModel;
	private OWLOntology ontology;

	final String owlfile = "src/test/resources/test/stockexchange-unittest.owl";
	final String obdafile = "src/test/resources/test/stockexchange-h2-unittest.obda";

	@Override
	public void setUp() throws Exception {
				
		/*
		 * Initializing and H2 database with the stock exchange data
		 */
		String driver = "org.h2.Driver";
		String url = "jdbc:h2:mem:questjunitdb";
		String username = "sa";
		String password = "";

		fac = OBDADataFactoryImpl.getInstance();

		conn = DriverManager.getConnection(url, username, password);
		Statement st = conn.createStatement();

		FileReader reader = new FileReader("src/test/resources/test/stockexchange-create-h2.sql");
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
		try {
			dropTables();
			conn.close();
		} catch (Exception e) {
			log.debug(e.getMessage());
		}
	}

	private void dropTables() throws SQLException, IOException {

		Statement st = conn.createStatement();

		FileReader reader = new FileReader("src/test/resources/test/stockexchange-drop-h2.sql");
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

		//test queries
		String query1 = "PREFIX : <http://www.owl-ontologies.com/Ontology1207768242.owl#> SELECT DISTINCT * WHERE {?x a :Stock }";
		String query1_1 = "PREFIX : <http://www.owl-ontologies.com/Ontology1207768242.owl#> SELECT (?value AS ?new) WHERE {?x a :Transaction. ?x :amountOfTransaction ?value }";
		
		// COUNT
		String query2 = "PREFIX : <http://www.owl-ontologies.com/Ontology1207768242.owl#> SELECT (COUNT(?value) AS ?count) WHERE {?x a :Transaction. ?x :amountOfTransaction ?value }";
		String query2_1 = "PREFIX : <http://www.owl-ontologies.com/Ontology1207768242.owl#> SELECT ?broker (COUNT(?value) AS ?count) WHERE {?x a :Transaction. ?x :isExecutedBy ?broker. ?x :amountOfTransaction ?value } GROUP BY ?broker";
		String query2_2 = "PREFIX : <http://www.owl-ontologies.com/Ontology1207768242.owl#> SELECT ?x (COUNT(?value) AS ?count) WHERE {?x a :Transaction. ?x :amountOfTransaction ?value } GROUP BY ?x HAVING (COUNT(?value) > 0)";
		
		String query3 = "PREFIX : <http://www.owl-ontologies.com/Ontology1207768242.owl#> SELECT ?broker (AVG(?value) AS ?vavg) WHERE {?x :isExecutedBy ?broker. ?x :amountOfTransaction ?value } GROUP BY ?broker";
		String query4 = "PREFIX : <http://www.owl-ontologies.com/Ontology1207768242.owl#> SELECT (SUM(?value) AS ?sum) WHERE {?x a :Transaction. ?x :amountOfTransaction ?value }";
		
		String query5 = "PREFIX : <http://www.owl-ontologies.com/Ontology1207768242.owl#> SELECT ?x ?value ?date WHERE {?x a :Transaction. ?x :amountOfTransaction ?value OPTIONAL {?x :transactionDate ?date} } GROUP BY ?x ?value ?date";
		
		String query6 = "PREFIX : <http://www.owl-ontologies.com/Ontology1207768242.owl#> SELECT ?value ?broker WHERE {{?x a :Transaction. ?x :amountOfTransaction ?value } UNION {?x :isExecutedBy ?broker} } GROUP BY ?value ?broker";
		try {
	//		executeQueryAssertResults(query2_2, st, 1);
//			executeQueryAssertResults(query1, st, 10);
//			executeQueryAssertResults(query1_1, st, 4);
			executeQueryAssertResults(query2, st, 1);
//			executeQueryAssertResults(query2_1, st, 1);
//			executeQueryAssertResults(query2_2, st, 1);
			
			//executeQueryAssertResults(query3, st, 1);
			//executeQueryAssertResults(query4, st, 1);
			//executeQueryAssertResults(query5, st, 4);
			//executeQueryAssertResults(query6, st, 7);
		} catch (Exception e) {
			throw e;
		} finally {
			try {

			} catch (Exception e) {
				st.close();
			}
			conn.close();
			reasoner.dispose();
		}
	}
	
	public void executeQueryAssertValue(String query, QuestOWLStatement st, int expectedValue) throws Exception {
		QuestOWLResultSet rs = st.executeTuple(query);
		rs.nextRow();
		int count = rs.getCount();
		System.out.print(rs.getSignature().get(0));
		System.out.print("=" + count);
		System.out.print(" ");
		System.out.println();
		rs.close();
		assertEquals(expectedValue, count);
	}
	
	public void executeQueryAssertResults(String query, QuestOWLStatement st, int expectedRows) throws Exception {
		QuestOWLResultSet rs = st.executeTuple(query);
		int count = 0;
		while (rs.nextRow()) {
			count++;
			for (int i = 1; i <= rs.getColumCount(); i++) {
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

	public void testAggr() throws Exception {

		QuestPreferences p = new QuestPreferences();
		p.setCurrentValueOf(QuestPreferences.ABOX_MODE, QuestConstants.VIRTUAL);
		p.setCurrentValueOf(QuestPreferences.OPTIMIZE_EQUIVALENCES, "true");
		p.setCurrentValueOf(QuestPreferences.OPTIMIZE_TBOX_SIGMA, "true");

		runTests(p);
	}
	
}
