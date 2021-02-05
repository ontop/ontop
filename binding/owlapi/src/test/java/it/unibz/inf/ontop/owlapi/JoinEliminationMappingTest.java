package it.unibz.inf.ontop.owlapi;

/*
 * #%L
 * ontop-quest-owlapi3
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


public class JoinEliminationMappingTest extends AbstractOWLAPITest {

	@BeforeClass
	public static void setUp() throws Exception {
		initOBDA("/test/ontologies/scenarios/join-elimination-test.sql",
				"/test/ontologies/scenarios/join-elimination-test.obda",
				"/test/ontologies/scenarios/join-elimination-test.owl");
	}

	@AfterClass
	public static void tearDown() throws Exception {
		release();
	}

	@Test
	public void testViEqSig() throws Exception {
		String query = "PREFIX : <http://it.unibz.krdb/obda/ontologies/join-elimination-test.owl#> \n" +
						"SELECT ?x WHERE {?x :R ?y. ?y a :A}";

		checkReturnedValues(query, "x", ImmutableList.of());
	}

}
