package it.unibz.krdb.obda.sparql.entailments;

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

import it.unibz.krdb.obda.io.ModelIOManager;
import it.unibz.krdb.obda.model.OBDADataFactory;
import it.unibz.krdb.obda.model.OBDAModel;
import it.unibz.krdb.obda.model.impl.OBDADataFactoryImpl;
import it.unibz.krdb.obda.owlrefplatform.core.QuestConstants;
import it.unibz.krdb.obda.owlrefplatform.core.QuestPreferences;
import it.unibz.krdb.obda.owlrefplatform.owlapi3.*;
import org.junit.Before;
import org.junit.Test;
import org.semanticweb.owlapi.apibinding.OWLManager;
import org.semanticweb.owlapi.model.OWLOntology;
import org.semanticweb.owlapi.model.OWLOntologyManager;
import org.semanticweb.owlapi.reasoner.SimpleConfiguration;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.File;
import java.util.HashMap;
import java.util.Map;

/**
 * Test LUBM ontology with the standard LUMB Queries, with special LUBM Queries
 * and with SPARQLDL queries. 
 * Modify the value obdafile  to test with LUBM9, LUBM20 or LUBM500.
 */
public class LUBMMySQLTest  {

	private OBDADataFactory fac;

	Logger log = LoggerFactory.getLogger(this.getClass());
	private OBDAModel obdaModel;
	private OWLOntology ontology;

	String owlfile = "src/test/resources/subclass/univ-benchQL.owl";
	String obdafile = "src/test/resources/subclass/univ-benchQL.obda";

