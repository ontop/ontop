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
import java.util.ArrayList;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Properties;

import junit.framework.TestCase;

import org.semanticweb.owlapi.apibinding.OWLManager;
import org.semanticweb.owlapi.model.OWLIndividual;
import org.semanticweb.owlapi.model.OWLLiteral;
import org.semanticweb.owlapi.model.OWLObject;
import org.semanticweb.owlapi.model.OWLOntology;
import org.semanticweb.owlapi.model.OWLOntologyManager;
import org.semanticweb.owlapi.reasoner.SimpleConfiguration;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Test the simple ontology test-hierarchy-extended for sparql owl entailments.
 * rdfs:subclass, rdfs:subProperty, owl:inverseof owl:equivalentclass
 * owl:equivalentProperty owl:disjointWith owl:propertyDisjointWith rdfs:domain
 * rdfs:range QuestPreferences has SPARQL_OWL_ENTAILMENT set to true.
 * 
 */
public class LUBM1MySQLTest extends TestCase {

	private OBDADataFactory fac;

	Logger log = LoggerFactory.getLogger(this.getClass());
	private OBDAModel obdaModel;
	private OWLOntology ontology;

	final String owlfile =
			"src/test/resources/subclass/univ-bench.owl";
	final String obdafile =
			"src/test/resources/subclass/univ1-bench.obda";

	@Override
	public void setUp() throws Exception {

		// Loading the OWL file
		OWLOntologyManager manager = OWLManager.createOWLOntologyManager();
		ontology = manager.loadOntologyFromOntologyDocument((new File(owlfile)));

		// Loading the OBDA data
		fac = OBDADataFactoryImpl.getInstance();
		obdaModel = fac.getOBDAModel();

		ModelIOManager ioManager = new ModelIOManager(obdaModel);
		ioManager.load(obdafile);

	}


