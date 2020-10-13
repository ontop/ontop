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
import org.junit.*;

/***
 * Tests that H2 identifiers for tables and columns are treated
 * correctly. Especially, that the unquoted identifiers are treated as uppercase, and
 * that the case of quoted identifiers is not changed
 */
public class H2IdentifierTest extends AbstractOWLAPITest {

	@BeforeClass
	public static void setUp() throws Exception {
		initOBDA("/identifiers/create-h2.sql",
				"/identifiers/identifiers-h2.obda",
				"/identifiers/identifiers.owl");
	}

	@AfterClass
	public static void tearDown() throws Exception {
		release();
	}

	/**
	 * Test use of lowercase, unquoted table, schema and column identifiers (also in target)
	 */
	@Test
	public void testLowercaseUnquoted() throws Exception {
		String query = "PREFIX : <http://www.semanticweb.org/ontologies/2013/7/untitled-ontology-150#> " +
				"SELECT ?v WHERE {?v a :Country} ORDER BY ?v";
		checkReturnedValues(query, "v", ImmutableList.of(
				"<http://www.semanticweb.org/ontologies/2013/7/untitled-ontology-150#Country-Argentina>"));
	}

	/**
	 * Test use of lowercase, unquoted table and column identifiers (also in target) with uppercase table identifiers
	 */
	@Test
	public void testUpperCaseTableUnquoted() throws Exception {
		String query = "PREFIX : <http://www.semanticweb.org/ontologies/2013/7/untitled-ontology-150#> " +
				"SELECT ?v WHERE {?v a :Country2} ORDER BY ?v";
		checkReturnedValues(query, "v", ImmutableList.of(
				"<http://www.semanticweb.org/ontologies/2013/7/untitled-ontology-150#Country2-Argentina>"));
	}
	
	/**
	 * Test use of lowercase, quoted alias in a view definition 
	 */
	@Test
	public void testLowerCaseColumnViewDefQuoted() throws Exception {
		String query = "PREFIX : <http://www.semanticweb.org/ontologies/2013/7/untitled-ontology-150#> " +
				"SELECT ?v WHERE {?v a :Country4} ORDER BY ?v";
		checkReturnedValues(query, "v", ImmutableList.of(
				"<http://www.semanticweb.org/ontologies/2013/7/untitled-ontology-150#Country4-1010>"));
	}

	/**
	 * Test use of lowercase, unquoted alias in a view definition 
	 */
	@Test
	public void testLowerCaseColumnViewDefUnquoted() throws Exception {
		String query = "PREFIX : <http://www.semanticweb.org/ontologies/2013/7/untitled-ontology-150#> " +
				"SELECT ?v WHERE {?v a :Country5} ORDER BY ?v";
		checkReturnedValues(query, "v", ImmutableList.of(
				"<http://www.semanticweb.org/ontologies/2013/7/untitled-ontology-150#Country5-1010>"));
	}
	
	/**
	 * Test access to lowercase table name, mixed case column name, and constant alias 
	 */
	@Test
	public void testLowerCaseTable() throws Exception {
		String query = "PREFIX : <http://www.semanticweb.org/ontologies/2013/7/untitled-ontology-150#> " +
				"SELECT ?v WHERE {?v a :Country3} ORDER BY ?v";
		checkReturnedValues(query, "v", ImmutableList.of(
				"<http://www.semanticweb.org/ontologies/2013/7/untitled-ontology-150#Country3-BladeRunner-2020-Constant>"));
	}

	@Test
	public void testLowerCaseTableWithSymbol() throws Exception {
		String query = "PREFIX : <http://www.semanticweb.org/ontologies/2013/7/untitled-ontology-150#> " +
				"SELECT ?v WHERE {?v a :NoCountry} ORDER BY ?v";
		checkReturnedValues(query, "v", ImmutableList.of(
				"<http://www.semanticweb.org/ontologies/2013/7/untitled-ontology-150#CountryNo-Atlantis>"));
	}
}

