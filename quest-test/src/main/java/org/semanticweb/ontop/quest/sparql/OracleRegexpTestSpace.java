package org.semanticweb.ontop.quest.sparql;

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

import org.semanticweb.ontop.io.ModelIOManager;
import org.semanticweb.ontop.model.OBDADataFactory;
import org.semanticweb.ontop.model.OBDAModel;
import org.semanticweb.ontop.model.impl.OBDADataFactoryImpl;
import org.semanticweb.ontop.owlrefplatform.core.QuestConstants;
import org.semanticweb.ontop.owlrefplatform.core.QuestPreferences;
import org.semanticweb.ontop.owlrefplatform.owlapi3.QuestOWL;
import org.semanticweb.ontop.owlrefplatform.owlapi3.QuestOWLConnection;
import org.semanticweb.ontop.owlrefplatform.owlapi3.QuestOWLFactory;
import org.semanticweb.ontop.owlrefplatform.owlapi3.QuestOWLResultSet;
import org.semanticweb.ontop.owlrefplatform.owlapi3.QuestOWLStatement;

import java.io.File;

import junit.framework.TestCase;

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

/***
 * Tests that the system can handle the SPARQL "LIKE" keyword in the oracle setting
 * (i.e. that it is translated to REGEXP_LIKE and not LIKE in oracle sql)
 */
public class OracleRegexpTestSpace extends TestCase {

	// TODO We need to extend this test to import the contents of the mappings
	// into OWL and repeat everything taking form OWL

	private OBDADataFactory fac;
	private QuestOWLConnection conn;

	Logger log = LoggerFactory.getLogger(this.getClass());
	private OBDAModel obdaModel;
	private OWLOntology ontology;

	final String owlfile = "resources/regexp/oracle-regexp.owl";
	final String obdafile = "resources/regexp/oracle-regexp.obda";
	private QuestOWL reasoner;

	@Override
	@Before
	public void setUp() throws Exception {
		
		
		// Loading the OWL file
		OWLOntologyManager manager = OWLManager.createOWLOntologyManager();
		ontology = manager.loadOntologyFromOntologyDocument((new File(owlfile)));

		// Loading the OBDA data
		fac = OBDADataFactoryImpl.getInstance();
		obdaModel = fac.getOBDAModel();
		
		ModelIOManager ioManager = new ModelIOManager(obdaModel);
		ioManager.load(obdafile);
	
		QuestPreferences p = new QuestPreferences();
		p.setCurrentValueOf(QuestPreferences.ABOX_MODE, QuestConstants.VIRTUAL);
		p.setCurrentValueOf(QuestPreferences.OBTAIN_FULL_METADATA, QuestConstants.FALSE);
		
		
		// Creating a new instance of the reasoner
		QuestOWLFactory factory = new QuestOWLFactory();
		factory.setOBDAController(obdaModel);

		factory.setPreferenceHolder(p);

		reasoner = (QuestOWL) factory.createReasoner(ontology, new SimpleConfiguration());

		// Now we are ready for querying
		conn = reasoner.getConnection();

		
	}

	@After
	public void tearDown() throws Exception{
		conn.close();
		reasoner.dispose();
	}
	

	
	private String runTest(QuestOWLStatement st, String query, boolean hasResult) throws Exception {
		String retval;
		QuestOWLResultSet rs = st.executeTuple(query);
		if(hasResult){
			assertTrue(rs.nextRow());
			OWLIndividual ind1 =	rs.getOWLIndividual("country")	 ;
			retval = ind1.toString();
		} else {
			assertFalse(rs.nextRow());
			retval = "";
		}

		return retval;
	}

	/**
	 * Tests the use of mapings with regex in subqueries and where with SQL subquery
	 * @throws Exception
	 */
	@Test
	public void testSparql2OracleRegexWhere() throws Exception {
		QuestOWLStatement st = null;
		try {
			st = conn.createStatement();

			
			
			String query = "PREFIX : <http://www.semanticweb.org/ontologies/2013/7/untitled-ontology-150#> SELECT ?country WHERE {?country a :CountryWithSpace . }";
			String countryName = runTest(st, query, true);
			System.out.println(countryName);
			assertEquals(countryName, "<http://www.semanticweb.org/ontologies/2013/7/untitled-ontology-150#Country-United%20Kingdom>");
		
		
			
		} catch (Exception e) {
			throw e;
		} finally {
			if (st != null)
				st.close();
		}
	}
	
	
	/**
	 * Tests the use of mapings with regex in subqueries without where with SQL subquery
	 * @throws Exception
	 */
	@Test
	public void testSparql2OracleRegexNoWhere() throws Exception {
		QuestOWLStatement st = null;
		try {
			st = conn.createStatement();

			
			
			String query = "PREFIX : <http://www.semanticweb.org/ontologies/2013/7/untitled-ontology-150#> SELECT ?country ?pos WHERE {?country a :CountriesWithSpaceNoWhere . ?country :position ?pos . FILTER (?pos >0)}";
			String countryName = runTest(st, query, true);
			System.out.println(countryName);
			assertEquals(countryName, "<http://www.semanticweb.org/ontologies/2013/7/untitled-ontology-150#Country-United%20Kingdom>");
		
		
			
		} catch (Exception e) {
			throw e;
		} finally {
			if (st != null)
				st.close();
		}
	}

	/**
	 * Tests the use of mapings with regex in subqueries without where without subquery
	 * @throws Exception
	 */
	@Test
	public void testSparql2OracleRegexNoWhereNoSubquery() throws Exception {
		QuestOWLStatement st = null;
		try {
			st = conn.createStatement();

			
			
			String query = "PREFIX : <http://www.semanticweb.org/ontologies/2013/7/untitled-ontology-150#> SELECT ?country ?pos WHERE {?country a :CountriesWithSpaceNoWhereNoSubquery . ?country :position ?pos . FILTER (?pos >0)}";
			String countryName = runTest(st, query, true);
			System.out.println(countryName);
			assertEquals(countryName, "<http://www.semanticweb.org/ontologies/2013/7/untitled-ontology-150#Country-United%20Kingdom>");
		
		
			
		} catch (Exception e) {
			throw e;
		} finally {
			if (st != null)
				st.close();
		}
	}

	

}
