package it.unibz.krdb.obda.unfold;

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
import org.semanticweb.owlapi.model.OWLAxiom;
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
public class LeftJoinTestVirtual extends TestCase {

	private OBDADataFactory fac;
	private Connection conn;

	Logger log = LoggerFactory.getLogger(this.getClass());
	private OBDAModel obdaModel;
	private OWLOntology ontology;

	final String owlfile = "src/test/resources/person.owl";
	final String obdafile = "src/test/resources/person.obda";

	@Override
	public void setUp() throws Exception {
		//String url = "jdbc:h2:mem:ljtest;DATABASE_TO_UPPER=FALSE";
		//String username = "sa";
		//String password = "";

		
		String url = "jdbc:mysql://obdalin3:3306/optional_test";
		String username = "fish";
		String password = "fish";

		fac = OBDADataFactoryImpl.getInstance();
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

		// Loading the OBDA data
		obdaModel = fac.getOBDAModel();
		
		ModelIOManager ioManager = new ModelIOManager(obdaModel);
		ioManager.load(obdafile);
		
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

		//@formatter.off
		String query_multi = "PREFIX : <http://www.example.org/test#> SELECT DISTINCT * WHERE "
				+ "{?p a :Person . OPTIONAL {{?p :salary ?salary .} UNION   {?p :name ?name .}}}";
		String query_multi1 = "PREFIX : <http://www.example.org/test#> SELECT DISTINCT * WHERE {?p a :Person . ?p :name ?name }";
		String query_multi2 = "PREFIX : <http://www.example.org/test#> SELECT DISTINCT * WHERE {?p a :Person . OPTIONAL {?p :name ?name} }";
		String query_multi3 = "PREFIX : <http://www.example.org/test#> SELECT DISTINCT * WHERE {?p :name ?name . OPTIONAL {?p :nick1 ?nick1} }";
		String query_multi4 = "PREFIX : <http://www.example.org/test#> SELECT DISTINCT * WHERE {?p a :Person . OPTIONAL {?p :name ?name . OPTIONAL {?p :nick1 ?nick1} } }";
		String query_multi5 = "PREFIX : <http://www.example.org/test#> SELECT DISTINCT * WHERE {?p a :Person . OPTIONAL {?p :name ?name . OPTIONAL {?p :nick1 ?nick1} OPTIONAL {?p :nick2 ?nick2} } }";
		
		String query_multi6 = "PREFIX : <http://www.example.org/test#> "
				+ "SELECT DISTINCT * "
				+ "WHERE {"
				+ "  ?p a :Person . "
				+ "  OPTIONAL {"
				+ "    ?p :name ?name . "
				+ "    OPTIONAL {"
				+ "      ?p :nick1 ?nick1 "
				+ "      OPTIONAL {?p :nick2 ?nick2} } } }";
		
		
		String query1 = "PREFIX : <http://www.example.org/test#> "
				+ "SELECT DISTINCT * WHERE "
				+ "{?p a :Person . ?p :name ?name . ?p :age ?age }";

		// fails now
		String query2 = "PREFIX : <http://www.example.org/test#> "
				+ "SELECT DISTINCT * "
				+ "WHERE {?p a :Person . ?p :name ?name . "
				+ "  OPTIONAL {?p :nick11 ?nick1} "
				+ "  OPTIONAL {?p :nick22 ?nick2} }";
		

		String query3 = "PREFIX : <http://www.example.org/test#> "
				+ "SELECT DISTINCT * "
				+ "WHERE {"
				+ "?p a :Person . ?p :name ?name . "
				+ "		OPTIONAL {?p :nick11 ?nick1} }";
		
		
		String query4 = "PREFIX : <http://www.example.org/test#> "
				+ "SELECT DISTINCT * "
				+ "WHERE {"
				+ " ?p a :Person . ?p :name ?name . "
				+ "  OPTIONAL {?p :nick1 ?nick1} "
				+ "  OPTIONAL {?p :nick2 ?nick2} }";
		
		String query5 = "PREFIX : <http://www.example.org/test#> "
				+ "SELECT DISTINCT * "
				+ "WHERE {"
				+ "  ?p a :Person . "
				+ "  ?p :name ?name ."
				+ "    OPTIONAL {?p :age ?age} }";
		
		String query6 = "PREFIX : <http://www.example.org/test#> "
				+ "SELECT * "
				+ "WHERE {"
				+ " ?p a :Person . ?p :name ?name ."
				+ " OPTIONAL {"
				+ "   ?p :nick1 ?nick1 "
				+ "   OPTIONAL {"
				+ "     {?p :nick2 ?nick2 } UNION {?p :nick22 ?nick22} } } }";
	
		String query7 = "PREFIX : <http://www.example.org/test#> "
				+ "SELECT DISTINCT * "
				+ "WHERE {"
				+ "  ?p a :Person . "
				+ "  ?p :name ?name ."
				+ "  OPTIONAL {"
				+ "    ?p :nick11 ?nick11 "
				+ "    OPTIONAL { {?p :nick33 ?nick33 } UNION {?p :nick22 ?nick22} } } }";
		//@formatter.on
		
		try {
//			executeQueryAssertResults(query_multi, st, 4);
			//executeQueryAssertResults(query_multi1, st, 4);
			//executeQueryAssertResults(query_multi2, st, 4);
			//executeQueryAssertResults(query_multi3, st, 4);
			//executeQueryAssertResults(query_multi4, st, 4);
			//executeQueryAssertResults(query_multi5, st, 4);
			executeQueryAssertResults(query_multi6, st, 4);

			//executeQueryAssertResults(query1, st, 3);
			//executeQueryAssertResults(query2, st, 4);
//			executeQueryAssertResults(query3, st, 4);
//			executeQueryAssertResults(query4, st, 4);
//			executeQueryAssertResults(query5, st, 4);
//			executeQueryAssertResults(query6, st, 6);
//			executeQueryAssertResults(query7, st, 4);

			
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

	public void testLeftJoin() throws Exception {

		QuestPreferences p = new QuestPreferences();
		runTests(p);
	}

}
