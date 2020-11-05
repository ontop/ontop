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
 * A simple test that check if the system is able to handle Mappings for
 * classes/roles and attributes even if there are no URI templates. i.e., the
 * database stores URIs directly.
 * 
 * We are going to create an H2 DB, the .sql file is fixed. We will map directly
 * there and then query on top.
 */
public class ReverseURIH2Test extends AbstractOWLAPITest {

	@BeforeClass
	public static void setUp() throws Exception {
		initOBDA("/reverseuri/reverse-uri-test.sql",
				"/reverseuri/reverse-uri-test.obda",
				"/reverseuri/reverse-uri-test.owl");
	}

	@AfterClass
	public static void tearDown() throws Exception {
		release();
	}

	@Test
	public void testSingleColum2() throws Exception {
		String query = "PREFIX : <http://www.ontop.org/> " +
				"SELECT ?v WHERE {<http://www.ontop.org/test-Cote%20D%27ivore> a ?v}";
		checkReturnedValues(query, "v", ImmutableList.of(
				"<http://www.ontop.org/Test>"));
	}

	@Test
	public void testSingleColum() throws Exception {
		String query = "PREFIX : <http://www.ontop.org/> " +
				"SELECT ?v WHERE {<http://www.ontop.org/test-John%20Smith> a ?v}";
		checkReturnedValues(query, "v", ImmutableList.of
				("<http://www.ontop.org/Test>"));
	}

	@Test
	public void testTwoColum2() throws Exception {
		String query = "PREFIX : <http://www.ontop.org/> " +
				"SELECT ?v WHERE {<http://www.ontop.org/test-Cote%20D%27ivore-Cote%20D%27ivore> a ?v}";
		checkReturnedValues(query, "v", ImmutableList.of(
				"<http://www.ontop.org/Test>"));
	}

	@Test
	public void testTwoColum2Value() throws Exception {
		String query = "PREFIX : <http://www.ontop.org/> " +
				"SELECT ?v WHERE {<http://www.ontop.org/test-John%20Smith-John%20Smith%202> a ?v}";
		checkReturnedValues(query, "v", ImmutableList.of(
				"<http://www.ontop.org/Test>"));
	}

	@Test
	public void testTwoColum22Vaule() throws Exception {
		String query = "PREFIX : <http://www.ontop.org/> " +
				"SELECT ?v WHERE {<http://www.ontop.org/test-Cote%20D%27ivore-Cote%20D%27ivore%202> a ?v}";
		checkReturnedValues(query, "v", ImmutableList.of(
				"<http://www.ontop.org/Test>"));
	}

	@Test
	public void testTwoColum() throws Exception {
		String query = "PREFIX : <http://www.ontop.org/> " +
				"SELECT ?v WHERE {<http://www.ontop.org/test-John%20Smith-John%20Smith> a ?v}";
		checkReturnedValues(query, "v", ImmutableList.of(
				"<http://www.ontop.org/Test>"));
	}

	@Test
	public void testThreeColum2() throws Exception {
		String query = "PREFIX : <http://www.ontop.org/> " +
				"SELECT ?v WHERE {<http://www.ontop.org/test-Cote%20D%27ivore-Cote%20D%27ivore-Cote%20D%27ivore> a ?v}";
		checkReturnedValues(query, "v", ImmutableList.of(
				"<http://www.ontop.org/Test>"));
	}

	@Test
	public void testThreeColum() throws Exception {
		String query = "PREFIX : <http://www.ontop.org/> " +
				"SELECT ?v WHERE {<http://www.ontop.org/test-John%20Smith-John%20Smith-John%20Smith> a ?v}";
		checkReturnedValues(query, "v", ImmutableList.of(
				"<http://www.ontop.org/Test>"));
	}

	@Test
	public void testThreeColum23Value() throws Exception {
		String query = "PREFIX : <http://www.ontop.org/> " +
				"SELECT ?v WHERE {<http://www.ontop.org/test-Cote%20D%27ivore-Cote%20D%27ivore%202-Cote%20D%27ivore%203> a ?v}";
		checkReturnedValues(query, "v", ImmutableList.of(
				"<http://www.ontop.org/Test>"));
	}

	@Test
	public void testThreeColum3Value() throws Exception {
		String query = "PREFIX : <http://www.ontop.org/> " +
				"SELECT ?v WHERE {<http://www.ontop.org/test-John%20Smith-John%20Smith%202-John%20Smith%203> a ?v}";
		checkReturnedValues(query, "v", ImmutableList.of(
				"<http://www.ontop.org/Test>"));
	}

}
