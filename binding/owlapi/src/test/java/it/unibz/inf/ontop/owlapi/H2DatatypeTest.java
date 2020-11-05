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
 * Tests that h2 datatypes
 */
public class H2DatatypeTest extends AbstractOWLAPITest {

	@BeforeClass
	public static void setUp() throws Exception {
		initOBDA("/datatype/h2-datatypes.sql",
				"/datatype/datetime-h2.obda",
				"/datatype/datatypes.owl");
	}

	@AfterClass
	public static void tearDown() throws Exception {
		release();
	}

	/**
	 * Test use of date
	 */
	@Test
	public void testDate() throws Exception {
		String query =  "PREFIX : <http://ontop.inf.unibz.it/test/datatypes#> " +
				"SELECT ?s ?v\n" +
                "WHERE {\n" +
                "   ?s a :Row; :hasDate ?v\n" +
                "   FILTER ( ?v = \"2013-03-18\"^^xsd:date ) .\n" +
                "}";
		checkReturnedValues(query, "v", ImmutableList.of(
				"\"2013-03-18\"^^xsd:date"));
	}

	@Test
	public void testDate2() throws Exception {
        String query =  "PREFIX : <http://ontop.inf.unibz.it/test/datatypes#> " +
				"SELECT ?v\n" +
                "WHERE {\n" +
                "   ?v a :Row; :hasDate \"2013-03-18\"^^xsd:date\n" +
                "}";
		checkReturnedValues(query, "v", ImmutableList.of(
				"<http://ontop.inf.unibz.it/test/datatypes#datetime-1>"));
    }
}

