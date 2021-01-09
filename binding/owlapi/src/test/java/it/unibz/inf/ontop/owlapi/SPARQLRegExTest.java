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

import org.junit.*;

/***
 * A simple test that check if the system is able to handle Mappings for
 * classes/roles and attributes even if there are no URI templates. i.e., the
 * database stores URI's directly.
 * 
 * We are going to create an H2 DB, the .sql file is fixed. We will map directly
 * there and then query on top.
 */
public class SPARQLRegExTest extends AbstractOWLAPITest {

	@BeforeClass
	public static void setUp() throws Exception {
		initOBDA("/regex/sparql-regex-test.sql",
				"/regex/sparql-regex-test.obda",
				"/regex/sparql-regex-test.owl");
	}

	@AfterClass
	public static void tearDown() throws Exception {
		release();
	}

	@Test
	public void testIssue72() throws Exception {
		String query = "SELECT DISTINCT * WHERE {\n" +
				"?s a <http://www.opendatasemanticuplift.org/vocab/class/Data_Centre_List_Five> .\n" +
				"?s ?p ?lit .\n" +
				"FILTER regex(?lit, \"^(?i)Zzdl2*(?-i)\") .\n" +
				"?s <http://www.w3.org/2000/01/rdf-schema#label> ?label .\n" +
				"}";
		checkNumberOfReturnedValues(query, 0);
	}

	@Test
	public void testSingleColum2() throws Exception {
		String query = "PREFIX : <http://www.ontop.org/> " +
				"SELECT ?x ?name WHERE {?x :name ?name . FILTER regex(?name, \"ABA\")}";
		checkNumberOfReturnedValues(query, 2);
	}

	@Test
	public void testRegexOnURIStr() throws Exception {
		String query = "PREFIX : <http://www.ontop.org/> S" +
				"ELECT ?x ?name WHERE {?x :name ?name . FILTER regex(str(?x), \"ABA\")}";
		checkNumberOfReturnedValues(query, 2);
	}

	// we should not return results when we execute a regex on a uri without the use of str function
	@Test
	public void testRegexOnURI() throws Exception {
		String query = "PREFIX : <http://www.ontop.org/> " +
				"SELECT ?x ?name WHERE {?x :name ?name . FILTER regex(?x, \"ABA\")}";
		checkNumberOfReturnedValues(query, 0);
	}
}
