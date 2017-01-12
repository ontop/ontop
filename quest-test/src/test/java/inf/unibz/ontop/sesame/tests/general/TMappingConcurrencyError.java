package inf.unibz.ontop.sesame.tests.general;

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

import it.unibz.inf.ontop.quest.AbstractVirtualModeTest;


/***
 * Tests that TMapping does not return error in case of symmetric properties.
 * Use to check that no concurrency error appears. 
 */
public class TMappingConcurrencyError extends AbstractVirtualModeTest {

	static final String owlfile = "src/test/resources/exampleTMapping.owl";
	static final String obdafile = "src/test/resources/exampleTMapping.obda";

	protected TMappingConcurrencyError() {
		super(owlfile, obdafile);
	}



	public void tearDown() throws Exception{
		conn.close();
		reasoner.dispose();
	}

	/**
	 * Test use of quoted mixed case table name
	 * @throws Exception
	 */
	public void test() throws Exception {
		String query = "PREFIX  : <http://www.semanticweb.org/sarah/ontologies/2014/4/untitled-ontology-73#> SELECT ?x WHERE { ?x a :Man }";
		String val = runQueryAndReturnStringOfIndividualX(query);

	}




}
