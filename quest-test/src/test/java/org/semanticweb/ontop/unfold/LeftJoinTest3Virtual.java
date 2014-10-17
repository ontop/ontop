package org.semanticweb.ontop.unfold;

/*
 * #%L
 * ontop-test
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

import org.semanticweb.ontop.owlrefplatform.core.QuestPreferences;
import org.semanticweb.ontop.owlrefplatform.owlapi3.QuestOWL;
import org.semanticweb.ontop.owlrefplatform.owlapi3.QuestOWLConnection;
import org.semanticweb.ontop.owlrefplatform.owlapi3.QuestOWLFactory;
import org.semanticweb.ontop.owlrefplatform.owlapi3.QuestOWLResultSet;
import org.semanticweb.ontop.owlrefplatform.owlapi3.QuestOWLStatement;

import java.io.File;
import java.util.Properties;

import junit.framework.TestCase;

import org.semanticweb.owlapi.apibinding.OWLManager;
import org.semanticweb.owlapi.model.OWLOntology;
import org.semanticweb.owlapi.model.OWLOntologyManager;
import org.semanticweb.owlapi.reasoner.SimpleConfiguration;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Class to check the translation of the combination of Optional/Union in SPARQL into Datalog, and finally 
 * SQL
 * @author Minda, Guohui, mrezk
 */
public class LeftJoinTest3Virtual extends TestCase {

	Logger log = LoggerFactory.getLogger(this.getClass());
	private OWLOntology ontology;

	final String owlfile = "src/test/resources/person.owl";
	final String obdafile = "src/test/resources/person3.obda";

	@Override
	public void setUp() throws Exception {
		//String url = "jdbc:h2:mem:ljtest;DATABASE_TO_UPPER=FALSE";
		//String username = "sa";
		//String password = "";

		
		String url = "jdbc:mysql://obdalin3:3306/optional_test";
		String username = "fish";
		String password = "fish";
/*
		conn = DriverManager.getConnection(url, username, password);
		log.debug("Creating in-memory DB and inserting data!");
		Statement st = conn.createStatement();
		FileReader reader = new FileReader("src/test/resources/create_optional.sql");
		BufferedReader in = new BufferedReader(reader);
		StringBuilder bf = new StringBuilder();
		String line = in.readLine();
		while (line != null) {
			bf.append(line + "\n");
			line = in.readLine();
		}

		st.executeUpdate(bf.toString());
		conn.commit();
		log.debug("Data loaded!");
*/
		// Loading the OWL file
		OWLOntologyManager manager = OWLManager.createOWLOntologyManager();
		ontology = manager.loadOntologyFromOntologyDocument((new File(owlfile)));
		
	}

	private void runTests(Properties p) throws Exception {

		// Creating a new instance of the reasoner
        QuestOWLFactory factory = new QuestOWLFactory(new File(obdafile), p);

		QuestOWL reasoner = (QuestOWL) factory.createReasoner(ontology, new SimpleConfiguration());

		// Now we are ready for querying
		QuestOWLConnection conn = reasoner.getConnection();
		QuestOWLStatement st = conn.createStatement();

		//@formatter.off
		String query_multi1 = "PREFIX : <http://www.example.org/test#> "
				+ "SELECT DISTINCT * "
				+ "WHERE {"
				+ "  ?p a :Person . "
				+ "  ?p :name ?name . "
				+ "  OPTIONAL {?p :nick ?nick} }";
		
		//@formatter.on
		
		try {
			executeQueryAssertResults(query_multi1, st, 5);

			
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
	
	public void executeQueryAssertResults(String query, QuestOWLStatement st, int expectedRows) throws Exception {
		QuestOWLResultSet rs = st.executeTuple(query);
		int count = 0;
		while (rs.nextRow()) {
			count++;
			for (int i = 1; i <= rs.getColumnCount(); i++) {
				String varName = rs.getSignature().get(i-1);
				System.out.print(varName);
				//System.out.print("=" + rs.getOWLObject(i));
				System.out.print("=" + rs.getOWLObject(varName));
				System.out.print(" ");
			}
			System.out.println();
		}
		rs.close();
		assertEquals(expectedRows, count);
	}

	public void testLeftJoin() throws Exception {

		QuestPreferences p = new QuestPreferences();
		runTests(p);
	}

}
