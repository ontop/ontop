package it.unibz.inf.ontop.owlapi;

/*
 * #%L
 * ontop-quest-owlapi
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
import org.junit.AfterClass;
import org.junit.BeforeClass;
import org.junit.Test;

/***
 * A simple test that check if the system is able to handle Mappings for
 * classes/roles and attributes even if there are no URI templates. i.e., the
 * database stores URI's directly.
 * 
 * We are going to create an H2 DB, the .sql file is fixed. We will map directly
 * there and then query on top.
 */
public class SimpleMappingVirtualABoxTest extends AbstractOWLAPITest {

	@BeforeClass
	public static void setUp() throws Exception {
		initOBDA("/test/simplemapping-create-h2.sql",
				"/test/simplemapping.obda",
				"/test/simplemapping.owl");
	}

	@AfterClass
	public static void tearDown() throws Exception {
		release();
	}

	@Test
	public void testViEqSig() throws Exception {
		String query = "PREFIX : <http://it.unibz.inf/obda/test/simple#> " +
				"SELECT * WHERE { ?x a :A; :P ?y; :U ?z; :P ?y; :U ?z; :P ?y ; :U ?z; :P ?y; :U ?z; :P ?y ; :U ?z; :P ?y; :U ?z; :P ?y ; :U ?z; :P ?y; :U ?z; :P ?y ; :U ?z; :P ?y; :U ?z; :P ?y ; :U ?z; :P ?y; :U ?z; :P ?y ; :U ?z; :P ?y; :U ?z; :P ?y ; :U ?z }";

		checkReturnedValues(query, "x", ImmutableList.of(
				"<http://example.org/uri1>"));

		checkReturnedValues(query, "y", ImmutableList.of(
				"<http://example.org/uri1>"));

		checkReturnedValues(query, "z", ImmutableList.of(
				"\"value1\"^^xsd:string"));
	}
}
