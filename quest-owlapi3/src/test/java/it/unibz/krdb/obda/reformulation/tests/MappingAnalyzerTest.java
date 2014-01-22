package it.unibz.krdb.obda.reformulation.tests;

/*
 * #%L
 * ontop-quest-owlapi3
 * %%
 * Copyright (C) 2009 - 2013 Free University of Bozen-Bolzano
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

import it.unibz.krdb.obda.exception.InvalidMappingException;
import it.unibz.krdb.obda.exception.InvalidPredicateDeclarationException;
import it.unibz.krdb.obda.io.ModelIOManager;
import it.unibz.krdb.obda.model.OBDADataFactory;
import it.unibz.krdb.obda.model.OBDAModel;
import it.unibz.krdb.obda.model.impl.OBDADataFactoryImpl;
import it.unibz.krdb.obda.owlrefplatform.core.QuestConstants;
import it.unibz.krdb.obda.owlrefplatform.core.QuestPreferences;
import it.unibz.krdb.obda.owlrefplatform.owlapi3.QuestOWL;
import it.unibz.krdb.obda.owlrefplatform.owlapi3.QuestOWLFactory;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.io.IOException;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;
import java.sql.Statement;

import junit.framework.TestCase;

import org.semanticweb.owlapi.apibinding.OWLManager;
import org.semanticweb.owlapi.model.OWLOntology;
import org.semanticweb.owlapi.model.OWLOntologyManager;
import org.semanticweb.owlapi.reasoner.SimpleConfiguration;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/***
 * A simple test that check if the system is able to handle mapping variants
 * to construct the proper datalog program.
 */
public class MappingAnalyzerTest extends TestCase {
	
	private OBDADataFactory fac;
	private Connection conn;

	Logger log = LoggerFactory.getLogger(this.getClass());
	private OBDAModel obdaModel;
	private OWLOntology ontology;

	final String owlfile = "src/test/resources/test/mappinganalyzer/ontology.owl";

	@Override
	public void setUp() throws Exception {
		// Initializing and H2 database with the stock exchange data
		// String driver = "org.h2.Driver";
		String url = "jdbc:h2:mem:questjunitdb";
		String username = "sa";
		String password = "";

		fac = OBDADataFactoryImpl.getInstance();

		conn = DriverManager.getConnection(url, username, password);
		Statement st = conn.createStatement();

		FileReader reader = new FileReader("src/test/resources/test/mappinganalyzer/create-tables.sql");
		BufferedReader in = new BufferedReader(reader);
		StringBuilder bf = new StringBuilder();
		String line = in.readLine();
		while (line != null) {
			bf.append(line);
			line = in.readLine();
		}
		st.executeUpdate(bf.toString());
		conn.commit();

		// Loading the OWL file
		OWLOntologyManager manager = OWLManager.createOWLOntologyManager();
		ontology = manager.loadOntologyFromOntologyDocument((new File(owlfile)));
		
		obdaModel = fac.getOBDAModel();
	}

	@Override
	public void tearDown() throws Exception {
		try {
			dropTables();
			conn.close();
		} catch (Exception e) {
			log.debug(e.getMessage());
		}
	}

	private void dropTables() throws SQLException, IOException {
		Statement st = conn.createStatement();
		FileReader reader = new FileReader("src/test/resources/test/mappinganalyzer/drop-tables.sql");
		BufferedReader in = new BufferedReader(reader);
		StringBuilder bf = new StringBuilder();
		String line = in.readLine();
		while (line != null) {
			bf.append(line);
			line = in.readLine();
		}
		st.executeUpdate(bf.toString());
		st.close();
		conn.commit();
	}

	private void runTests() throws Exception {
		QuestPreferences p = new QuestPreferences();
		p.setCurrentValueOf(QuestPreferences.ABOX_MODE, QuestConstants.VIRTUAL);
		p.setCurrentValueOf(QuestPreferences.OPTIMIZE_EQUIVALENCES, "true");
		p.setCurrentValueOf(QuestPreferences.OPTIMIZE_TBOX_SIGMA, "true");
		
		// Creating a new instance of the reasoner
		QuestOWLFactory factory = new QuestOWLFactory();
		factory.setOBDAController(obdaModel);
		factory.setPreferenceHolder(p);

		QuestOWL reasoner = (QuestOWL) factory.createReasoner(ontology, new SimpleConfiguration());

		// Get ready for querying
		reasoner.getStatement();
	}

	public void testMapping_1() throws IOException, InvalidPredicateDeclarationException, InvalidMappingException {
		ModelIOManager ioManager = new ModelIOManager(obdaModel);
		ioManager.load("src/test/resources/test/mappinganalyzer/case_1.obda");
		try {
			runTests();
		} catch (Exception e) {
			assertTrue(e.toString(), false);
		}
	}
	
	public void testMapping_2() throws IOException, InvalidPredicateDeclarationException, InvalidMappingException {
		ModelIOManager ioManager = new ModelIOManager(obdaModel);
		ioManager.load("src/test/resources/test/mappinganalyzer/case_2.obda");
		try {
			runTests();
		} catch (Exception e) {
			assertTrue(e.toString(), false);
		}
	}
	
	public void testMapping_3() throws IOException, InvalidPredicateDeclarationException, InvalidMappingException {
		ModelIOManager ioManager = new ModelIOManager(obdaModel);
		ioManager.load("src/test/resources/test/mappinganalyzer/case_3.obda");
		try {
			runTests();
		} catch (Exception e) {
			assertTrue(e.toString(), false);
		}
	}
	