	@Before
	public void setUp() throws Exception {

		// // Loading the OWL file
		// OWLOntologyManager manager = OWLManager.createOWLOntologyManager();
		// ontology = manager.loadOntologyFromOntologyDocument((new
		// File(owlfile)));
		//
		// // Loading the OBDA data
		// fac = OBDADataFactoryImpl.getInstance();
		// obdaModel = fac.getOBDAModel();
		//
		// ModelIOManager ioManager = new ModelIOManager(obdaModel);
		// ioManager.load(obdafile);

	}
	@Test
	public void testSpecial() throws Exception {

		long start1 = System.currentTimeMillis();
		// Loading the OWL file
		OWLOntologyManager manager = OWLManager.createOWLOntologyManager();
		ontology = manager
				.loadOntologyFromOntologyDocument((new File(owlfile)));

		// Loading the OBDA data
		fac = OBDADataFactoryImpl.getInstance();
		obdaModel = fac.getOBDAModel();

		ModelIOManager ioManager = new ModelIOManager(obdaModel);
		ioManager.load(obdafile);

		QuestPreferences p = new QuestPreferences();
		p.setCurrentValueOf(QuestPreferences.ABOX_MODE, QuestConstants.VIRTUAL);
		p.setCurrentValueOf(QuestPreferences.OPTIMIZE_EQUIVALENCES, "true");
		p.setCurrentValueOf(QuestPreferences.OPTIMIZE_TBOX_SIGMA, "true");
		p.setCurrentValueOf(QuestPreferences.SPARQL_OWL_ENTAILMENT, "true");

		// Creating a new instance of the reasoner
		QuestOWLFactory factory = new QuestOWLFactory();
		factory.setOBDAController(obdaModel);

		factory.setPreferenceHolder(p);
		int run = 1;

		QuestOWL reasoner = (QuestOWL) factory.createReasoner(ontology,
				new SimpleConfiguration());
		long end1 = System.currentTimeMillis() - start1;
		log.info("---time Reasoner " + end1);

		Map<Integer, String> queries = new HashMap<Integer, String>();
		Map<String, Long> queriesTime = new HashMap<String, Long>();
		String queryString;

		// query4_01
		queryString = "PREFIX ub:<http://swat.cse.lehigh.edu/onto/univ-bench.owl#> SELECT ?x ?y1 ?y2 ?y3 ?z WHERE { "
				+ "?x rdf:type ?z. "
				+ "?x ub:worksFor <http://www.Department0.University0.edu>. "
				+ "?x ub:name ?y1. "
				+ "?x ub:emailAddress ?y2. "
				+ "?x ub:telephone ?y3. }";
		queries.put(15, queryString);
		queriesTime.put(queryString, Long.valueOf(0));

		// query4_02
		queryString = "PREFIX ub:<http://swat.cse.lehigh.edu/onto/univ-bench.owl#> SELECT ?x ?y1 ?y2 ?y3 ?z WHERE { "
				+ "?x rdf:type ub:Professor. "
				+ "ub:Professor rdfs:subClassOf ?z. "
				+ "?x ub:worksFor <http://www.Department0.University0.edu>. "
				+ "?x ub:name ?y1. "
				+ "?x ub:emailAddress ?y2. "
				+ "?x ub:telephone ?y3. }";
		queries.put(16, queryString);
		queriesTime.put(queryString, Long.valueOf(0));

		// query9_01
		queryString = "PREFIX ub:<http://swat.cse.lehigh.edu/onto/univ-bench.owl#> SELECT ?x ?y ?z ?x1  WHERE { "
				+ "?x rdf:type ?x1. "
				+ "?x1 rdfs:subClassOf ub:Student. "
				+ "?y rdf:type ub:Faculty. "
				+ "?z rdf:type ub:Course. "
				+ "?x ub:advisor ?y. "
				+ "?y ub:teacherOf ?z. "
				+ "?x ub:takesCourse ?z. }";
		queries.put(17, queryString);
		queriesTime.put(queryString, Long.valueOf(0));

		// query9_02
		queryString = "PREFIX ub:<http://swat.cse.lehigh.edu/onto/univ-bench.owl#> SELECT ?x ?y ?z ?y1  WHERE { "
				+ "?x rdf:type ub:Student. "
				+ "?y rdf:type ?y1. "
				+ "?y1 owl:equivalentClass ub:Faculty. "
				+ "?z rdf:type ub:Course. "
				+ "?x ub:advisor ?y. "
				+ "?y ub:teacherOf ?z. " + "?x ub:takesCourse ?z. }";
		queries.put(18, queryString);
		queriesTime.put(queryString, Long.valueOf(0));
		

		// Now we are ready for querying
		QuestOWLConnection conn = reasoner.getConnection();

		try {
			while (run < 5) {
				for (Integer number : queries.keySet()) {
					String query = queries.get(number);
					log.info("---Query " + number + query);

					QuestOWLStatement st = conn.createStatement();
					long start = System.currentTimeMillis();
					QuestOWLResultSet rs = st.executeTuple(query);
					long end = System.currentTimeMillis() - start;
					log.info("---time " + end);
					if (run != 1) {
						queriesTime.put(query,
								Long.valueOf(end) + queriesTime.get(query));
						int numberResults = 0;

						while (rs.nextRow()) {
							// for(String element: rs.getSignature()){
							// OWLObject ind1 = rs.getOWLObject(element);
							//
							// log.info(element + ind1 + " ");
							// }

							numberResults++;
						}
						log.info("---results " + numberResults);
						st.close();
					}

				}

				run++;
			}

			for (Integer number : queries.keySet()) {
				String query = queries.get(number);
				log.info("time query" + number + " "
						+ Math.round(queriesTime.get(query) / 3.0D));

			}

		} catch (Exception e) {
			throw e;
		} finally {
			try {

			} catch (Exception e) {

			}
			conn.close();

			reasoner.dispose();
		}
	}

