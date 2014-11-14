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
import it.unibz.krdb.obda.owlrefplatform.core.QuestPreferences;
import it.unibz.krdb.obda.owlrefplatform.owlapi3.*;
import junit.framework.TestCase;
import org.semanticweb.owlapi.apibinding.OWLManager;
import org.semanticweb.owlapi.model.OWLOntology;
import org.semanticweb.owlapi.model.OWLOntologyManager;
import org.semanticweb.owlapi.reasoner.SimpleConfiguration;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.File;
import java.sql.Connection;
import java.util.Properties;


public class ValuesTestVirtual extends TestCase {

	private OBDADataFactory fac;
	private Connection conn;

	Logger log = LoggerFactory.getLogger(this.getClass());
	private OBDAModel obdaModel;
	private OWLOntology ontology;

	final String owlfile = "src/test/resources/person.owl";
	final String obdafile = "src/test/resources/person.obda";

	@Override
	public void setUp() throws Exception {

		fac = OBDADataFactoryImpl.getInstance();
		
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

		QuestOWL reasoner = factory.createReasoner(ontology, new SimpleConfiguration());

		// Now we are ready for querying
		QuestOWLConnection conn = reasoner.getConnection();
		QuestOWLStatement st = conn.createStatement();

		String prefix = "PREFIX : <http://www.semanticweb.org/mindaugas/ontologies/2013/9/untitled-ontology-58#> ";

		String query1 = prefix +
				"SELECT * " +
				"WHERE {" +
				"   ?p a :Person ; :name ?name . " +
				"}";
		String query2 = prefix +
				"SELECT * " +
				"WHERE {" +
				"   ?p a :Person ; :name ?name . " +
				"   FILTER ( ?name = \"Alice\" ) " +
				"}";
		String query3 =  prefix +
				"SELECT * " +
				"WHERE {" +
				"  ?p a :Person ; :name ?name ." +
				"  VALUES ?name { \"Alice\" } " +
				"}";
		String query4 = prefix +
				"SELECT * " +
				"WHERE {" +
				"   ?p a :Person ; :name ?name . " +
				"   FILTER ( ?name = \"Alice\" || ?name = \"Bob\" ) " +
				"}";
		String query5 =  prefix +
				"SELECT * " +
				"WHERE {" +
				"  ?p a :Person ; :name ?name ." +
				"  VALUES ?name { \"Alice\" \"Bob\" } " +
				"}";
		String query6 =  prefix +
				"SELECT * " +
				"WHERE {" +
				"  ?p a :Person ; :name ?name ; :age ?age ." +
				"  FILTER ( ?name  = \"Alice\" && ?age = 18 ) " +
				"}";
		String query7 =  prefix +
				"SELECT * " +
				"WHERE {" +
				"  ?p a :Person ; :name ?name ; :age ?age ." +
				"  VALUES (?name ?age) { (\"Alice\" 18) } " +
				"}";
		String query8 =  prefix +
				"SELECT * " +
				"WHERE {" +
				"  ?p a :Person ; :name ?name ." +
				"  VALUES ?name { \"Nobody\" } " +
				"}";
		String query9 =  prefix +
				"SELECT * " +
				"WHERE {" +
				"  ?p a :Person ; :name ?name ; :age ?age ." +
				"  VALUES (?name ?age) { (\"Alice\" UNDEF) } " +
				"}";
		String query10 =  prefix +
				"SELECT * " +
				"WHERE {" +
				"  ?p a :Person ; :name ?name ; :age ?age ." +
				"  VALUES (?name ?age) { (\"Mark\" UNDEF) } " +
				"}";
		String query11 =  prefix +
				"SELECT * " +
				"WHERE {" +
				"  ?p a :Person ; :name ?name ; :age ?age ." +
				"  VALUES (?name ?age) { " +
				"     (\"Alice\" 18) " +
				"     (\"Bob\" 19) " +
				"  } " +
				"}";
		String query12 =  prefix +
				"SELECT * " +
				"WHERE {" +
				"  VALUES (?name ?age) { " +
				"     (\"Alice\" 18) " +
				"     (\"Bob\" 19) " +
				"  } " +
				"  ?p a :Person ; :name ?name ; :age ?age ." +
				"}";
		
		try {
			executeQueryAssertResults(query1, st, 4);
			executeQueryAssertResults(query2, st, 1);
			executeQueryAssertResults(query3, st, 1);
			executeQueryAssertResults(query4, st, 2);
			executeQueryAssertResults(query5, st, 2);
			executeQueryAssertResults(query6, st, 1);
			executeQueryAssertResults(query7, st, 1);
			executeQueryAssertResults(query8, st, 0);
			executeQueryAssertResults(query9, st, 1);
			executeQueryAssertResults(query10, st, 0);
			executeQueryAssertResults(query11, st, 2);
			executeQueryAssertResults(query12, st, 2);
			
		} catch (Exception e) {
			throw e;
		} finally {
			try {

			} catch (Exception e) {
				st.close();
				assertTrue(false);
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

	public void testLeftJoin() throws Exception {

		QuestPreferences p = new QuestPreferences();
		runTests(p);
	}

}
