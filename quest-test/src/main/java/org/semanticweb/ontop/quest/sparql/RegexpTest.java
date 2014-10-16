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

import java.io.File;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;
import java.util.Arrays;
import java.util.Collection;
import java.util.Scanner;

import junit.framework.TestCase;

import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.junit.runners.Parameterized.Parameters;
import org.semanticweb.ontop.io.SQLMappingParser;
import org.semanticweb.ontop.model.OBDADataFactory;
import org.semanticweb.ontop.model.impl.OBDADataFactoryImpl;
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
 * Tests that the system can handle the SPARQL "regex" FILTER
 * This is a parameterized class, see junit documentation
 * Tests oracle, mysql, postgres and mssql
 */
@RunWith(org.junit.runners.Parameterized.class)
public class RegexpTest extends TestCase {

	// TODO We need to extend this test to import the contents of the mappings
	// into OWL and repeat everything taking form OWL

	private OBDADataFactory fac;
	private QuestOWLConnection conn;

	Logger log = LoggerFactory.getLogger(this.getClass());
	private SQLOBDAModel obdaModel;
	private OWLOntology ontology;

	final String owlfile = "src/main/resources/testcases-scenarios/virtual-mode/stockexchange/simplecq/stockexchange.owl";
	private String obdafile;
	
	private QuestOWL reasoner;
	private Connection sqlConnection;
	private boolean isH2;
	/**
	 * Constructor is necessary for parameterized test
	 */
	public RegexpTest(String database, boolean isH2){
		this.obdafile = "src/main/resources/testcases-scenarios/virtual-mode/stockexchange/simplecq/stockexchange-" + database + ".obda";
		this.isH2 = isH2;
	}


	/**
	 * Returns the obda files for the different database engines
	 */
	@Parameters
	public static Collection<Object[]> getObdaFiles(){
		return Arrays.asList(new Object[][] {
				{"h2", true}, 
				 {"mysql", false },
				 {"pgsql", false},
				 {"oracle", false },
				 //no support for mssql and db2
				 });
	}

	
	private void createH2Database() throws Exception {
		try {
			sqlConnection = DriverManager.getConnection("jdbc:h2:mem:questrepository","fish", "fish");
			java.sql.Statement s = sqlConnection.createStatement();

			try {
				String text = new Scanner( new File("resources/regexp/create.sql") ).useDelimiter("\\A").next();
				s.execute(text);
				//Server.startWebServer(sqlConnection);
				
			} catch(SQLException sqle) {
				System.out.println("Exception in creating db from script");
				sqle.printStackTrace();
				throw sqle;
			}

			s.close();
		} catch (Exception exc) {
			try {
				deleteH2Database();
			} catch (Exception e2) {
				e2.printStackTrace();
				throw e2;
			}
		}	
	}
	
	@Override
	@Before
	public void setUp() throws Exception {
		if(this.isH2)
			this.createH2Database();
				
		
		// Loading the OWL file
		OWLOntologyManager manager = OWLManager.createOWLOntologyManager();
		ontology = manager.loadOntologyFromOntologyDocument((new File(owlfile)));

		// Loading the OBDA data
		fac = OBDADataFactoryImpl.getInstance();
		obdaModel = fac.getOBDAModel();
		
		SQLMappingParser ioManager = new SQLMappingParser(obdaModel);
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
		if(this.isH2)
			deleteH2Database();
	}
	
	private void deleteH2Database() throws Exception {
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
	

	private String runTest(QuestOWLStatement st, String query, boolean hasResult) throws Exception {
		String retval;
		QuestOWLResultSet rs = st.executeTuple(query);
		if(hasResult){
			assertTrue(rs.nextRow());
			OWLIndividual ind1 =	rs.getOWLIndividual("x")	 ;
			retval = ind1.toString();
		} else {
			assertFalse(rs.nextRow());
			retval = "";
		}

		return retval;
	}

	/**
	 * Tests the use of SPARQL like
	 * @throws Exception
	 */
	@Test
	public void testSparql2sqlRegex() throws Exception {
		QuestOWLStatement st = null;
		try {
			st = conn.createStatement();

			String[] queries = {
					"'J[ano]*'", 
					"'j[ANO]*', 'i'",
					"'^J[ano]*$'",
					"'^J[ano]*$', 'm'",
					"'J'"
					};
			for (String regex : queries){
				String query = "PREFIX : <http://www.owl-ontologies.com/Ontology1207768242.owl#> SELECT DISTINCT ?x WHERE { ?x a :StockBroker. ?x :firstName ?name. FILTER regex (?name, " + regex + ")}";
				String broker = runTest(st, query, true);
				assertEquals(broker, "<http://www.owl-ontologies.com/Ontology1207768242.owl#person-112>");
			}
			String[] wrongs = {
					"'^j[ANO]*$'",
					"'j[ANO]*'"
					};
			for (String regex : wrongs){
				String query = "PREFIX : <http://www.owl-ontologies.com/Ontology1207768242.owl#> SELECT DISTINCT ?x WHERE { ?x a :StockBroker. ?x :firstName ?name. FILTER regex (?name, " + regex + ")}";
				String res = runTest(st, query, false);
				assertEquals(res, "");
			}
		} catch (Exception e) {
			throw e;
		} finally {
			if (st != null)
				st.close();
		}
	}
	


}
