package it.unibz.inf.ontop.owlapi;

/*
 * #%L
 * ontop-reformulation-core
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

public class URITemplateMatcherTest extends AbstractOWLAPITest {

	@BeforeClass
	public static void setUp() throws Exception {
		initOBDA("/template/smallDatasetURIconstants.sql",
				"/template/oboe-coreURIconstants.obda",
				"/template/oboe-core.owl");
	}

	@AfterClass
	public static void tearDown() throws Exception {
		release();
	}

	@Test
	public void testURIConstant() throws Exception {
		String queryBind = "PREFIX : <http://www.ola.fr#>\n" +
				"  PREFIX oboe-core: <http://ecoinformatics.org/oboe/oboe.1.0/oboe-core.owl#>\n" +
				"  SELECT ?v {\n" +
				"     ?v a oboe-core:Measurement ; oboe-core:usesStandard \n" +
				"             <http://ecoinformatics.org/oboe/oboe.1.0/oboe-core.owl#Unit> . \n" +
				"  }";

		checkReturnedValues(queryBind, "v", ImmutableList.of(
				"<http://www.ola.fr#measurement/unit/name/1>",
				"<http://www.ola.fr#measurement/unit/name/2>"));
	}

	@Test
	public void testURIConstant2() throws Exception {
		String queryBind = "PREFIX : <http://www.ola.fr#>\n" +
				"  PREFIX oboe-core: <http://ecoinformatics.org/oboe/oboe.1.0/oboe-core.owl#>\n" +
				"  SELECT ?v {\n" +
				"     <http://www.ola.fr#measurement/unit/name/1> a oboe-core:Measurement ; oboe-core:hasValue ?v \n" +
				"  }";

		checkReturnedValues(queryBind, "v", ImmutableList.of(
				"<http://urlconstants.org/32>"));
	}
}
