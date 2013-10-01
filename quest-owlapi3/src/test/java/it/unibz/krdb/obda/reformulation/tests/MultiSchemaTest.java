/*
 * Copyright (C) 2009-2013, Free University of Bozen Bolzano
 * This source code is available under the terms of the Affero General Public
 * License v3.
 * 
 * Please see LICENSE.txt for full license terms, including the availability of
 * proprietary exceptions.
 */
package it.unibz.krdb.obda.reformulation.tests;

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
import java.util.Properties;

import junit.framework.TestCase;

import org.semanticweb.owlapi.apibinding.OWLManager;
import org.semanticweb.owlapi.model.OWLIndividual;
import org.semanticweb.owlapi.model.OWLLiteral;
import org.semanticweb.owlapi.model.OWLOntology;
import org.semanticweb.owlapi.model.OWLOntologyManager;
import org.semanticweb.owlapi.reasoner.SimpleConfiguration;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/***
 * A simple test that check if the system is able to handle Mappings for
 * classes/roles and attributes even if there are no URI templates. i.e., the
 * database stores URI's directly.
 * 
 * We are going to create an H2 DB, the .sql file is fixed. We will map directly
 * there and then query on top.
 */
public class MultiSchemaTest extends TestCase {

	// TODO We need to extend this test to import the contents of the mappings
	// into OWL and repeat everything taking form OWL

	private OBDADataFactory fac;
	private QuestOWLConnection conn;

	Logger log = LoggerFactory.getLogger(this.getClass());
	private OBDAModel obdaModel;
	private OWLOntology ontology;

	final String owlfile = "src/test/resources/test/oracle.owl";
	final String obdafile = "src/test/resources/test/oracle.obda";
	private QuestOWL reasoner;

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
	
		QuestPreferences p = new QuestPreferences();
		p.setCurrentValueOf(QuestPreferences.ABOX_MODE, QuestConstants.VIRTUAL);

		// Creating a new instance of the reasoner
		QuestOWLFactory factory = new QuestOWLFactory();
		factory.setOBDAController(obdaModel);

		factory.setPreferenceHolder(p);

		reasoner = (QuestOWL) factory.createReasoner(ontology, new SimpleConfiguration());

		// Now we are ready for querying
		conn = reasoner.getConnection();

		
	}


	public void tearDown() throws Exception{
		conn.close();
		reasoner.dispose();
	}
	

	
	private void runTests(String query) throws Exception {
		QuestOWLStatement st = conn.createStatement();
		StringBuilder bf = new StringBuilder(query);
		try {
			

			QuestOWLResultSet rs = st.executeTuple(query);
			/*
			boolean nextRow = rs.nextRow();
			
			*/
			assertTrue(rs.nextRow());
			while (rs.nextRow()){
				OWLIndividual ind1 =	rs.getOWLIndividual("x")	 ;
				System.out.println(ind1.toString());
			}
		
/*
			assertEquals("<uri1>", ind1.toString());
			assertEquals("<uri1>", ind2.toString());
			assertEquals("\"value1\"", val.toString());
	*/		

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

	/**
	 * Test use of different schema, table prefix, where clause with "!="
	 * @throws Exception
	 */
	public void testMultiSchemaWhereNot() throws Exception {
		String query = "PREFIX : <http://www.semanticweb.org/ontologies/2013/7/untitled-ontology-150#> SELECT ?x WHERE {?x a :CountryNotEgypt}";
		runTests(query);
	}
	

	/**
	 * Test use of different schema, table prefix, where clause and join
	 * @throws Exception
	 */
	public void testMultiSchemaWherePrefix() throws Exception {
		String query = "PREFIX : <http://www.semanticweb.org/ontologies/2013/7/untitled-ontology-150#> SELECT ?x ?r WHERE { ?x :countryIsInRegion ?r }";
		runTests(query);
	}
	
	/**
	 * Tests simplest possible use of different schema than logged in user
	 * @throws Exception
	 */
	public void testMultiSchema() throws Exception {
		String query = "PREFIX : <http://www.semanticweb.org/ontologies/2013/7/untitled-ontology-150#> SELECT ?x WHERE { ?x a :Country }";
		runTests(query);
	}

	/**
	 * Tests simplest possible use of different schema than logged in user without quotation marks
	 * @throws Exception
	 */
	public void testMultiSchemaNQ() throws Exception {
		String query = "PREFIX : <http://www.semanticweb.org/ontologies/2013/7/untitled-ontology-150#> SELECT ?x WHERE { ?x a :CountryPrefixNQ }";
		runTests(query);
	}

	
	/**
	 * Test us of different schema together with table prefix in column name
	 * @throws Exception
	 */
	public void testMultiSchemaPrefix() throws Exception {
		String query = "PREFIX : <http://www.semanticweb.org/ontologies/2013/7/untitled-ontology-150#> SELECT ?x WHERE { ?x a :Pais }";
		runTests(query);
	}


	/**
	 * Test use of different schema and table prefix in column name, and column alias
	 * @throws Exception
	 */
	public void testMultiSchemaAlias() throws Exception {
		String query = "PREFIX : <http://www.semanticweb.org/ontologies/2013/7/untitled-ontology-150#> SELECT ?x WHERE { ?x a :Land }";
		runTests(query);
	}

	/**
	 * Test use of different schema and table prefix in column name, and column alias, and quote in table prefix
	 * @throws Exception
	 */
	public void testMultiSchemaAliasQuote() throws Exception {
		String query = "PREFIX : <http://www.semanticweb.org/ontologies/2013/7/untitled-ontology-150#> SELECT ?x WHERE { ?x a :LandQuote }";
		runTests(query);
	}
	
	/**
	 * Test use of different schema and table prefix in where clause
	 * @throws Exception
	 */
	public void testMultiSchemaWhere() throws Exception {
		String query = "PREFIX : <http://www.semanticweb.org/ontologies/2013/7/untitled-ontology-150#> SELECT ?x WHERE { ?x a :CountryEgypt }";
		runTests(query);
	}
		
}
