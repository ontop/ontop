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

import java.sql.Connection;
import java.sql.DriverManager;
import java.util.Properties;

import it.unibz.inf.ontop.injection.OntopSQLOWLAPIConfiguration;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.semanticweb.owlapi.reasoner.IllegalConfigurationException;

import static it.unibz.inf.ontop.utils.OWLAPITestingTools.executeFromFile;


/***
 * A simple test that check if the system is able to handle mapping variants
 * to construct the proper datalog program.
 */
public class MappingAnalyzerTest {

	private Connection conn;

	private static final String owlfile = "src/test/resources/test/mappinganalyzer/ontology.owl";

	private static final String url = "jdbc:h2:mem:questjunitdb";
	private static final String username = "sa";
	private static final String password = "";

	@Before
	public void setUp() throws Exception {
		conn = DriverManager.getConnection(url, username, password);
		executeFromFile(conn, "src/test/resources/test/mappinganalyzer/create-tables.sql");
	}

	@After
	public void tearDown() throws Exception {
		executeFromFile(conn, "src/test/resources/test/mappinganalyzer/drop-tables.sql");
		conn.close();
	}

	private void runTests(String obdaFileName) throws Exception {
		OntopOWLFactory factory = OntopOWLFactory.defaultFactory();
		OntopSQLOWLAPIConfiguration configuration = OntopSQLOWLAPIConfiguration.defaultBuilder()
				.nativeOntopMappingFile(obdaFileName)
				.ontologyFile(owlfile)
				.jdbcUrl(url)
				.jdbcUser(username)
				.jdbcPassword(password)
				.enableTestMode()
				.build();

		OntopOWLReasoner reasoner = factory.createReasoner(configuration);
		reasoner.close();
	}

	@Test
	public void testMapping_1() throws Exception {
		runTests("src/test/resources/test/mappinganalyzer/case_1.obda");
	}

	@Test
	public void testMapping_2() throws Exception {
		runTests("src/test/resources/test/mappinganalyzer/case_2.obda");
	}

	@Test
	public void testMapping_3() throws Exception {
		runTests("src/test/resources/test/mappinganalyzer/case_3.obda");
	}

	@Test
	public void testMapping_4() throws Exception {
		runTests("src/test/resources/test/mappinganalyzer/case_4.obda");
	}

	@Test(expected = IllegalConfigurationException.class)
	public void test_5_ambiguous_column_name() throws Exception {
		runTests("src/test/resources/test/mappinganalyzer/case_5.obda");
	}

	@Test
	public void testMapping_6() throws Exception {
		runTests("src/test/resources/test/mappinganalyzer/case_6.obda");
	}

	@Test
	public void testMapping_7() throws Exception {
		runTests("src/test/resources/test/mappinganalyzer/case_7.obda");
	}

	@Test(expected = IllegalConfigurationException.class)
	public void test_8_duplicate_column_name() throws Exception {
		runTests("src/test/resources/test/mappinganalyzer/case_8.obda");
	}

	@Test(expected = IllegalConfigurationException.class)
	public void test_9_placeholder_does_not_match() throws Exception {
		runTests("src/test/resources/test/mappinganalyzer/case_9.obda");
	}

	@Test(expected = IllegalConfigurationException.class)
	public void test_10_duplicate_column_name() throws Exception {
		runTests("src/test/resources/test/mappinganalyzer/case_10.obda");
	}

	@Test
	public void testMapping_11() throws Exception {
		runTests("src/test/resources/test/mappinganalyzer/case_11.obda");
	}

	@Test
	public void testMapping_12() throws Exception {
		runTests("src/test/resources/test/mappinganalyzer/case_12.obda");
	}

	@Test
	public void testMapping_13() throws Exception {
		runTests("src/test/resources/test/mappinganalyzer/case_13.obda");
	}

	@Test(expected = IllegalConfigurationException.class)
	public void test_14_fqdn_in_target() throws Exception {
		runTests("src/test/resources/test/mappinganalyzer/case_14.obda");
	}

	@Test
	public void testMapping_15() throws Exception {
		runTests("src/test/resources/test/mappinganalyzer/case_15.obda");
	}

	@Test
	public void testMapping_16() throws Exception {
		runTests("src/test/resources/test/mappinganalyzer/case_16.obda");
	}

	@Test(expected = IllegalConfigurationException.class)
	public void test_17_ambigous_column_name() throws Exception {
		runTests("src/test/resources/test/mappinganalyzer/case_17.obda");
	}

	@Test
	public void testMapping_18() throws Exception {
		runTests("src/test/resources/test/mappinganalyzer/case_18.obda");
	}
}
