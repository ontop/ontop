package it.unibz.inf.ontop.testsuite;

/*
 * #%L
 * ontop-sparql-compliance
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

import it.unibz.inf.ontop.test.sparql.MemorySPARQLOntopQueryTest;
import it.unibz.inf.ontop.test.sparql11.MemorySPARQL11QueryTest;
import junit.framework.Test;
import junit.framework.TestSuite;

public class TestOntopSparqlSuite extends TestSuite {

	private static boolean ignoreFailures = true;
	public static Test suite() throws Exception {
		TestSuite suite = new TestSuite("SPARQL Compliance Tests for Ontop");
		suite.addTest(MemorySPARQLOntopQueryTest.suite(ignoreFailures));
		suite.addTest(MemorySPARQL11QueryTest.suite(ignoreFailures));
		return suite;
	}
}
