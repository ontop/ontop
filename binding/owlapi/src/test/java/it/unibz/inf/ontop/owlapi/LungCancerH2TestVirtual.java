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

import java.sql.Connection;
import java.sql.DriverManager;

import it.unibz.inf.ontop.injection.OntopSQLOWLAPIConfiguration;
import it.unibz.inf.ontop.owlapi.connection.OWLConnection;
import it.unibz.inf.ontop.owlapi.connection.OWLStatement;
import it.unibz.inf.ontop.owlapi.resultset.GraphOWLResultSet;
import it.unibz.inf.ontop.owlapi.resultset.OWLBindingSet;
import it.unibz.inf.ontop.owlapi.resultset.TupleOWLResultSet;
import junit.framework.TestCase;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import static it.unibz.inf.ontop.utils.OWLAPITestingTools.executeFromFile;

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

	private Connection conn;

	private static final String owlfile = "src/test/resources/test/lung-cancer3.owl";
	private static final String obdafile = "src/test/resources/test/lung-cancer3.obda";

	private static final String url = "jdbc:h2:mem:questjunitdb";
	private static final String username = "sa";
	private static final String password = "";

	@Override
	public void setUp() throws Exception {
		conn = DriverManager.getConnection(url, username, password);
		executeFromFile(conn, "src/test/resources/test/lung-cancer3-create-h2.sql");
	}

	@Override
	public void tearDown() throws Exception {
		executeFromFile(conn, "src/test/resources/test/lung-cancer3-drop-h2.sql");
		conn.close();
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
        OntopOWLReasoner reasoner = factory.createReasoner(config);

		// Now we are ready for querying
		OWLConnection conn = reasoner.getConnection();

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
		
		try (OWLStatement st = conn.createStatement()) {
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
			

		}
		finally {
			conn.close();
			reasoner.dispose();
		}
	}
	
	public void executeQueryAssertResults(String query, OWLStatement st, int expectedRows) throws Exception {
		TupleOWLResultSet rs = st.executeSelectQuery(query);
		int count = 0;
		while (rs.hasNext()) {
            final OWLBindingSet bindingSet = rs.next();
            count++;
			for (String name: rs.getSignature()) {
				System.out.print(name);
				System.out.print("=" + bindingSet.getOWLObject(name));
				System.out.print(" ");
			}
			System.out.println();
		}
		rs.close();
		assertEquals(expectedRows, count);
	}
	
	public void executeConstructQueryAssertResults(String query, OWLStatement st, int expectedRows) throws Exception {
		GraphOWLResultSet rs = st.executeConstructQuery(query);
		int count = 0;
		while (rs.hasNext()) {
			System.out.println(rs.next());
			count++;
		}		
		assertEquals(expectedRows, count);
	}

	public void testViEqSig() throws Exception {

		runTests();
	}
	
//	public void testClassicEqSig() throws Exception {
//
//		Properties p = new Properties();
//		p.setProperty(QuestPreferences.ABOX_MODE, QuestConstants.CLASSIC);
//		p.setProperty(QuestPreferences.OPTIMIZE_EQUIVALENCES, "true");
//		p.setProperty(QuestPreferences.OPTIMIZE_TBOX_SIGMA, "true");
//		p.setProperty(QuestPreferences.OBTAIN_FROM_MAPPINGS, "true");
//
//		runTests(new QuestPreferences(p));
//	}


}
