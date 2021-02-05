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
 * Test use of canonical iri in h2 simple database on wellbores
 */
public class H2NoDuplicatesCanonicalIRITest extends AbstractOWLAPITest {

	@BeforeClass
	public static void setUp() throws Exception {
		initOBDA("/sameAs/wellbores-same-as-can.sql",
				"/sameAs/wellbores-same-as-can.obda",
				"/sameAs/wellbores-same-as-can.owl");
	}

	@AfterClass
	public static void tearDown() throws Exception {
		release();
	}

	@Test
    public void testCanIRI1() throws Exception {
        String query =  "PREFIX : <http://ontop.inf.unibz.it/test/wellbore#> " +
				"SELECT DISTINCT ?x ?y WHERE {\n" +
				"?x a :Wellbore .\n" +
				"?x :inWell ?y .\n" +
				"}";

		checkNumberOfReturnedValues(query, 10);
    }

	@Test
	public void testCanIRI2() throws Exception {
		String query =  "PREFIX : <http://ontop.inf.unibz.it/test/wellbore#> " +
				"SELECT DISTINCT ?x WHERE{\n" +
				"?x a :Wellbore .\n" +
				"}";

		checkNumberOfReturnedValues(query, 5);
	}

	@Test
	public void testCanIRI3() throws Exception {
		String query =  "PREFIX : <http://ontop.inf.unibz.it/test/wellbore#> " +
				"SELECT DISTINCT ?x ?y ?z WHERE{\n" +
				"?x a :Wellbore .\n" +
				"?x :inWell ?y .\n" +
				"?x :name ?z .\n" +
				"}";

		checkNumberOfReturnedValues(query, 24);
	}

	@Test
	public void testCanIRI4() throws Exception {
		String query =  "PREFIX : <http://ontop.inf.unibz.it/test/wellbore#> " +
				"SELECT DISTINCT ?x ?y WHERE{\n" +
				"?x a :Well .\n" +
				"?x :hasWellbore ?y .\n" +
				"}";

		checkNumberOfReturnedValues(query, 4);
	}

	@Test
	public void testCanIRI5() throws Exception {
		String query =  "PREFIX : <http://ontop.inf.unibz.it/test/wellbore#> " +
				"SELECT * WHERE{\n" +
				"?x :inWell ?y .\n" +
				"}";

		checkNumberOfReturnedValues(query, 10);
	}

	@Test
	public void testCanIRI6() throws Exception {
		String query =  "PREFIX : <http://ontop.inf.unibz.it/test/wellbore#> " +
				"SELECT DISTINCT ?x WHERE{\n" +
				"?x a :Well .\n" +
				"}";

		checkNumberOfReturnedValues(query, 4);
	}

	@Test
	public void testCanIRI7() throws Exception {
		String query =  "PREFIX : <http://ontop.inf.unibz.it/test/wellbore#> " +
				"SELECT * WHERE{\n" +
				"?x :hasWellbore ?y .\n" +
				"}";

		checkNumberOfReturnedValues(query, 4);
	}
}

