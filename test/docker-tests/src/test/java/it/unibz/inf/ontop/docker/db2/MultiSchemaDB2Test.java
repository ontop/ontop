package it.unibz.inf.ontop.docker.db2;

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
 */
public class MultiSchemaDB2Test extends AbstractVirtualModeTest {

	// TODO We need to extend this test to import the contents of the mappings
	// into OWL and repeat everything taking form OWL


	static final String owlfile = "/db2/schema/multischemadb2.owl";
	static final String obdafile = "/db2/schema/multischemadb2.obda";
	static final String propertiesfile = "/db2/db2-stock.properties";

	public MultiSchemaDB2Test() {
		super(owlfile, obdafile, propertiesfile);
	}

	/**
	 * Test use of two aliases to same table
	 * @throws Exception
	 */
	@Test
	public void testOneSchema() throws Exception {
		String query = "PREFIX : <http://www.semanticweb.org/ontologies/2013/7/untitled-ontology-150#> SELECT ?x WHERE {?x a :Address}";
		checkThereIsAtLeastOneResult(query);
	}

	@Test
	public void testTableOneSchema() throws Exception {
		String query = "PREFIX : <http://www.semanticweb.org/ontologies/2013/7/untitled-ontology-150#> SELECT ?x WHERE {?x a :Broker}";
		checkThereIsAtLeastOneResult(query);
	}

	@Test
	public void testAliasOneSchema() throws Exception {
		String query = "PREFIX : <http://www.semanticweb.org/ontologies/2013/7/untitled-ontology-150#> SELECT ?x WHERE {?x a :Worker}";
		checkThereIsAtLeastOneResult(query);
	}

	@Test
	public void testSchemaWhere() throws Exception {
		String query = "PREFIX : <http://www.semanticweb.org/ontologies/2013/7/untitled-ontology-150#> SELECT ?x ?r WHERE { ?x :isBroker ?r }";
		checkThereIsAtLeastOneResult(query);
	}

	@Test
	public void testMultischema() throws Exception {
		String query = "PREFIX : <http://www.semanticweb.org/ontologies/2013/7/untitled-ontology-150#> SELECT ?x WHERE { ?x :hasFile ?r }";
		checkThereIsAtLeastOneResult(query);
	}
	
		
}
