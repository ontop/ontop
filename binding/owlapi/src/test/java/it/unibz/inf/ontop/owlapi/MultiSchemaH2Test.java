package it.unibz.inf.ontop.owlapi;

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


import com.google.common.collect.ImmutableList;
import it.unibz.inf.ontop.injection.OntopSQLOWLAPIConfiguration;
import it.unibz.inf.ontop.owlapi.connection.OWLConnection;
import it.unibz.inf.ontop.owlapi.connection.OWLStatement;
import it.unibz.inf.ontop.owlapi.resultset.TupleOWLResultSet;
import org.junit.*;

import java.sql.Connection;
import java.sql.DriverManager;

import static it.unibz.inf.ontop.utils.OWLAPITestingTools.executeFromFile;
import static junit.framework.TestCase.assertTrue;

/***
 * A simple test that check if the system is able to handle Mappings for
 * classes/roles and attributes even if there are no URI templates. i.e., the
 * database stores URI's directly.
 * 
 * We are going to create an H2 DB, the .sql file is fixed. We will map directly
 * there and then query on top.
 */
public class MultiSchemaH2Test extends AbstractOWLAPITest {

	@BeforeClass
	public static void setUp() throws Exception {
		initOBDA("/multischema/stockexchange-h2Schema.sql",
				"/multischema/multischemah2.obda",
				"/multischema/multischemah2.owl");
	}

	@AfterClass
	public static void tearDown() throws Exception {
		release();
	}

	@Test
	public void testOneSchema() throws Exception {
		String query = "PREFIX : <http://www.owl-ontologies.com/Ontology1207768242.owl#> " +
				"SELECT ?x WHERE {?x a :Address}";
		checkReturnedValues(query, "x", ImmutableList.of(
				"<http://www.owl-ontologies.com/Ontology1207768242.owl#Address-991>",
				"<http://www.owl-ontologies.com/Ontology1207768242.owl#Address-992>",
				"<http://www.owl-ontologies.com/Ontology1207768242.owl#Address-993>",
				"<http://www.owl-ontologies.com/Ontology1207768242.owl#Address-995>",
				"<http://www.owl-ontologies.com/Ontology1207768242.owl#Address-996>",
				"<http://www.owl-ontologies.com/Ontology1207768242.owl#Address-997>",
				"<http://www.owl-ontologies.com/Ontology1207768242.owl#Address-998>"));
	}
	@Test
	public void testTableOneSchema() throws Exception {
		String query = "PREFIX : <http://www.owl-ontologies.com/Ontology1207768242.owl#> " +
				"SELECT ?x WHERE {?x a :Broker}";
		checkReturnedValues(query, "x", ImmutableList.of(
				"<http://www.owl-ontologies.com/Ontology1207768242.owl#Broker-112>",
				"<http://www.owl-ontologies.com/Ontology1207768242.owl#Broker-113>",
				"<http://www.owl-ontologies.com/Ontology1207768242.owl#Broker-114>"));
	}

	@Test
	public void testAliasOneSchema() throws Exception {
		String query = "PREFIX : <http://www.owl-ontologies.com/Ontology1207768242.owl#> " +
				"SELECT ?x WHERE {?x a :Worker}";
		checkReturnedValues(query, "x", ImmutableList.of(
				"<http://www.owl-ontologies.com/Ontology1207768242.owl#Worker-112>",
				"<http://www.owl-ontologies.com/Ontology1207768242.owl#Worker-113>",
				"<http://www.owl-ontologies.com/Ontology1207768242.owl#Worker-114>"));
	}

	@Test
	public void testSchemaWhere() throws Exception {
		String query = "PREFIX : <http://www.owl-ontologies.com/Ontology1207768242.owl#> " +
				"SELECT ?x ?r WHERE { ?x :isBroker ?r }";
		checkReturnedValues(query, "x", ImmutableList.of(
				"<http://www.owl-ontologies.com/Ontology1207768242.owl#Client-112>"));
	}

	@Test
	public void testMultischema() throws Exception {
		String query = "PREFIX : <http://www.owl-ontologies.com/Ontology1207768242.owl#> " +
				"SELECT ?x WHERE { ?x :hasFile ?r }";
		checkReturnedValues(query, "x", ImmutableList.of(
				"<http://www.owl-ontologies.com/Ontology1207768242.owl#Broker2-997>",
				"<http://www.owl-ontologies.com/Ontology1207768242.owl#Broker2-996>",
				"<http://www.owl-ontologies.com/Ontology1207768242.owl#Broker2-995>",
				"<http://www.owl-ontologies.com/Ontology1207768242.owl#Broker2-993>",
				"<http://www.owl-ontologies.com/Ontology1207768242.owl#Broker2-992>",
				"<http://www.owl-ontologies.com/Ontology1207768242.owl#Broker2-991>",
				"<http://www.owl-ontologies.com/Ontology1207768242.owl#Broker2-998>"));
	}
}
