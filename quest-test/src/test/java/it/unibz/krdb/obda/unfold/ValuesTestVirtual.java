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
import it.unibz.krdb.obda.owlrefplatform.owlapi3.*;
import org.junit.Ignore;
import org.junit.Test;
import org.semanticweb.owlapi.apibinding.OWLManager;
import org.semanticweb.owlapi.model.OWLOntology;
import org.semanticweb.owlapi.model.OWLOntologyManager;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.File;

import static junit.framework.TestCase.assertEquals;
import static org.junit.Assert.assertTrue;


public class ValuesTestVirtual {

	private OBDADataFactory fac;
	private QuestOWLConnection conn;

	Logger log = LoggerFactory.getLogger(this.getClass());
	private OBDAModel obdaModel;
	private OWLOntology ontology;

	final String owlfile = "src/test/resources/person.owl";
	final String obdafile = "src/test/resources/person.obda";
	private QuestOWL reasoner;
	private static final String PREFIX = "PREFIX : <http://www.semanticweb.org/mindaugas/ontologies/2013/9/untitled-ontology-58#> ";



	public ValuesTestVirtual() throws Exception {

		fac = OBDADataFactoryImpl.getInstance();
		
		// Loading the OWL file
		OWLOntologyManager manager = OWLManager.createOWLOntologyManager();
		ontology = manager.loadOntologyFromOntologyDocument((new File(owlfile)));

		// Loading the OBDA data
		obdaModel = fac.getOBDAModel();
		
		ModelIOManager ioManager = new ModelIOManager(obdaModel);
		ioManager.load(obdafile);

		// Creating a new instance of the reasoner
		QuestOWLFactory factory = new QuestOWLFactory();

        QuestOWLConfiguration config = QuestOWLConfiguration.builder().obdaModel(obdaModel).build();

		reasoner = factory.createReasoner(ontology, config);

		// Now we are ready for querying
		conn = reasoner.getConnection();
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

	private void runTest(String query, int expectedRows) throws Exception {
		QuestOWLStatement st = conn.createStatement();

		try {
			executeQueryAssertResults(query, st, expectedRows);

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

	@Test
	public void testQ01() throws Exception {
		String query1 = PREFIX +
				"SELECT * " +
				"WHERE {" +
				"   ?p a :Person ; :name ?name . " +
				"}";
		runTest(query1, 4);
	}

	@Test
	public void testQ02() throws Exception {
		String query2 = PREFIX +
				"SELECT * " +
				"WHERE {" +
				"   ?p a :Person ; :name ?name . " +
				"   FILTER ( ?name = \"Alice\" ) " +
				"}";
		runTest(query2, 1);
	}

	@Test
	public void testQ03() throws Exception {
		String query3 =  PREFIX +
				"SELECT * " +
				"WHERE {" +
				"  ?p a :Person ; :name ?name ." +
				"  VALUES ?name { \"Alice\" } " +
				"}";
		runTest(query3, 1);
	}

	@Test
	public void testQ04() throws Exception {
		String query4 = PREFIX +
				"SELECT * " +
				"WHERE {" +
				"   ?p a :Person ; :name ?name . " +
				"   FILTER ( ?name = \"Alice\" || ?name = \"Bob\" ) " +
				"}";
		runTest(query4, 2);
	}

	@Test
	public void testQ05() throws Exception {
		String query5 =  PREFIX +
				"SELECT * " +
				"WHERE {" +
				"  ?p a :Person ; :name ?name ." +
				"  VALUES ?name { \"Alice\" \"Bob\" } " +
				"}";
		runTest(query5, 2);
	}

	@Test
	public void testQ06() throws Exception {
		String query6 =  PREFIX +
				"SELECT * " +
				"WHERE {" +
				"  ?p a :Person ; :name ?name ; :age ?age ." +
				"  FILTER ( ?name  = \"Alice\" && ?age = 18 ) " +
				"}";
		runTest(query6, 1);
	}

	@Test
	public void testQ07() throws Exception {
		String query7 =  PREFIX +
				"SELECT * " +
				"WHERE {" +
				"  ?p a :Person ; :name ?name ; :age ?age ." +
				"  VALUES (?name ?age) { (\"Alice\" 18) } " +
				"}";
		runTest(query7, 1);
	}

	@Test
	public void testQ08() throws Exception {
		String query8 =  PREFIX +
				"SELECT * " +
				"WHERE {" +
				"  ?p a :Person ; :name ?name ." +
				"  VALUES ?name { \"Nobody\" } " +
				"}";
		runTest(query8, 0);
	}

	@Test
	public void testQ09() throws Exception {
		String query9 =  PREFIX +
				"SELECT * " +
				"WHERE {" +
				"  ?p a :Person ; :name ?name ; :age ?age ." +
				"  VALUES (?name ?age) { (\"Alice\" UNDEF) } " +
				"}";
		runTest(query9, 1);
	}

	@Test
	public void testQ10() throws Exception {
		String query10 =  PREFIX +
				"SELECT * " +
				"WHERE {" +
				"  ?p a :Person ; :name ?name ; :age ?age ." +
				"  VALUES (?name ?age) { (\"Mark\" UNDEF) } " +
				"}";
		runTest(query10, 0);
	}

	@Test
	public void testQ11() throws Exception {
		String query11 =  PREFIX +
				"SELECT * " +
				"WHERE {" +
				"  ?p a :Person ; :name ?name ; :age ?age ." +
				"  VALUES (?name ?age) { " +
				"     (\"Alice\" 18) " +
				"     (\"Bob\" 19) " +
				"  } " +
				"}";
		runTest(query11, 2);
	}


	/**
	 * Not yet supported
	 */
	@Ignore
	@Test
	public void testQ12() throws Exception {
		String query12 =  PREFIX +
				"SELECT * " +
				"WHERE {" +
				"  VALUES (?name ?age) { " +
				"     (\"Alice\" 18) " +
				"     (\"Bob\" 19) " +
				"  } " +
				"  ?p a :Person ; :name ?name ; :age ?age ." +
				"}";
		runTest(query12, 2);
	}

	@Test
	public void testQ12b() throws Exception {
		String query12 =  PREFIX +
				"SELECT * " +
				"WHERE {" +
				"  VALUES (?name ?age) { " +
				"     (\"Alice\" 18) " +
				"     (\"Bob\" 19) " +
				"  } " +
				"  { ?p a :Person ; :name ?name ; :age ?age . }" +
				"}";
		runTest(query12, 2);
	}

	@Test
	public void testQ13() throws Exception {
		String query13 =  PREFIX +
				"SELECT * " +
				"WHERE {" +
				"  ?p a :Person ; :name ?name ." +
				"  VALUES ?name { \"Alice\" \"Bob\" \"Eve\" } " +
				"}";
		runTest(query13, 3);
	}

	@Test
	public void testQ14() throws Exception {
		String query14 =  PREFIX +
				"SELECT * " +
				"WHERE {" +
				"  ?p a :Person ; :name ?name ." +
				"  VALUES ?name { \"Alice\" \"Bob\" \"Eve\" \"Mark\" } " +
				"}";
		runTest(query14, 4);
	}

	@Test
	public void testQ15() throws Exception {
		String query15 =  PREFIX +
				"SELECT * " +
				"WHERE {" +
				"  ?p a :Person ; :name ?name ." +
				"  VALUES ?name { \"Alice\" \"Bob\" \"Eve\" \"Mark\" \"Nobody\" } " +
				"}";
		runTest(query15, 4);
	}

	@Test
	public void testQ16() throws Exception {
		String query16 =  PREFIX +
				"SELECT * " +
				"WHERE {" +
				"  ?p a :Person ; :name ?name ; :age ?age ; :mbox ?mbox ." +
				"  VALUES (?name ?age ?mbox) { " +
				"     (\"Alice\" 18 \"alice@example.org\" ) " +
				"     (\"Bob\" 19 \"bob@example.org\" ) " +
				"     (\"Eve\" 20 \"eve@example.org\" ) " +
				"  } " +
				"}";
		runTest(query16, 3);
	}

}
