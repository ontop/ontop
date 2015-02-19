package org.semanticweb.ontop.identifiers;

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


import java.io.File;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;
import java.util.Properties;
import java.util.Scanner;

import junit.framework.TestCase;

import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.semanticweb.ontop.owlrefplatform.core.QuestConstants;
import org.semanticweb.ontop.owlrefplatform.core.QuestPreferences;
import org.semanticweb.ontop.owlrefplatform.owlapi3.QuestOWL;
import org.semanticweb.ontop.owlrefplatform.owlapi3.QuestOWLConnection;
import org.semanticweb.ontop.owlrefplatform.owlapi3.QuestOWLFactory;
import org.semanticweb.ontop.owlrefplatform.owlapi3.QuestOWLResultSet;
import org.semanticweb.ontop.owlrefplatform.owlapi3.QuestOWLStatement;
import org.semanticweb.owlapi.apibinding.OWLManager;
import org.semanticweb.owlapi.model.OWLIndividual;
import org.semanticweb.owlapi.model.OWLOntology;
import org.semanticweb.owlapi.model.OWLOntologyManager;
import org.semanticweb.owlapi.reasoner.SimpleConfiguration;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/***
 * Tests that oracle identifiers for tables and columns are treated
 * correctly. Especially, that the unquoted identifers are treated as uppercase, and
 * that the case of quoted identifiers is not changed
 */
public class H2IdentifierTest extends TestCase {

	private QuestOWLConnection conn;

	Logger log = LoggerFactory.getLogger(this.getClass());
	private OWLOntology ontology;

	final String owlfile = "resources/identifiers/identifiers.owl";
	final String obdafile = "resources/identifiers/identifiers-h2.obda";
	private QuestOWL reasoner;
	private Connection sqlConnection;

	@Override
	@Before
	public void setUp() throws Exception {
		try {
			 sqlConnection= DriverManager.getConnection("jdbc:h2:mem:countries","sa", "");
			    java.sql.Statement s = sqlConnection.createStatement();
			  
			    try {
			    	String text = new Scanner( new File("resources/identifiers/create-h2.sql") ).useDelimiter("\\A").next();
			    	s.execute(text);
			    	//Server.startWebServer(sqlConnection);
			    	 
			    } catch(SQLException sqle) {
			        System.out.println("Exception in creating db from script");
			    }
			   
			    s.close();
		
		
		// Loading the OWL file
		OWLOntologyManager manager = OWLManager.createOWLOntologyManager();
		ontology = manager.loadOntologyFromOntologyDocument((new File(owlfile)));
	
		Properties p = new Properties();
		p.setProperty(QuestPreferences.ABOX_MODE, QuestConstants.VIRTUAL);
		p.setProperty(QuestPreferences.OBTAIN_FULL_METADATA, QuestConstants.FALSE);
		// Creating a new instance of the reasoner
		QuestOWLFactory factory = new QuestOWLFactory(new File(obdafile), new QuestPreferences(p));

		reasoner = (QuestOWL) factory.createReasoner(ontology, new SimpleConfiguration());

		// Now we are ready for querying
		conn = reasoner.getConnection();
		} catch (Exception exc) {
			
				tearDown();
				throw exc;
			
		}	
		
	}


	@After
	public void tearDown() throws Exception{
		conn.close();
		reasoner.dispose();
		if (!sqlConnection.isClosed()) {
			java.sql.Statement s = sqlConnection.createStatement();
			try {
				s.execute("DROP ALL OBJECTS DELETE FILES");
			} catch (SQLException sqle) {
				System.out.println("Table not found, not dropping");
			} finally {
				s.close();
				sqlConnection.close();
			}
		}
	}
	

	
	private String runTests(String query) throws Exception {
		QuestOWLStatement st = conn.createStatement();
		String retval;
		try {
			QuestOWLResultSet rs = st.executeTuple(query);
			assertTrue(rs.nextRow());
			OWLIndividual ind1 =	rs.getOWLIndividual("x")	 ;
			retval = ind1.toString();
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
		return retval;
	}

	/**
	 * Test use of lowercase, unquoted table, schema and column identifiers (also in target)
	 * @throws Exception
	 */
	@Test
	public void testLowercaseUnquoted() throws Exception {
		String query = "PREFIX : <http://www.semanticweb.org/ontologies/2013/7/untitled-ontology-150#> SELECT ?x WHERE {?x a :Country} ORDER BY ?x";
		String val = runTests(query);
		assertEquals("<http://www.semanticweb.org/ontologies/2013/7/untitled-ontology-150#Country-Argentina>", val);
	}


	/**
	 * Test use of lowercase, unquoted table and column identifiers (also in target) with uppercase table identifiers
	 * @throws Exception
	 */
	@Test
	public void testUpperCaseTableUnquoted() throws Exception {
		String query = "PREFIX : <http://www.semanticweb.org/ontologies/2013/7/untitled-ontology-150#> SELECT ?x WHERE {?x a :Country2} ORDER BY ?x";
		String val =  runTests(query);
		assertEquals("<http://www.semanticweb.org/ontologies/2013/7/untitled-ontology-150#Country2-Argentina>", val);
	}
	
	/**
	 * Test use of lowercase, quoted alias in a view definition 
	 * @throws Exception
	 */
	@Test
	public void testLowerCaseColumnViewDefQuoted() throws Exception {
		String query = "PREFIX : <http://www.semanticweb.org/ontologies/2013/7/untitled-ontology-150#> SELECT ?x WHERE {?x a :Country4} ORDER BY ?x";
		String val =  runTests(query);
		assertEquals("<http://www.semanticweb.org/ontologies/2013/7/untitled-ontology-150#Country4-1010>", val);
	}

	/**
	 * Test use of lowercase, unquoted alias in a view definition 
	 * @throws Exception
	 */
	@Test
	public void testLowerCaseColumnViewDefUnquoted() throws Exception {
		String query = "PREFIX : <http://www.semanticweb.org/ontologies/2013/7/untitled-ontology-150#> SELECT ?x WHERE {?x a :Country5} ORDER BY ?x";
		String val =  runTests(query);
		assertEquals("<http://www.semanticweb.org/ontologies/2013/7/untitled-ontology-150#Country5-1010>", val);
	}
	
	/**
	 * Test access to lowercase table name, mixed case column name, and constant alias 
	 * @throws Exception
	 */
	@Test
	public void testLowerCaseTable() throws Exception {
		String query = "PREFIX : <http://www.semanticweb.org/ontologies/2013/7/untitled-ontology-150#> SELECT ?x WHERE {?x a :Country3} ORDER BY ?x";
		String val =  runTests(query);
		assertEquals("<http://www.semanticweb.org/ontologies/2013/7/untitled-ontology-150#Country3-BladeRunner-2020-Constant>", val);
	}
	
}

