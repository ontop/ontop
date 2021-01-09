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
 * Tests that TMapping does not return error in case of symmetric properties.
 * Use to check that no concurrency error appears. 
 */
public class TMappingConcurrencyErrorFixTest extends AbstractOWLAPITest {

	@BeforeClass
	public static void setUp() throws Exception {
		initOBDA("/test/tmapping/create-tables.sql",
				"/test/tmapping/exampleTMapping.obda",
				"/test/tmapping/exampleTMapping.owl");
	}

	@AfterClass
	public static void tearDown() throws Exception{
		release();
	}

	@Test
	public void test() throws Exception {
		String query = "PREFIX  : <http://www.semanticweb.org/sarah/ontologies/2014/4/untitled-ontology-73#> " +
				"SELECT ?y WHERE { ?y a :Man }";

		checkReturnedValues(query, "y", ImmutableList.of(
				"<http://www.semanticweb.org/sarah/ontologies/2014/4/untitled-ontology-73#111>",
				"<http://www.semanticweb.org/sarah/ontologies/2014/4/untitled-ontology-73#112>"));
	}
}
