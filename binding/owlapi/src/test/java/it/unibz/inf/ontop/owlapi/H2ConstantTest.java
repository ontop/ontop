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
 * Tests constants
 */
public class H2ConstantTest extends AbstractOWLAPITest {

	@BeforeClass
	public static void setUp() throws Exception {
		initOBDA("/constant/constantsDatabase-h2.sql",
				"/constant/mappingConstants.obda",
				"/constant/mappingConstants.owl");
	}

	@AfterClass
	public static void tearDown() throws Exception {
		release();
	}
	

	@Test
	public void testConstantDouble() throws Exception {
		String query =  "PREFIX : <http://www.semanticweb.org/smallDatabase#> " +
				"SELECT ?x ?y\n" +
                "WHERE {\n" +
                "   ?x a :Company; :hasNetworth ?y\n" +
                "}";

		checkReturnedValues(query, "y", ImmutableList.of(
				"\"1234.5678\"^^xsd:double",
				"\"1234.5678\"^^xsd:double"));
	}

	@Test
	public void testConstantInteger() throws Exception {
		String query =  "PREFIX : <http://www.semanticweb.org/smallDatabase#> " +
				"SELECT ?x ?y\n" +
				"WHERE {\n" +
				"   ?x a :Address; :hasNumber ?y\n" +
				"}";

		checkReturnedValues(query, "y", ImmutableList.of(
				"\"35\"^^xsd:integer",
				"\"35\"^^xsd:integer",
				"\"35\"^^xsd:integer",
				"\"35\"^^xsd:integer",
				"\"35\"^^xsd:integer",
				"\"35\"^^xsd:integer",
				"\"35\"^^xsd:integer"));
	}

	@Test
	public void testConstantBoolean() throws Exception {
		String query =  "PREFIX : <http://www.semanticweb.org/smallDatabase#> " +
				"SELECT ?x ?y\n" +
				"WHERE {\n" +
				"   ?x a :Company; :hasSupplier ?y\n" +
				"}";

		checkReturnedValues(query, "y", ImmutableList.of(
				"\"true\"^^xsd:boolean",
				"\"true\"^^xsd:boolean"));
	}

	@Test
	public void testConstantDecimal() throws Exception {
		String query =  "PREFIX : <http://www.semanticweb.org/smallDatabase#> " +
				"SELECT ?x ?y\n" +
				"WHERE {\n" +
				"   ?x a :Company; :hasMarketShares ?y\n" +
				"}";

		checkReturnedValues(query, "y", ImmutableList.of(
				"\"1.000433564392849540\"^^xsd:decimal"));
	}
}