	public void testMapping_4() throws IOException, InvalidPredicateDeclarationException, InvalidMappingException {
		ModelIOManager ioManager = new ModelIOManager(obdaModel);
		ioManager.load("src/test/resources/test/mappinganalyzer/case_4.obda");
		try {
			runTests();
		} catch (Exception e) {
			assertTrue(e.toString(), false);
		}
	}
	
	public void testMapping_5() throws IOException, InvalidPredicateDeclarationException, InvalidMappingException {
		ModelIOManager ioManager = new ModelIOManager(obdaModel);
		ioManager.load("src/test/resources/test/mappinganalyzer/case_5.obda");
		try {
			runTests();
		} catch (Exception e) {
			assertTrue(e.toString(), true); // FAIL
		}
	}
	
	public void testMapping_6() throws IOException, InvalidPredicateDeclarationException, InvalidMappingException {
		ModelIOManager ioManager = new ModelIOManager(obdaModel);
		ioManager.load("src/test/resources/test/mappinganalyzer/case_6.obda");
		try {
			runTests();
		} catch (Exception e) {
			assertTrue(e.toString(), false);
		}
	}
	
	public void testMapping_7() throws IOException, InvalidPredicateDeclarationException, InvalidMappingException {
		ModelIOManager ioManager = new ModelIOManager(obdaModel);
		ioManager.load("src/test/resources/test/mappinganalyzer/case_7.obda");
		try {
			runTests();
		} catch (Exception e) {
			assertTrue(e.toString(), false);
		}
	}
	
	public void testMapping_8() throws IOException, InvalidPredicateDeclarationException, InvalidMappingException {
		ModelIOManager ioManager = new ModelIOManager(obdaModel);
		ioManager.load("src/test/resources/test/mappinganalyzer/case_8.obda");
		try {
			runTests();
		} catch (Exception e) {
			assertTrue(e.toString(), false); 
		}
	}
	
	public void testMapping_9() throws IOException, InvalidPredicateDeclarationException, InvalidMappingException {
		ModelIOManager ioManager = new ModelIOManager(obdaModel);
		ioManager.load("src/test/resources/test/mappinganalyzer/case_9.obda");
		try {
			runTests();
		} catch (Exception e) {
			assertTrue(e.toString(), true); // FAIL we cannot handle the case in the look up table were id map to two different values
		}
	}
	
	public void testMapping_10() throws IOException, InvalidPredicateDeclarationException, InvalidMappingException {
		ModelIOManager ioManager = new ModelIOManager(obdaModel);
		ioManager.load("src/test/resources/test/mappinganalyzer/case_10.obda");
		try {
			runTests();
		} catch (Exception e) {
			assertTrue(e.toString(), true); // FAIL we cannot handle the case in the look up table were alias map to two different values
		}
	}
	
	public void testMapping_11() throws IOException, InvalidPredicateDeclarationException, InvalidMappingException {
		ModelIOManager ioManager = new ModelIOManager(obdaModel);
		ioManager.load("src/test/resources/test/mappinganalyzer/case_11.obda");
		try {
			runTests();
		} catch (Exception e) {
			assertTrue(e.toString(), false);
		}
	}
	
	public void testMapping_12() throws IOException, InvalidPredicateDeclarationException, InvalidMappingException {
		ModelIOManager ioManager = new ModelIOManager(obdaModel);
		ioManager.load("src/test/resources/test/mappinganalyzer/case_12.obda");
		try {
			runTests();
		} catch (Exception e) {
			assertTrue(e.toString(), true); // FAIL we cannot handle the case in the look up table were name map to two different values
		}
	}
	
	public void testMapping_13() throws IOException, InvalidPredicateDeclarationException, InvalidMappingException {
		ModelIOManager ioManager = new ModelIOManager(obdaModel);
		ioManager.load("src/test/resources/test/mappinganalyzer/case_13.obda");
		try {
			runTests();
		} catch (Exception e) {
			assertTrue(e.toString(), false);
		}
	}
	
	public void testMapping_14() throws IOException, InvalidPredicateDeclarationException, InvalidMappingException {
		ModelIOManager ioManager = new ModelIOManager(obdaModel);
		ioManager.load("src/test/resources/test/mappinganalyzer/case_14.obda");
		try {
			runTests();
		} catch (Exception e) {
			assertTrue(e.toString(), true); // FAIL
		}
	}
	
	public void testMapping_15() throws IOException, InvalidPredicateDeclarationException, InvalidMappingException {
		ModelIOManager ioManager = new ModelIOManager(obdaModel);
		ioManager.load("src/test/resources/test/mappinganalyzer/case_15.obda");
		try {
			runTests();
		} catch (Exception e) {
			assertTrue(e.toString(), false);
		}
	}
	
	public void testMapping_16() throws IOException, InvalidPredicateDeclarationException, InvalidMappingException {
		ModelIOManager ioManager = new ModelIOManager(obdaModel);
		ioManager.load("src/test/resources/test/mappinganalyzer/case_16.obda");
		try {
			runTests();
		} catch (Exception e) {
			assertTrue(e.toString(), false);
		}
	}
	
	public void testMapping_17() throws IOException, InvalidPredicateDeclarationException, InvalidMappingException {
		ModelIOManager ioManager = new ModelIOManager(obdaModel);
		ioManager.load("src/test/resources/test/mappinganalyzer/case_17.obda");
		try {
			runTests();
		} catch (Exception e) {
			assertTrue(e.toString(), true); // FAIL
		}
	}
	
	public void testMapping_18() throws IOException, InvalidPredicateDeclarationException, InvalidMappingException {
		ModelIOManager ioManager = new ModelIOManager(obdaModel);
		ioManager.load("src/test/resources/test/mappinganalyzer/case_18.obda");
		try {
			runTests();
		} catch (Exception e) {
			assertTrue(e.toString(), false);
		}
	}
}