	@Test
	public void test3runLUBM() throws Exception {

		long start1 = System.currentTimeMillis();
		// Loading the OWL file
		OWLOntologyManager manager = OWLManager.createOWLOntologyManager();
		ontology = manager
				.loadOntologyFromOntologyDocument((new File(owlfile)));

		// Loading the OBDA data
		fac = OBDADataFactoryImpl.getInstance();
		obdaModel = fac.getOBDAModel();

		ModelIOManager ioManager = new ModelIOManager(obdaModel);
		ioManager.load(obdafile);

		QuestPreferences p = new QuestPreferences();
		p.setCurrentValueOf(QuestPreferences.ABOX_MODE, QuestConstants.VIRTUAL);
		p.setCurrentValueOf(QuestPreferences.OPTIMIZE_EQUIVALENCES, "true");
		p.setCurrentValueOf(QuestPreferences.OPTIMIZE_TBOX_SIGMA, "true");
		p.setCurrentValueOf(QuestPreferences.SPARQL_OWL_ENTAILMENT, "true");

		// Creating a new instance of the reasoner
		QuestOWLFactory factory = new QuestOWLFactory();
		factory.setOBDAController(obdaModel);

		factory.setPreferenceHolder(p);
		int run = 1;

		QuestOWL reasoner = (QuestOWL) factory.createReasoner(ontology,
				new SimpleConfiguration());
		long end1 = System.currentTimeMillis() - start1;
		log.info("---time Reasoner " + end1);

		Map<Integer, String> queries = new HashMap<Integer, String>();
		Map<String, Long> queriesTime = new HashMap<String, Long>();
		String queryString;

		// query1
		queryString = "PREFIX rdf: <http://www.w3.org/1999/02/22-rdf-syntax-ns#> "
				+ "PREFIX : <http://swat.cse.lehigh.edu/onto/univ-bench.owl#>"
				+ "SELECT ?X WHERE {"
				+ "?X rdf:type :GraduateStudent . ?X :takesCourse <http://www.Department0.University0.edu/GraduateCourse0>. }";

		queries.put(1, queryString);
		queriesTime.put(queryString, Long.valueOf(0));

		// query2
		queryString = "PREFIX ub:<http://swat.cse.lehigh.edu/onto/univ-bench.owl#> SELECT ?x ?y ?z WHERE { "
				+ "?x rdf:type ub:GraduateStudent. "
				+ "?y rdf:type ub:University. "
				+ "?z rdf:type ub:Department. "
				+ "?x ub:memberOf ?z. "
				+ "?z ub:subOrganizationOf ?y. "
				+ "?x ub:undergraduateDegreeFrom ?y.}";
		queries.put(2, queryString);
		queriesTime.put(queryString, Long.valueOf(0));

		// query3
		queryString = "PREFIX ub:<http://swat.cse.lehigh.edu/onto/univ-bench.owl#> SELECT ?x WHERE { "
				+ "?x rdf:type ub:Publication. "
				+ "?x ub:publicationAuthor <http://www.Department0.University0.edu/AssistantProfessor0>.} ";
		queries.put(3, queryString);
		queriesTime.put(queryString, Long.valueOf(0));

		// query4
		queryString = "PREFIX ub:<http://swat.cse.lehigh.edu/onto/univ-bench.owl#> SELECT ?x ?y1 ?y2 ?y3 WHERE { "
				+ "?x rdf:type ub:Professor. "
				+ "?x ub:worksFor <http://www.Department0.University0.edu>. "
				+ "?x ub:name ?y1. "
				+ "?x ub:emailAddress ?y2. "
				+ "?x ub:telephone ?y3. }";
		queries.put(4, queryString);
		queriesTime.put(queryString, Long.valueOf(0));

		// query5
		queryString = "PREFIX ub:<http://swat.cse.lehigh.edu/onto/univ-bench.owl#> SELECT ?x WHERE { "
				+ "?x rdf:type ub:Person. "
				+ "?x ub:memberOf <http://www.Department0.University0.edu>. }";
		queries.put(5, queryString);
		queriesTime.put(queryString, Long.valueOf(0));

		// query6
		queryString = "PREFIX ub:<http://swat.cse.lehigh.edu/onto/univ-bench.owl#> SELECT ?x WHERE { "
				+ "?x rdf:type ub:Student. }";
		queries.put(6, queryString);
		queriesTime.put(queryString, Long.valueOf(0));

		// query7
		queryString = "PREFIX ub:<http://swat.cse.lehigh.edu/onto/univ-bench.owl#> SELECT ?x ?y WHERE { "
				+ "?x rdf:type ub:Student. "
				+ "?y rdf:type ub:Course. "
				+ "?x ub:takesCourse ?y. "
				+ "<http://www.Department0.University0.edu/AssociateProfessor0> ub:teacherOf ?y. }";
		queries.put(7, queryString);
		queriesTime.put(queryString, Long.valueOf(0));

		// query8
		queryString = "PREFIX ub:<http://swat.cse.lehigh.edu/onto/univ-bench.owl#> SELECT ?x ?y ?z WHERE { "
				+ "?x rdf:type ub:Student. "
				+ "?y rdf:type ub:Department. "
				+ "?x ub:memberOf ?y. "
				+ "?y ub:subOrganizationOf <http://www.University0.edu>."
				+ "?x ub:emailAddress ?z. }";
		queries.put(8, queryString);
		queriesTime.put(queryString, Long.valueOf(0));

		// query9
		queryString = "PREFIX ub:<http://swat.cse.lehigh.edu/onto/univ-bench.owl#> SELECT ?x ?y ?z WHERE { "
				+ "?x rdf:type ub:Student. "
				+ "?y rdf:type ub:Faculty. "
				+ "?z rdf:type ub:Course. "
				+ "?x ub:advisor ?y. "
				+ "?y ub:teacherOf ?z. " + "?x ub:takesCourse ?z. }";
		queries.put(9, queryString);
		queriesTime.put(queryString, Long.valueOf(0));

		// query10
		queryString = "PREFIX ub:<http://swat.cse.lehigh.edu/onto/univ-bench.owl#> SELECT ?x WHERE { "
				+ "?x rdf:type ub:Student. "
				+ "?x ub:takesCourse <http://www.Department0.University0.edu/GraduateCourse0>. }";
		queries.put(10, queryString);
		queriesTime.put(queryString, Long.valueOf(0));

		// query11
		queryString = "PREFIX ub:<http://swat.cse.lehigh.edu/onto/univ-bench.owl#> SELECT ?x WHERE { "
				+ "?x rdf:type ub:ResearchGroup. "
				+ "?x ub:subOrganizationOf <http://www.University0.edu>. }";
		queries.put(11, queryString);
		queriesTime.put(queryString, Long.valueOf(0));

		// query12
		queryString = "PREFIX ub:<http://swat.cse.lehigh.edu/onto/univ-bench.owl#> SELECT ?x ?y WHERE { "
				+ "?x rdf:type ub:Chair. "
				+ "?y rdf:type ub:Department. "
				+ "?x ub:worksFor ?y. "
				+ "?y ub:subOrganizationOf <http://www.University0.edu>. }";
		queries.put(12, queryString);
		queriesTime.put(queryString, Long.valueOf(0));

		// query13
		queryString = "PREFIX ub:<http://swat.cse.lehigh.edu/onto/univ-bench.owl#> SELECT ?x WHERE { "
				+ "?x rdf:type ub:Person. "
				+ "<http://www.University0.edu> ub:hasAlumnus ?x.}";
		queries.put(13, queryString);
		queriesTime.put(queryString, Long.valueOf(0));

		// query14
		queryString = "PREFIX ub:<http://swat.cse.lehigh.edu/onto/univ-bench.owl#> SELECT ?x WHERE { "
				+ "?x rdf:type ub:UndergraduateStudent. }";
		queries.put(14, queryString);
		queriesTime.put(queryString, Long.valueOf(0));

		// Now we are ready for querying
		QuestOWLConnection conn = reasoner.getConnection();

		try {
			while (run < 5) {
				for (Integer number : queries.keySet()) {
					String query = queries.get(number);
					log.info("---Query " + number + query);

					QuestOWLStatement st = conn.createStatement();
					long start = System.currentTimeMillis();
					QuestOWLResultSet rs = st.executeTuple(query);
					long end = System.currentTimeMillis() - start;
					log.info("---time " + end);
					if (run != 1) {
						queriesTime.put(query,
								Long.valueOf(end) + queriesTime.get(query));
						int numberResults = 0;

						while (rs.nextRow()) {
							// for(String element: rs.getSignature()){
							// OWLObject ind1 = rs.getOWLObject(element);
							//
							// log.debug(element + ind1 + " ");
							// }
							//
							numberResults++;
						}
						log.info("---results " + numberResults);
						st.close();
					}
				}

				run++;
			}

			for (Integer number : queries.keySet()) {
				String query = queries.get(number);
				log.info("time query" + number + " "
						+ Math.round(queriesTime.get(query) / 3.0D));

			}

		} catch (Exception e) {
			throw e;
		} finally {
			try {

			} catch (Exception e) {

			}
			conn.close();

			reasoner.dispose();
		}

	}
	@Test
	public void testSPARQLDL() throws Exception {

		long start1 = System.currentTimeMillis();
		// Loading the OWL file
		OWLOntologyManager manager = OWLManager.createOWLOntologyManager();
		ontology = manager
				.loadOntologyFromOntologyDocument((new File(owlfile)));

		// Loading the OBDA data
		fac = OBDADataFactoryImpl.getInstance();
		obdaModel = fac.getOBDAModel();

		ModelIOManager ioManager = new ModelIOManager(obdaModel);
		ioManager.load(obdafile);

		QuestPreferences p = new QuestPreferences();
		p.setCurrentValueOf(QuestPreferences.ABOX_MODE, QuestConstants.VIRTUAL);
		p.setCurrentValueOf(QuestPreferences.OPTIMIZE_EQUIVALENCES, "true");
		p.setCurrentValueOf(QuestPreferences.OPTIMIZE_TBOX_SIGMA, "true");
		p.setCurrentValueOf(QuestPreferences.SPARQL_OWL_ENTAILMENT, "true");

		// Creating a new instance of the reasoner
		QuestOWLFactory factory = new QuestOWLFactory();
		factory.setOBDAController(obdaModel);

		factory.setPreferenceHolder(p);
		int run = 1;

		QuestOWL reasoner = (QuestOWL) factory.createReasoner(ontology,
				new SimpleConfiguration());
		long end1 = System.currentTimeMillis() - start1;
		log.info("---time Reasoner " + end1);

		Map<Integer, String> queries = new HashMap<Integer, String>();
		Map<String, Long> queriesTime = new HashMap<String, Long>();
		String queryString;

//		queryString = "PREFIX ub:<http://swat.cse.lehigh.edu/onto/univ-bench.owl#> SELECT * WHERE { "
//				+ "?x rdf:type ub:GraduateStudent. "
//				+ "?x ?y ?z. "
//				+ "?w rdf:type ub:Course." + " } ";
//
//		queries.put(1, queryString);
//		queriesTime.put(queryString, Long.valueOf(0));
		

		queryString = "PREFIX ub:<http://swat.cse.lehigh.edu/onto/univ-bench.owl#> SELECT * WHERE { "
				+ "?x rdf:type ub:Student."
				+ "?x rdf:type ?C."
				+ "?C rdfs:subClassOf ub:Employee."
				+ "?x ub:undergraduateDegreeFrom ?y."
				+ "} ";

		queries.put(2, queryString);
		queriesTime.put(queryString, Long.valueOf(0));
//
		queryString = "PREFIX ub:<http://swat.cse.lehigh.edu/onto/univ-bench.owl#> SELECT * WHERE { "
				+ "?x rdf:type ub:Person."
				+ "?x ?y <http://www.Department0.University0.edu>."
				+ "?y rdfs:subPropertyOf ub:memberOf."
				+ "} "; 

		queries.put(3, queryString);
		queriesTime.put(queryString, Long.valueOf(0));
//
		queryString = "PREFIX ub:<http://swat.cse.lehigh.edu/onto/univ-bench.owl#> SELECT * WHERE { "
				+ "<http://www.Department0.University0.edu/GraduateStudent5> ?y ?w."
				+ "?w rdf:type ?z." + 
				"?z rdfs:subClassOf ub:Course." + "} ";
		queries.put(4, queryString);
		queriesTime.put(queryString, Long.valueOf(0));
//
		queryString = "PREFIX ub:<http://swat.cse.lehigh.edu/onto/univ-bench.owl#> SELECT * WHERE { "
				+ "?x rdf:type ub:GraduateStudent."
				+ "?x ?y ?w."
				+ "?w rdf:type ?z." + "?z rdfs:subClassOf ub:Course." + "} ";
		queries.put(5, queryString);
		queriesTime.put(queryString, Long.valueOf(0));
//
//		queryString = "PREFIX ub:<http://swat.cse.lehigh.edu/onto/univ-bench.owl#> SELECT * WHERE { "
//				+ "?x rdf:type ub:GraduateStudent."
//				+ "?x ?y ?w."
//				+ "?w rdf:type ?z."
//				+ "?z owl:disjointWith ub:GraduateCourse."
//				+ "} ";
//		queries.put(6, queryString);
//		queriesTime.put(queryString, Long.valueOf(0));

//		queryString = "PREFIX ub:<http://swat.cse.lehigh.edu/onto/univ-bench.owl#> SELECT * WHERE { "
//				+ "?x rdf:type ?c."
//				+ "?x ub:takesCourse ?a." + "} ";
//				+ "?x ub:teachingAssistantOf ?a." + "} ";
//		queries.put(7, queryString);
//		queriesTime.put(queryString, Long.valueOf(0));

		queryString = "PREFIX ub:<http://swat.cse.lehigh.edu/onto/univ-bench.owl#> SELECT * WHERE { "
				+ "?x ub:advisor ?y."
				+ "?x rdf:type ?a."
				+ "?a rdfs:subClassOf ub:Person." + "} ";
		queries.put(8, queryString);
		queriesTime.put(queryString, Long.valueOf(0));
//                            
		queryString = "PREFIX ub:<http://swat.cse.lehigh.edu/onto/univ-bench.owl#> SELECT * WHERE { "
				+ "?a rdfs:subClassOf ub:Person."
				+ "?x rdf:type ?a."
				+ "?x ub:teachingAssistantOf ?y."
				+ "?y rdf:type ub:Course."
				+ "} ";
		queries.put(9, queryString);
		queriesTime.put(queryString, Long.valueOf(0));
//
		queryString = "PREFIX ub:<http://swat.cse.lehigh.edu/onto/univ-bench.owl#> SELECT * WHERE { "
				+ "?x rdf:type ub:GraduateStudent."
				+ "?x ub:memberOf ?w."
				+ "?a ?p ?w."
				+ "?a rdf:type ?b."
				+ "?p rdfs:subPropertyOf ub:worksFor."
				+ "?b rdfs:subClassOf ub:Faculty." + "?x ub:advisor ?a." + "} ";
		queries.put(10, queryString);
		queriesTime.put(queryString, Long.valueOf(0));
		// Now we are ready for querying
		QuestOWLConnection conn = reasoner.getConnection();

		try {
			while (run < 5) {
				for (Integer number : queries.keySet()) {
					String query = queries.get(number);
					log.info("---Query " + number + query);

					QuestOWLStatement st = conn.createStatement();
					long start = System.currentTimeMillis();
					QuestOWLResultSet rs = st.executeTuple(query);
					long end = System.currentTimeMillis() - start;
					log.info("---time " + end);
					if (run != 1) {
						queriesTime.put(query,
								Long.valueOf(end) + queriesTime.get(query));
						int numberResults = 0;

						while (rs.nextRow()) {
							// for(String element: rs.getSignature()){
							// OWLObject ind1 = rs.getOWLObject(element);
							//
							// log.debug(element + ind1 + " ");
							// }
							//
							numberResults++;
						}
						log.info("---results " + numberResults);
						st.close();
					}
				}

				run++;
			}

			for (Integer number : queries.keySet()) {
				String query = queries.get(number);
				log.info("time query" + number + " "
						+ Math.round(queriesTime.get(query) / 3.0D));

			}

		} catch (Exception e) {
			throw e;
		} finally {
			try {

			} catch (Exception e) {

			}
			conn.close();

			reasoner.dispose();
		}

	}

	public static void main(String[] args) throws Exception {
		if (args == null || args.length == 0 || args[0].equals("-h")
				|| args[0].equals("--help") || args[0].equals("")) {
			System.err
					.println("Usage: main ontology.owl mapping.obda [special/DL]");
			System.exit(0);
		}

		LUBMMySQLTest benchmark = new LUBMMySQLTest();
		benchmark.owlfile = args[0];
		benchmark.obdafile = args[1];

		if (args.length == 2) {

			// benchmark.setUp();
			benchmark.test3runLUBM();
		}

		else {
			String run = args[2];
			// benchmark.setUp();

			if (run.equals("special")) {
				benchmark.testSpecial();
			} else if (run.equals("DL")) {
				benchmark.testSPARQLDL();
				;
			} else {
				System.err
						.println("Usage: main ontology.owl mapping.obda [special/DL]");
				System.exit(0);
			}

		}

	}

}
