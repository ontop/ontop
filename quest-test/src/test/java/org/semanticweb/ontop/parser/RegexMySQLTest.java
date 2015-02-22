package org.semanticweb.ontop.parser;

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

import static org.junit.Assert.*;

import org.semanticweb.ontop.owlrefplatform.core.QuestConstants;
import org.semanticweb.ontop.owlrefplatform.core.QuestPreferences;
import org.semanticweb.ontop.owlrefplatform.owlapi3.QuestOWL;
import org.semanticweb.ontop.owlrefplatform.owlapi3.QuestOWLConnection;
import org.semanticweb.ontop.owlrefplatform.owlapi3.QuestOWLFactory;
import org.semanticweb.ontop.owlrefplatform.owlapi3.QuestOWLResultSet;
import org.semanticweb.ontop.owlrefplatform.owlapi3.QuestOWLStatement;

import java.io.File;
import java.util.Properties;

import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.semanticweb.owlapi.apibinding.OWLManager;
import org.semanticweb.owlapi.model.OWLIndividual;
import org.semanticweb.owlapi.model.OWLOntology;
import org.semanticweb.owlapi.model.OWLOntologyManager;
import org.semanticweb.owlapi.reasoner.SimpleConfiguration;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
/** 
 * Test to check if the sql parser supports regex correctly when written with mysql syntax. 
 * Translated in a datalog function and provides the correct results
 */
public class RegexMySQLTest {

	// TODO We need to extend this test to import the contents of the mappings
	// into OWL and repeat everything taking form OWL

	private QuestOWLConnection conn;

	Logger log = LoggerFactory.getLogger(this.getClass());
	private OWLOntology ontology;

	final String owlfile = "src/test/resources/regex/stockBolzanoAddress.owl";
	final String obdafile = "src/test/resources/regex/stockexchangeRegexMySQL.obda";
	private QuestOWL reasoner;

	@Before
	public void setUp() throws Exception {
		
		
		// Loading the OWL file
		OWLOntologyManager manager = OWLManager.createOWLOntologyManager();
		ontology = manager.loadOntologyFromOntologyDocument((new File(owlfile)));
	
		Properties p = new Properties();
		p.setProperty(QuestPreferences.ABOX_MODE, QuestConstants.VIRTUAL);
		p.setProperty(QuestPreferences.OBTAIN_FULL_METADATA, QuestConstants.FALSE);
		// Creating a new instance of the reasoner
		QuestOWLFactory factory = new QuestOWLFactory(new File(obdafile), new QuestPreferences(p));

		reasoner = factory.createReasoner(ontology, new SimpleConfiguration());

		// Now we are ready for querying
		conn = reasoner.getConnection();

		
	}

 @After
	public void tearDown() throws Exception{
		conn.close();
		reasoner.dispose();
	}
	

	
	private int runTests(String query) throws Exception {
		QuestOWLStatement st = conn.createStatement();
		StringBuilder bf = new StringBuilder(query);
		
		int results=0;
		try {
			

			QuestOWLResultSet rs = st.executeTuple(query);

//			assertTrue(rs.nextRow());
			while (rs.nextRow()){
				OWLIndividual ind1 =	rs.getOWLIndividual("x")	 ;
				log.debug(ind1.toString());
				results++;
			}
		


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
		return results;
	}

	/**
	 * Test use of regex in MySQL
	 * select id, street, number, city, state, country from address where city regexp 'b.+z'
	 * @throws Exception
	 */
	@Test
	public void testOracleRegexLike() throws Exception {
		String query = "PREFIX : <http://www.owl-ontologies.com/Ontology1207768242.owl#> SELECT ?x WHERE {?x a :BolzanoAddress}";
		int numberResults = runTests(query);
		assertEquals(2, numberResults);
	}
	
	/**
	 * Test use of regex in MySQL
	 * select "id", "name", "lastname", "dateofbirth", "ssn" from "broker" where "name" regexp binary 'J.+a'
	 * @throws Exception
	 */
	@Test
	public void testOracleRegexLikeUppercase() throws Exception {
		String query = "PREFIX : <http://www.owl-ontologies.com/Ontology1207768242.owl#> SELECT ?x WHERE {?x a :StockBroker}";
		int numberResults = runTests(query);
		assertEquals(1, numberResults);
	}
	

	
	
	

		
}
