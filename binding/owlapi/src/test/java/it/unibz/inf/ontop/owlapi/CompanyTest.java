package it.unibz.inf.ontop.owlapi;

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

import com.google.common.collect.ImmutableList;
import org.junit.*;

/***
 * Check how the optional filter is converted in left join
 * We do not support this kind of SPARQL query because it is not a well designed graph pattern
 *
 * We are going to create an H2 DB, the .sql file is fixed. We will map directly
 * there and then query on top.
 */
public class CompanyTest extends AbstractOWLAPITest {

	@BeforeClass
	public static void setUp() throws Exception {
		initOBDA("/optional/company-h2.sql",
				"/optional/company.obda",
				"/optional/company.owl");
	}

	@AfterClass
	public static void tearDown() throws Exception {
		release();
	}

	@Test
	public void runOptionalTest() throws Exception {

		String query = "PREFIX : <http://it.unibz.krdb/obda/test/company#> " +
				"SELECT * WHERE { " +
				"  :A a :Company . " +
				"  OPTIONAL { ?x :companyName :A . ?x :depName \"HR\" .  " +
				"    OPTIONAL { ?z :depId ?x }}}";

		checkReturnedValues(query, "z", ImmutableList.of(
				"<http://it.unibz.krdb/obda/test/company#mark>"));

		//assertEquals("<http://it.unibz.krdb/obda/test/company#1>", ind3.toString());
	}

	@Test
	public void runOptionalFilterTest() throws Exception {

		String query = "PREFIX : <http://it.unibz.krdb/obda/test/company#> " +
				"SELECT ?y?z WHERE { " +
				"  ?c a :Company . " +
				"  FILTER (?c=:A) " +
				"  OPTIONAL { ?x :companyName ?c .  ?x :depName ?y . " +
				"    FILTER (?y = \"HR\") " +
				"    OPTIONAL { ?z :depId ?x }}}";

		checkReturnedValues(query, "z", ImmutableList.of(
				"<http://it.unibz.krdb/obda/test/company#mark>"));

//			assertEquals("HR", ind1.getLiteral());
	}

	@Ignore
	@Test
	public void runSPOWithFilterTest() throws Exception {
		String query = "PREFIX : <http://it.unibz.krdb/obda/test/company#> " +
				" SELECT * WHERE"
				+ "{ ?c a :Department . ?c ?p ?o .}";

		checkReturnedValues(query, "c", ImmutableList.of());
	}
}
