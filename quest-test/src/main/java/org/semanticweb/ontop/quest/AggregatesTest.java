package org.semanticweb.ontop.quest;

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

import org.semanticweb.ontop.io.ModelIOManager;
import org.semanticweb.ontop.model.OBDADataFactory;
import org.semanticweb.ontop.model.SQLOBDAModel;
import org.semanticweb.ontop.model.impl.OBDADataFactoryImpl;
import org.semanticweb.ontop.owlrefplatform.core.QuestConstants;
import org.semanticweb.ontop.owlrefplatform.core.QuestPreferences;
import org.semanticweb.ontop.owlrefplatform.owlapi3.QuestOWL;
import org.semanticweb.ontop.owlrefplatform.owlapi3.QuestOWLConnection;
import org.semanticweb.ontop.owlrefplatform.owlapi3.QuestOWLFactory;
import org.semanticweb.ontop.owlrefplatform.owlapi3.QuestOWLResultSet;
import org.semanticweb.ontop.owlrefplatform.owlapi3.QuestOWLStatement;

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

import junit.framework.TestCase;

import org.semanticweb.owlapi.apibinding.OWLManager;
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
	private SQLOBDAModel obdaModel;
	private OWLOntology ontology;

	final String owlfile = "src/test/resources/test/stockexchange-unittest.owl";
	final String obdafile = "src/test/resources/test/stockexchange-mysql-unittest.obda";

	@Override
	public void setUp() throws Exception {
				
		/*
		 * Initializing and H2 database with the stock exchange data
		 */
		String driver = "com.mysql.jdbc.Driver";
		String url = "jdbc:mysql://10.7.20.39/stockexchange";
		String username = "fish";
		String password = "fish";

		//?sessionVariables=sql_mode='ANSI'
		
		fac = OBDADataFactoryImpl.getInstance();

		conn = DriverManager.getConnection(url, username, password);
		conn.setAutoCommit(true);
		Statement st = conn.createStatement();

		FileReader reader = new FileReader("src/test/resources/test/stockexchange-create-mysql.sql");
		BufferedReader in = new BufferedReader(reader);
		StringBuilder bf = new StringBuilder();
		String line = in.readLine();
		while (line != null) {
			bf.append(line + "\n");
			line = in.readLine();
		}
		String[] sqls = bf.toString().split(";");
		for (String st_sql: sqls) {
			st.addBatch(st_sql);
		}
		//st.executeBatch();
		//st.executeUpdate(bf.toString());
		//conn.commit();

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
		//	dropTables();
			conn.close();
		} catch (Exception e) {
			log.debug(e.getMessage());
		}
	}

	private void dropTables() throws SQLException, IOException {

		Statement st = conn.createStatement();

		FileReader reader = new FileReader("src/test/resources/test/stockexchange-drop-mysql.sql");
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

	private void runTests(QuestPreferences p, String query, int expectedvalue) throws Exception {

		// Creating a new instance of the reasoner
		QuestOWLFactory factory = new QuestOWLFactory();
		factory.setOBDAController(obdaModel);

		factory.setPreferenceHolder(p);

		QuestOWL reasoner = (QuestOWL) factory.createReasoner(ontology, new SimpleConfiguration());

		// Now we are ready for querying
		QuestOWLConnection conn = reasoner.getConnection();
		QuestOWLStatement st = conn.createStatement();

		//test queries
		
		
		
		try {
		
			

			//TODO: Groupby using more than 1 variable
			//TODO: Add tests for the rest of the aggregates!!
			//TODO: Add value test.... check if the answer is correct, not just rows
			
			
			
			executeQueryAssertResults(query, st, expectedvalue);
			
			/*
			 * String query1 = "PREFIX : <http://www.owl-ontologies.com/Ontology1207768242.owl#> SELECT DISTINCT * WHERE {?x a :Stock }";
		String query1_1 = "PREFIX : <http://www.owl-ontologies.com/Ontology1207768242.owl#> SELECT (?value AS ?new) WHERE {?x a :Transaction. ?x :amountOfTransaction ?value }";
		
		// COUNT
		
		
		String query6 = "PREFIX : <http://www.owl-ontologies.com/Ontology1207768242.owl#> SELECT ?value ?broker WHERE {{?x a :Transaction. ?x :amountOfTransaction ?value } UNION {?x :isExecutedBy ?broker} } "; // GROUP BY ?value  ?broker

		
		executeQueryAssertResults(query1, st, 10);
			executeQueryAssertResults(query6, st, 7);
			executeQueryAssertResults(query1_1, st, 4);
		

			 * 
			 */
			
			
		} catch (Exception e) {
			throw e;
		} finally {
			conn.close();
			st.close();
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

	public void testAggrCount() throws Exception {

		QuestPreferences p = new QuestPreferences();
		p.setCurrentValueOf(QuestPreferences.ABOX_MODE, QuestConstants.VIRTUAL);
		p.setCurrentValueOf(QuestPreferences.OPTIMIZE_EQUIVALENCES, "true");
		p.setCurrentValueOf(QuestPreferences.OPTIMIZE_TBOX_SIGMA, "true");
		String query = "PREFIX : <http://www.owl-ontologies.com/Ontology1207768242.owl#> SELECT (COUNT(?value) AS ?count) WHERE {?x a :Transaction. ?x :amountOfTransaction ?value }";

		runTests(p,query,1);
	}


	public void testAggrCount2() throws Exception {

		QuestPreferences p = new QuestPreferences();
		p.setCurrentValueOf(QuestPreferences.ABOX_MODE, QuestConstants.VIRTUAL);
		p.setCurrentValueOf(QuestPreferences.OPTIMIZE_EQUIVALENCES, "true");
		p.setCurrentValueOf(QuestPreferences.OPTIMIZE_TBOX_SIGMA, "true");
		String query = "PREFIX : <http://www.owl-ontologies.com/Ontology1207768242.owl#> SELECT ?broker (COUNT(?value) AS ?count) WHERE {?x a :Transaction. ?x :isExecutedBy ?broker. ?x :amountOfTransaction ?value } GROUP BY ?broker";

		runTests(p,query,1);

	}
	
	public void testAggrCount3() throws Exception {

		QuestPreferences p = new QuestPreferences();
		p.setCurrentValueOf(QuestPreferences.ABOX_MODE, QuestConstants.VIRTUAL);
		p.setCurrentValueOf(QuestPreferences.OPTIMIZE_EQUIVALENCES, "true");
		p.setCurrentValueOf(QuestPreferences.OPTIMIZE_TBOX_SIGMA, "true");
		String query = "PREFIX : <http://www.owl-ontologies.com/Ontology1207768242.owl#> SELECT ?x (COUNT(?value) AS ?count) WHERE {?x a :Transaction. ?x :amountOfTransaction ?value } GROUP BY ?x";

		runTests(p,query,4);

	}

	public void testAggrCount4() throws Exception {

		QuestPreferences p = new QuestPreferences();
		p.setCurrentValueOf(QuestPreferences.ABOX_MODE, QuestConstants.VIRTUAL);
		p.setCurrentValueOf(QuestPreferences.OPTIMIZE_EQUIVALENCES, "true");
		p.setCurrentValueOf(QuestPreferences.OPTIMIZE_TBOX_SIGMA, "true");
		String query = "PREFIX : <http://www.owl-ontologies.com/Ontology1207768242.owl#> SELECT ?x (COUNT(?y) AS ?count) WHERE { ?x :belongsToCompany ?y } GROUP BY ?x";
		//String query = "PREFIX : <http://www.owl-ontologies.com/Ontology1207768242.owl#> SELECT ?x ?y WHERE { ?x :belongsToCompany ?y } ";

		runTests(p,query,10);

	}
	
	public void testAggrCount5() throws Exception {

		QuestPreferences p = new QuestPreferences();
		p.setCurrentValueOf(QuestPreferences.ABOX_MODE, QuestConstants.VIRTUAL);
		p.setCurrentValueOf(QuestPreferences.OPTIMIZE_EQUIVALENCES, "true");
		p.setCurrentValueOf(QuestPreferences.OPTIMIZE_TBOX_SIGMA, "true");
		String query = "PREFIX : <http://www.owl-ontologies.com/Ontology1207768242.owl#> SELECT (COUNT(?x) AS ?count) WHERE {?x a :Transaction. }";

		runTests(p,query,1);
	}
	
	/*
	public void testAggrCount5() throws Exception {

		QuestPreferences p = new QuestPreferences();
		p.setCurrentValueOf(QuestPreferences.ABOX_MODE, QuestConstants.VIRTUAL);
		p.setCurrentValueOf(QuestPreferences.OPTIMIZE_EQUIVALENCES, "true");
		p.setCurrentValueOf(QuestPreferences.OPTIMIZE_TBOX_SIGMA, "true");
		String query = "PREFIX : <http://www.owl-ontologies.com/Ontology1207768242.owl#> " +
				"SELECT ?x (COUNT(?value) AS ?count) " +
				"WHERE {?x a :Transaction. ?x :amountOfTransaction ?value } " +
				"GROUP BY ?x HAVING (?value > 0)";

		runTests(p,query,3);

	}
	
*/
	
	
	public void testAggrAVG() throws Exception {

		QuestPreferences p = new QuestPreferences();
		p.setCurrentValueOf(QuestPreferences.ABOX_MODE, QuestConstants.VIRTUAL);
		p.setCurrentValueOf(QuestPreferences.OPTIMIZE_EQUIVALENCES, "true");
		p.setCurrentValueOf(QuestPreferences.OPTIMIZE_TBOX_SIGMA, "true");
		String query = "PREFIX : <http://www.owl-ontologies.com/Ontology1207768242.owl#> SELECT ?broker (AVG(?value) AS ?vavg) WHERE {?x :isExecutedBy ?broker. ?x :amountOfTransaction ?value } GROUP BY ?broker";

		runTests(p,query,1);

	}
	
	public void testAggrSUM() throws Exception {

		QuestPreferences p = new QuestPreferences();
		p.setCurrentValueOf(QuestPreferences.ABOX_MODE, QuestConstants.VIRTUAL);
		p.setCurrentValueOf(QuestPreferences.OPTIMIZE_EQUIVALENCES, "true");
		p.setCurrentValueOf(QuestPreferences.OPTIMIZE_TBOX_SIGMA, "true");
		String query = "PREFIX : <http://www.owl-ontologies.com/Ontology1207768242.owl#> SELECT (SUM(?value) AS ?sum) WHERE {?x a :Transaction. ?x :amountOfTransaction ?value }";

		runTests(p,query,1);

	}
	
	public void testAggrMIN() throws Exception {

		QuestPreferences p = new QuestPreferences();
		p.setCurrentValueOf(QuestPreferences.ABOX_MODE, QuestConstants.VIRTUAL);
		p.setCurrentValueOf(QuestPreferences.OPTIMIZE_EQUIVALENCES, "true");
		p.setCurrentValueOf(QuestPreferences.OPTIMIZE_TBOX_SIGMA, "true");
		String query = "PREFIX : <http://www.owl-ontologies.com/Ontology1207768242.owl#> SELECT (MIN(?value) AS ?min) WHERE {?x a :Transaction. ?x :amountOfTransaction ?value }";

		runTests(p,query,1);

	}
	
	public void testAggrMAX() throws Exception {

		QuestPreferences p = new QuestPreferences();
		p.setCurrentValueOf(QuestPreferences.ABOX_MODE, QuestConstants.VIRTUAL);
		p.setCurrentValueOf(QuestPreferences.OPTIMIZE_EQUIVALENCES, "true");
		p.setCurrentValueOf(QuestPreferences.OPTIMIZE_TBOX_SIGMA, "true");
		String query = "PREFIX : <http://www.owl-ontologies.com/Ontology1207768242.owl#> SELECT (MAX(?value) AS ?max) WHERE {?x a :Transaction. ?x :amountOfTransaction ?value }";

		runTests(p,query,1);

	}
	
	

	
}