	private List<String> runTests(Properties p, String query, String function) throws Exception {

		// Creating a new instance of the reasoner
		QuestOWLFactory factory = new QuestOWLFactory();
		factory.setOBDAController(obdaModel);

		factory.setPreferenceHolder(p);

		QuestOWL reasoner = (QuestOWL) factory.createReasoner(ontology, new SimpleConfiguration());

		// Now we are ready for querying
		QuestOWLConnection conn = reasoner.getConnection();
		QuestOWLStatement st = conn.createStatement();
		List<String> individuals = new LinkedList<String>();

		StringBuilder bf = new StringBuilder(query);
		try {

			QuestOWLResultSet rs = st.executeTuple(query);
			while (rs.nextRow())
			{
				OWLIndividual ind1 = rs.getOWLIndividual("x");
				OWLIndividual ind2 = rs.getOWLIndividual("y");

				System.out.println(ind1 + " " + function + " " + ind2);
				individuals.add(ind1 + " " + function + " " + ind2);

			}
			return individuals;

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

	private List<String> runSingleNamedIndividualTests(Properties p, String query, String function) throws Exception {

		// Creating a new instance of the reasoner
		QuestOWLFactory factory = new QuestOWLFactory();
		factory.setOBDAController(obdaModel);

		factory.setPreferenceHolder(p);

		QuestOWL reasoner = (QuestOWL) factory.createReasoner(ontology, new SimpleConfiguration());

		// Now we are ready for querying
		QuestOWLConnection conn = reasoner.getConnection();
		QuestOWLStatement st = conn.createStatement();
		List<String> individuals = new LinkedList<String>();

		StringBuilder bf = new StringBuilder(query);
		try {

			QuestOWLResultSet rs = st.executeTuple(query);
			while (rs.nextRow())
			{
				
				OWLIndividual ind2 = rs.getOWLIndividual("x");

				System.out.println(ind2);
				if (ind2.isNamed())
					individuals.add(ind2.toString());

			}
			return individuals;

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

	public void testLUBM() throws Exception {

		QuestPreferences p = new QuestPreferences();
		p.setCurrentValueOf(QuestPreferences.ABOX_MODE, QuestConstants.VIRTUAL);
		p.setCurrentValueOf(QuestPreferences.OPTIMIZE_EQUIVALENCES, "true");
		p.setCurrentValueOf(QuestPreferences.OPTIMIZE_TBOX_SIGMA, "true");
		p.setCurrentValueOf(QuestPreferences.SPARQL_OWL_ENTAILMENT, "true");

		// Creating a new instance of the reasoner
		QuestOWLFactory factory = new QuestOWLFactory();
		factory.setOBDAController(obdaModel);

		factory.setPreferenceHolder(p);
		int run=1;
		
			
		
		long start1 =System.currentTimeMillis();
		QuestOWL reasoner = (QuestOWL) factory.createReasoner(ontology, new SimpleConfiguration());
		long end1= System.currentTimeMillis()-start1;
		log.info("---time Reasoner " + end1);
		
		Map<Integer, String> queries = new HashMap<Integer, String>();
		Map<String, Integer> queriesResult = new HashMap<String, Integer>();
		Map<String, Long> queriesTime = new HashMap<String, Long>();
		String queryString;
		
		//query1 
		queryString = "PREFIX rdf: <http://www.w3.org/1999/02/22-rdf-syntax-ns#> "
				+ "PREFIX : <http://swat.cse.lehigh.edu/onto/univ-bench.owl#>"
				+ "SELECT ?X WHERE {"
				+ "?X rdf:type :GraduateStudent . ?X :takesCourse <http://www.Department0.University0.edu/GraduateCourse0>. }" ;

		queries.put(1, queryString);
		queriesResult.put(queryString, 4);
		queriesTime.put(queryString, Long.valueOf(0));

		//query2  
		queryString = "PREFIX ub:<http://swat.cse.lehigh.edu/onto/univ-bench.owl#> SELECT ?x ?y ?z WHERE { " +
				"?x rdf:type ub:GraduateStudent. " +
				"?y rdf:type ub:University. " +
				"?z rdf:type ub:Department. " +
				"?x ub:memberOf ?z. " +
				"?z ub:subOrganizationOf ?y. " +
				"?x ub:undergraduateDegreeFrom ?y.}";
		queries.put(2, queryString);
		queriesResult.put(queryString, 0);
		queriesTime.put(queryString, Long.valueOf(0));

		//query3 
		queryString = "PREFIX ub:<http://swat.cse.lehigh.edu/onto/univ-bench.owl#> SELECT ?x WHERE { " +
				"?x rdf:type ub:Publication. " +
				"?x ub:publicationAuthor <http://www.Department0.University0.edu/AssistantProfessor0>.} " ;
		queries.put(3, queryString);
		queriesResult.put(queryString, 6);
		queriesTime.put(queryString, Long.valueOf(0));

		//query4
		queryString = "PREFIX ub:<http://swat.cse.lehigh.edu/onto/univ-bench.owl#> SELECT ?x ?y1 ?y2 ?y3 WHERE { " +
				"?x rdf:type ub:Professor. " +
				"?x ub:worksFor <http://www.Department0.University0.edu>. " +
				"?x ub:name ?y1. " +
				"?x ub:emailAddress ?y2. " +
				"?x ub:telephone ?y3. }" ;
		queries.put(4, queryString);
		queriesResult.put(queryString, 34);
		queriesTime.put(queryString, Long.valueOf(0));

		//query5 
		queryString = "PREFIX ub:<http://swat.cse.lehigh.edu/onto/univ-bench.owl#> SELECT ?x WHERE { " +
				"?x rdf:type ub:Person. " +
				"?x ub:memberOf <http://www.Department0.University0.edu>. }" ;
		queries.put(5, queryString);
		queriesResult.put(queryString, 719);
		queriesTime.put(queryString, Long.valueOf(0));

		//query6 
		queryString = "PREFIX ub:<http://swat.cse.lehigh.edu/onto/univ-bench.owl#> SELECT ?x WHERE { " +
				"?x rdf:type ub:Student. }" ;
		queries.put(6, queryString);
		queriesResult.put(queryString, 7790);
		queriesTime.put(queryString, Long.valueOf(0));
		
		//query7 
		queryString = "PREFIX ub:<http://swat.cse.lehigh.edu/onto/univ-bench.owl#> SELECT ?x ?y WHERE { " +
				"?x rdf:type ub:Student. " +
				"?y rdf:type ub:Course. " +
				"?x ub:takesCourse ?y. " +
				"<http://www.Department0.University0.edu/AssociateProfessor0> ub:teacherOf ?y. }" ;
		queries.put(7, queryString);
		queriesResult.put(queryString, 67);
		queriesTime.put(queryString, Long.valueOf(0));

		//query8 
		queryString = "PREFIX ub:<http://swat.cse.lehigh.edu/onto/univ-bench.owl#> SELECT ?x ?y ?z WHERE { " +
				"?x rdf:type ub:Student. " +
				"?y rdf:type ub:Department. " +
				"?x ub:memberOf ?y. " +
				"?y ub:subOrganizationOf <http://www.University0.edu>." +
				"?x ub:emailAddress ?z. }" ;
		queries.put(8, queryString);
		queriesResult.put(queryString, 7790);
		queriesTime.put(queryString, Long.valueOf(0));

		//query9 
		queryString = "PREFIX ub:<http://swat.cse.lehigh.edu/onto/univ-bench.owl#> SELECT ?x ?y ?z WHERE { " +
				"?x rdf:type ub:Student. " +
				"?y rdf:type ub:Faculty. " +
				"?z rdf:type ub:Course. " +
				"?x ub:advisor ?y. " +
				"?y ub:teacherOf ?z. " +
				"?x ub:takesCourse ?z. }" ;
		queries.put(9, queryString);
		queriesResult.put(queryString, 208);
		queriesTime.put(queryString, Long.valueOf(0));

		//query10 
		queryString = "PREFIX ub:<http://swat.cse.lehigh.edu/onto/univ-bench.owl#> SELECT ?x WHERE { " +
				"?x rdf:type ub:Student. " +
				"?x ub:takesCourse <http://www.Department0.University0.edu/GraduateCourse0>. }" ;
		queries.put(10, queryString);
		queriesResult.put(queryString, 4);
		queriesTime.put(queryString, Long.valueOf(0));

		//query11 
		queryString = "PREFIX ub:<http://swat.cse.lehigh.edu/onto/univ-bench.owl#> SELECT ?x WHERE { " +
				"?x rdf:type ub:ResearchGroup. " +
				"?x ub:subOrganizationOf <http://www.University0.edu>. }" ;
		queries.put(11, queryString);
		queriesResult.put(queryString, 224);
		queriesTime.put(queryString, Long.valueOf(0));

		//query12 
		queryString = "PREFIX ub:<http://swat.cse.lehigh.edu/onto/univ-bench.owl#> SELECT ?x ?y WHERE { " +
				"?x rdf:type ub:Chair. " +
				"?y rdf:type ub:Department. " +
				"?x ub:worksFor ?y. " +
				"?y ub:subOrganizationOf <http://www.University0.edu>. }" ;
		queries.put(12, queryString);
		queriesResult.put(queryString, 15);
		queriesTime.put(queryString, Long.valueOf(0));
		
		
		//query13 
		queryString = "PREFIX ub:<http://swat.cse.lehigh.edu/onto/univ-bench.owl#> SELECT ?x WHERE { " +
				"?x rdf:type ub:Person. " +
				"<http://www.University0.edu> ub:hasAlumnus ?x.}" ;
		queries.put(13, queryString);
		queriesResult.put(queryString, 1);
		queriesTime.put(queryString, Long.valueOf(0));

		//query14 
		queryString = "PREFIX ub:<http://swat.cse.lehigh.edu/onto/univ-bench.owl#> SELECT ?x WHERE { " +
				"?x rdf:type ub:UndergraduateStudent. }" ;
		queries.put(14, queryString);
		queriesResult.put(queryString, 5916);
		queriesTime.put(queryString, Long.valueOf(0));
	
		// Now we are ready for querying
		QuestOWLConnection conn = reasoner.getConnection();
		
		try {
			while(run<4){	
			for (Integer number : queries.keySet()) {
				String query = queries.get(number);
				log.info("---Query " + number + query);
				
				QuestOWLStatement st = conn.createStatement();
				long start =System.currentTimeMillis();
				QuestOWLResultSet rs = st.executeTuple(query);
				long end= System.currentTimeMillis()-start;
				log.info("---time " + end);
				queriesTime.put(query, Long.valueOf(end) + queriesTime.get(query));
				int numberResults = 0;
				
				while (rs.nextRow())
				{
					for(String element: rs.getSignature()){
					OWLObject ind1 = rs.getOWLObject(element);
					
					log.debug(element + ind1 + " ");
					}
					
					numberResults++;
				}
				assertEquals(queriesResult.get(query).intValue(), numberResults);
				st.close();
			}

			run++;
			}
			int i=1;
			for (Integer number : queries.keySet()){
				String query = queries.get(number);
			log.info("time query" + i + " " +  Math.round(queriesTime.get(query) / 3.0D) );
			i++;
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

	public void testSubDescription() throws Exception {

		QuestPreferences p = new QuestPreferences();
		p.setCurrentValueOf(QuestPreferences.ABOX_MODE, QuestConstants.VIRTUAL);
		p.setCurrentValueOf(QuestPreferences.OPTIMIZE_EQUIVALENCES, "true");
		p.setCurrentValueOf(QuestPreferences.OPTIMIZE_TBOX_SIGMA, "true");
		p.setCurrentValueOf(QuestPreferences.SPARQL_OWL_ENTAILMENT, "true");

		log.info("Find subProperty");
		List<String> individualsProperty = runTests(p, "PREFIX :<http://swat.cse.lehigh.edu/onto/univ-bench.owl#> SELECT * WHERE { ?x rdfs:subPropertyOf ?y }", "rdfs:subPropertyOf");
		assertEquals(106, individualsProperty.size());

		log.info("Find subClass");
		List<String> individualsClass = runTests(p, "PREFIX : <http://swat.cse.lehigh.edu/onto/univ-bench.owl#> SELECT * WHERE { ?x rdfs:subClassOf ?y }", "rdfs:subClassOf");
		assertEquals(283, individualsClass.size());

	}

	public void testEquivalences() throws Exception {

		QuestPreferences p = new QuestPreferences();
		p.setCurrentValueOf(QuestPreferences.ABOX_MODE, QuestConstants.VIRTUAL);
		p.setCurrentValueOf(QuestPreferences.OPTIMIZE_EQUIVALENCES, "true");
		p.setCurrentValueOf(QuestPreferences.OPTIMIZE_TBOX_SIGMA, "true");
		p.setCurrentValueOf(QuestPreferences.SPARQL_OWL_ENTAILMENT, "true");

		log.info("Find equivalent classes");
		List<String> individualsEquivClass = runTests(p, "PREFIX : <http://swat.cse.lehigh.edu/onto/univ-bench.owl#> SELECT * WHERE { ?x owl:equivalentClass ?y }", "owl:equivalentClass");
		assertEquals(119, individualsEquivClass.size());

		//
		log.info("Find equivalent properties");
		List<String> individualsEquivProperties = runTests(p, "PREFIX : <http://swat.cse.lehigh.edu/onto/univ-bench.owl#> SELECT * WHERE { ?x owl:equivalentProperty ?y }", "owl:equivalentProperty");
		assertEquals(76, individualsEquivProperties.size());

		//
	}

	public void testDomains() throws Exception {

		QuestPreferences p = new QuestPreferences();
		p.setCurrentValueOf(QuestPreferences.ABOX_MODE, QuestConstants.VIRTUAL);
		p.setCurrentValueOf(QuestPreferences.OPTIMIZE_EQUIVALENCES, "true");
		p.setCurrentValueOf(QuestPreferences.OPTIMIZE_TBOX_SIGMA, "true");
		p.setCurrentValueOf(QuestPreferences.SPARQL_OWL_ENTAILMENT, "true");

		log.info("Find domain");
		List<String> individualsDomainClass = runTests(p, "PREFIX : <http://swat.cse.lehigh.edu/onto/univ-bench.owl#> SELECT * WHERE { ?x rdfs:domain ?y }", "rdfs:domain");
		assertEquals(41, individualsDomainClass.size());
	}

	public void testRanges() throws Exception {

		QuestPreferences p = new QuestPreferences();
		p.setCurrentValueOf(QuestPreferences.ABOX_MODE, QuestConstants.VIRTUAL);
		p.setCurrentValueOf(QuestPreferences.OPTIMIZE_EQUIVALENCES, "true");
		p.setCurrentValueOf(QuestPreferences.OPTIMIZE_TBOX_SIGMA, "true");
		p.setCurrentValueOf(QuestPreferences.SPARQL_OWL_ENTAILMENT, "true");

		log.info("Find range");
		List<String> individualsRangeClass = runTests(p, "PREFIX : <http://swat.cse.lehigh.edu/onto/univ-bench.owl#> SELECT * WHERE { ?x rdfs:range ?y }", "rdfs:range");
		assertEquals(41, individualsRangeClass.size());
	}

	public void testDisjoint() throws Exception {

		QuestPreferences p = new QuestPreferences();
		p.setCurrentValueOf(QuestPreferences.ABOX_MODE, QuestConstants.VIRTUAL);
		p.setCurrentValueOf(QuestPreferences.OPTIMIZE_EQUIVALENCES, "true");
		p.setCurrentValueOf(QuestPreferences.OPTIMIZE_TBOX_SIGMA, "true");
		p.setCurrentValueOf(QuestPreferences.SPARQL_OWL_ENTAILMENT, "true");

		log.info("Find disjoint classes");
		List<String> individualsDisjClass = runTests(p, "PREFIX : <http://swat.cse.lehigh.edu/onto/univ-bench.owl#> SELECT * WHERE { ?x owl:disjointWith ?y }", "owl:disjointWith");
		assertEquals(0, individualsDisjClass.size());

		log.info("Find disjoint properties");
		List<String> individualsDisjProp = runTests(p, "PREFIX : <http://swat.cse.lehigh.edu/onto/univ-bench.owl#> SELECT * WHERE { ?x owl:propertyDisjointWith ?y }", "owl:propertyDisjointWith");
		assertEquals(0, individualsDisjProp.size());

	}

	public void testInverseOf() throws Exception {

		QuestPreferences p = new QuestPreferences();
		p.setCurrentValueOf(QuestPreferences.ABOX_MODE, QuestConstants.VIRTUAL);
		p.setCurrentValueOf(QuestPreferences.OPTIMIZE_EQUIVALENCES, "true");
		p.setCurrentValueOf(QuestPreferences.OPTIMIZE_TBOX_SIGMA, "true");
		p.setCurrentValueOf(QuestPreferences.SPARQL_OWL_ENTAILMENT, "true");

		log.info("Find inverse");
		List<String> individualsInverse = runTests(p, "PREFIX : <http://swat.cse.lehigh.edu/onto/univ-bench.owl#> SELECT * WHERE { ?x owl:inverseOf ?y }", "owl:inverseOf");
		assertEquals(76, individualsInverse.size());

	}

}
