package it.unibz.inf.ontop.owlapi;

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

import java.io.IOException;
import java.sql.Connection;
import java.sql.DriverManager;
import java.util.Properties;

import it.unibz.inf.ontop.injection.OntopSQLOWLAPIConfiguration;
import junit.framework.TestCase;

import it.unibz.inf.ontop.exception.InvalidMappingException;

import static it.unibz.inf.ontop.utils.OWLAPITestingTools.executeFromFile;


/***
 * A simple test that check if the system is able to handle mapping variants
 * to construct the proper datalog program.
 */
public class MappingAnalyzerTest extends TestCase {

	private Connection conn;

	private static final String owlfile = "src/test/resources/test/mappinganalyzer/ontology.owl";

	private static final String url = "jdbc:h2:mem:questjunitdb";
	private static final String username = "sa";
	private static final String password = "";

	@Override
	public void setUp() throws Exception {
		// Initializing and H2 database with the stock exchange data
		conn = DriverManager.getConnection(url, username, password);
		executeFromFile(conn, "src/test/resources/test/mappinganalyzer/create-tables.sql");
	}

	@Override
	public void tearDown() throws Exception {
		executeFromFile(conn, "src/test/resources/test/mappinganalyzer/drop-tables.sql");
		conn.close();
	}

	private void runTests(String obdaFileName) throws Exception {
		Properties p = new Properties();
		// Creating a new instance of the reasoner
		OntopOWLFactory factory = OntopOWLFactory.defaultFactory();

		OntopSQLOWLAPIConfiguration configuration = OntopSQLOWLAPIConfiguration.defaultBuilder()
				.nativeOntopMappingFile(obdaFileName)
				.ontologyFile(owlfile)
				.properties(p)
				.jdbcUrl(url)
				.jdbcUser(username)
				.jdbcPassword(password)
				.enableTestMode()
				.build();

		factory.createReasoner(configuration);
	}

	public void testMapping_1() {
		try {
			runTests("src/test/resources/test/mappinganalyzer/case_1.obda");
		} catch (Exception e) {
			assertTrue(e.toString(), false);
		}
	}
	
	public void testMapping_2() {
		try {
            runTests("src/test/resources/test/mappinganalyzer/case_2.obda");
		} catch (Exception e) {
			assertTrue(e.toString(), false);
		}
	}
	
	public void testMapping_3() {
		try {
            runTests("src/test/resources/test/mappinganalyzer/case_3.obda");
		} catch (Exception e) {
			assertTrue(e.toString(), false);
		}
	}
	
	public void testMapping_4() {
		try {
            runTests("src/test/resources/test/mappinganalyzer/case_4.obda");
		} catch (Exception e) {
			assertTrue(e.toString(), false);
		}
	}
	
	public void testMapping_5() {
		try {
            runTests("src/test/resources/test/mappinganalyzer/case_5.obda");
		} catch (Exception e) {
			assertTrue(e.toString(), true); // FAIL
		}
	}
	
	public void testMapping_6() {
		try {
            runTests("src/test/resources/test/mappinganalyzer/case_6.obda");
		} catch (Exception e) {
			assertTrue(e.toString(), false);
		}
	}
	
	public void testMapping_7() {
		try {
            runTests("src/test/resources/test/mappinganalyzer/case_7.obda");
		} catch (Exception e) {
			assertTrue(e.toString(), false);
		}
	}
	
	public void testMapping_8() {
		try {
            runTests("src/test/resources/test/mappinganalyzer/case_8.obda");
		} catch (Exception e) {
			assertTrue(e.toString(), true); // FAIL we cannot handle the case in the look up table were id map to two different values
		}
	}
	
	public void testMapping_9()  {
		try {
            runTests("src/test/resources/test/mappinganalyzer/case_9.obda");
		} catch (Exception e) {
			assertTrue(e.toString(), true); // FAIL we cannot handle the case in the look up table were id map to two different values
		}
	}
	
	public void testMapping_10() {
		try {
            runTests("src/test/resources/test/mappinganalyzer/case_10.obda");
		} catch (Exception e) {
			assertTrue(e.toString(), true); // FAIL we cannot handle the case in the look up table were alias map to two different values
		}
	}
	
	public void testMapping_11() {
		try {
            runTests("src/test/resources/test/mappinganalyzer/case_11.obda");
		} catch (Exception e) {
			assertTrue(e.toString(), false);
		}
	}
	
	public void testMapping_12() {
		try {
            runTests("src/test/resources/test/mappinganalyzer/case_12.obda");
		} catch (Exception e) {
			assertTrue(e.toString(), true); // FAIL we cannot handle the case in the look up table were name map to two different values
		}
	}
	
	public void testMapping_13() {
		try {
            runTests("src/test/resources/test/mappinganalyzer/case_13.obda");
		} catch (Exception e) {
			assertTrue(e.toString(), false);
		}
	}
	
	public void testMapping_14() {
		try {
            runTests("src/test/resources/test/mappinganalyzer/case_14.obda");
		} catch (Exception e) {
			assertTrue(e.toString(), true); // FAIL
		}
	}
	
	public void testMapping_15() {
		try {
            runTests("src/test/resources/test/mappinganalyzer/case_15.obda");
		} catch (Exception e) {
			assertTrue(e.toString(), false);
		}
	}
	
	public void testMapping_16()  {
		try {
            runTests("src/test/resources/test/mappinganalyzer/case_16.obda");
		} catch (Exception e) {
			assertTrue(e.toString(), false);
		}
	}
	
	public void testMapping_17()  {
		try {
            runTests("src/test/resources/test/mappinganalyzer/case_17.obda");
		} catch (Exception e) {
			assertTrue(e.toString(), true); // FAIL
		}
	}
	
	public void testMapping_18()  {
		try {
            runTests("src/test/resources/test/mappinganalyzer/case_18.obda");
		} catch (Exception e) {
			assertTrue(e.toString(), false);
		}
	}
}
