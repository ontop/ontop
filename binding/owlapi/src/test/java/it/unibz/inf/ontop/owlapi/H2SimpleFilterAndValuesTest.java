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
 * Test use of  VALUES ?sx { <http://www.safepec.org#Ship>}
 * and  use of FILTER (?sx = <http://www.safepec.org#Ship> )
 */
public class H2SimpleFilterAndValuesTest extends AbstractOWLAPITest {

	@BeforeClass
	public static void setUp() throws Exception {
		initOBDA("/filter/h2-datatypes.sql",
				"/filter/filter-h2.obda",
				"/filter/datatypes.owl");
	}

	@AfterClass
	public static void tearDown() throws Exception {
		release();
	}

	@Test
	public void testFilterClass() throws Exception {
		String query = "PREFIX : <http://ontop.inf.unibz.it/test/datatypes#> " +
				"SELECT ?x ?y\n" +
				"WHERE {\n" +
				"   ?z a ?x; :hasDate ?y\n" +
				"   FILTER ( ?x = :Row ) .\n" +
				"}";

		checkReturnedValues(query, "y", ImmutableList.of(
				"\"2013-03-18\"^^xsd:date"));
	}

	@Test
	public void testValuesClass() throws Exception {
		String query =  "PREFIX : <http://ontop.inf.unibz.it/test/datatypes#> " +
				"SELECT ?x ?y\n" +
				"WHERE {\n" +
				"   ?z a ?x; :hasDate ?y\n" +
				"   VALUES ?x { :Row } .\n" +
				"}";

		checkReturnedValues(query, "y", ImmutableList.of(
					"\"2013-03-18\"^^xsd:date"));
	}

	@Test
	public void testFilterProperty() throws Exception {
		String query =  "PREFIX : <http://ontop.inf.unibz.it/test/datatypes#> " +
				"SELECT ?x ?y\n" +
				"WHERE {\n" +
				"   ?z a :Row; ?x ?y\n" +
				"   FILTER ( ?x = :hasDate ) .\n" +
				"}";
		checkReturnedValues(query, "y", ImmutableList.of(
				"\"2013-03-18\"^^xsd:date"));
	}

	@Test
	public void testValuesProperty() throws Exception {
		String query =  "PREFIX : <http://ontop.inf.unibz.it/test/datatypes#> " +
				"SELECT ?x ?y\n" +
				"WHERE {\n" +
				"   ?z a :Row; ?x ?y\n" +
				"   VALUES ?x { :hasDate } .\n" +
				"}";

		checkReturnedValues(query, "y", ImmutableList.of(
				"\"2013-03-18\"^^xsd:date"));
	}

	@Test
	public void testValuesProperty2() throws Exception {
		String query = "PREFIX : <http://ontop.inf.unibz.it/test/datatypes#> " +
				"SELECT * WHERE"
				+ "{ ?x a  :Row .  ?x ?v ?y . VALUES ?v { :hasSmallInt } }";

		checkReturnedValues(query, "y", ImmutableList.of(
				"\"1\"^^xsd:integer"));
	}
}

