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

public class TMappingConstantPositionsTest extends AbstractOWLAPITest {

	@BeforeClass
	public static void setUp() throws Exception {
		initOBDA("/test/tmapping-positions-create-h2.sql",
				"/test/tmapping-positions.obda",
				"/test/tmapping-positions.owl");
	}

	@After
	public void tearDown() throws Exception {
		release();
	}

	@Test
	public void testViEqSig() throws Exception {
		String query = "PREFIX : <http://it.unibz.inf/obda/test/simple#> " +
				"SELECT * WHERE { ?x a :A. }";

		checkReturnedValues(query, "x", ImmutableList.of(
				"<http://example.org/2>",
				"<http://example.org/3>",
				"<http://example.org/1>"));
	}
}
