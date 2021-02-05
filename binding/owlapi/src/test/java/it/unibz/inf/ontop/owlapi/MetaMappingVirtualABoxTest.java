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
import org.junit.*;


/**
 * This test is adapted from SimpleMappingVirtualABoxTest.
 *
 * A simple test that check if the system is able to handle Mappings for
 * classes/roles and attributes even if there are no URI templates. i.e., the
 * database stores URIs directly.
 * 
 * We are going to create an H2 DB, the .sql file is fixed. We will map directly
 * there and then query on top.
 */
public class MetaMappingVirtualABoxTest extends AbstractOWLAPITest {

	@BeforeClass
	public static void setUp() throws Exception {
		initOBDA("/test/metamapping-create-h2.sql",
				"/test/metamapping.obda",
				"/test/metamapping.owl");
	}

	@AfterClass
    public static void tearDown() throws Exception {
		release();
	}

    @Test
	public void test1() throws Exception {
		String query1 = "PREFIX : <http://it.unibz.inf/obda/test/simple#> " +
				"SELECT * WHERE { ?x a :A_1 }";

		checkReturnedValues(query1, "x", ImmutableList.of(
				"<http://example.org/uri1>"));
	}

	@Test
	public void test2() throws Exception {
		String query2 = "PREFIX : <http://it.unibz.inf/obda/test/simple#> " +
				"SELECT * WHERE { ?x :P_1 ?y }";

		checkReturnedValues(query2, "x", ImmutableList.of(
				"<http://example.org/uri1>"));

		checkReturnedValues(query2, "y", ImmutableList.of(
				"<http://example.org/A>"));
	}
}
