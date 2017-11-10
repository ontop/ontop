package it.unibz.inf.ontop.docker.oracle;

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


import it.unibz.inf.ontop.docker.AbstractVirtualModeTest;
import org.junit.Test;

/***
 * A simple test that check if the system is able to handle Mappings for
 * classes/roles and attributes even if there are no URI templates. i.e., the
 * database stores URI's directly.
 * 
 * We are going to create an H2 DB, the .sql file is fixed. We will map directly
 * there and then query on top.
 */
public class MultiSchemaTest extends AbstractVirtualModeTest {

	static final String owlfile = "/oracle/oracle-schema.owl";
	static final String obdafile = "/oracle/oracle-schema.obda";
	static final String propertiesfile = "/oracle/oracle.properties";

	public MultiSchemaTest() {
		super(owlfile, obdafile, propertiesfile);
	}

	/**
	 * Test use of two aliases to same table
	 * @throws Exception
	 */
	@Test
	public void testMultiSchemaAliases() throws Exception {
		String query = "PREFIX : <http://www.semanticweb.org/ontologies/2013/7/untitled-ontology-150#> SELECT ?x WHERE {?x a :View}";
		checkThereIsAtLeastOneResult(query);
	}
	
	/**
	 * Test use of three aliases to same table, and a reference to the second
	 * @throws Exception
	 */
	@Test
	public void testMultiSchemaAlias2() throws Exception {
		String query = "PREFIX : <http://www.semanticweb.org/ontologies/2013/7/untitled-ontology-150#> SELECT ?x WHERE {?x a :View2}";
		checkThereIsAtLeastOneResult(query);
	}
	
	/**
	 * Test alias together with wrong case for table
	 * @throws Exception
	 */
	@Test
	public void testMultiSchemaCapitalAlias() throws Exception {
		String query = "PREFIX : <http://www.semanticweb.org/ontologies/2013/7/untitled-ontology-150#> SELECT ?x WHERE {?x a :Something}";
		checkThereIsAtLeastOneResult(query);
	}
	
	/**
	 * Test use of views
	 * @throws Exception
	 */
	@Test
	public void testMultiSchemaView() throws Exception {
		String query = "PREFIX : <http://www.semanticweb.org/ontologies/2013/7/untitled-ontology-150#> SELECT ?x WHERE {?x a :NewCountry}";
		checkThereIsAtLeastOneResult(query);
	}
	
	
	/**
	 * Test use of different schema, table prefix, and non-supported function in select clause
	 * @throws Exception
	 */
	@Test
	public void testMultiSchemaToChar() throws Exception {
		String query = "PREFIX : <http://www.semanticweb.org/ontologies/2013/7/untitled-ontology-150#> SELECT ?x WHERE {?x a :RegionID}";
		checkThereIsAtLeastOneResult(query);
	}
	
	/**
	 * Test use of different schema, table prefix, where clause with "!="
	 * @throws Exception
	 */
	@Test
	public void testMultiSchemaWhereNot() throws Exception {
		String query = "PREFIX : <http://www.semanticweb.org/ontologies/2013/7/untitled-ontology-150#> SELECT ?x WHERE {?x a :CountryNotEgypt}";
		checkThereIsAtLeastOneResult(query);
	}
	

	/**
	 * Test use of different schema, table prefix, where clause and join
	 * @throws Exception
	 */
	@Test
	public void testMultiSchemaWherePrefix() throws Exception {
		String query = "PREFIX : <http://www.semanticweb.org/ontologies/2013/7/untitled-ontology-150#> SELECT ?x ?r WHERE { ?x :countryIsInRegion ?r }";
		checkThereIsAtLeastOneResult(query);
	}
	
	/**
	 * Tests simplest possible use of different schema than logged in user
	 * @throws Exception
	 */
	@Test
	public void testMultiSchema() throws Exception {
		String query = "PREFIX : <http://www.semanticweb.org/ontologies/2013/7/untitled-ontology-150#> SELECT ?x WHERE { ?x a :Country }";
		checkThereIsAtLeastOneResult(query);
	}

	/**
	 * Tests simplest possible use of different schema than logged in user without quotation marks
	 * @throws Exception
	 */
	@Test
	public void testMultiSchemaNQ() throws Exception {
		String query = "PREFIX : <http://www.semanticweb.org/ontologies/2013/7/untitled-ontology-150#> SELECT ?x WHERE { ?x a :CountryPrefixNQ }";
		checkThereIsAtLeastOneResult(query);
	}

	
	/**
	 * Test us of different schema together with table prefix in column name
	 * @throws Exception
	 */
	@Test
	public void testMultiSchemaPrefix() throws Exception {
		String query = "PREFIX : <http://www.semanticweb.org/ontologies/2013/7/untitled-ontology-150#> SELECT ?x WHERE { ?x a :Pais }";
		checkThereIsAtLeastOneResult(query);
	}


	/**
	 * Test use of different schema and table prefix in column name, and column alias
	 * @throws Exception
	 */
	@Test
	public void testMultiSchemaAlias() throws Exception {
		String query = "PREFIX : <http://www.semanticweb.org/ontologies/2013/7/untitled-ontology-150#> SELECT ?x WHERE { ?x a :Land }";
		checkThereIsAtLeastOneResult(query);
	}

	/**
	 * Test use of different schema and table prefix in column name, and column alias, and quote in table prefix
	 * @throws Exception
	 */
	@Test
	public void testMultiSchemaAliasQuote() throws Exception {
		String query = "PREFIX : <http://www.semanticweb.org/ontologies/2013/7/untitled-ontology-150#> SELECT ?x WHERE { ?x a :LandQuote }";
		checkThereIsAtLeastOneResult(query);
	}
	
	/**
	 * Test use of different schema and table prefix in where clause
	 * @throws Exception
	 */
	@Test
	public void testMultiSchemaWhere() throws Exception {
		String query = "PREFIX : <http://www.semanticweb.org/ontologies/2013/7/untitled-ontology-150#> SELECT ?x WHERE { ?x a :CountryEgypt }";
		checkThereIsAtLeastOneResult(query);
	}
		
}
