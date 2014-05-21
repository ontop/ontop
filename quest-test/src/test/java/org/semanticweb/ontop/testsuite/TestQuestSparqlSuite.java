package org.semanticweb.ontop.testsuite;

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

import org.semanticweb.ontop.quest.sparql.QuestMemorySPARQLQueryTest;

import junit.framework.Test;
import junit.framework.TestSuite;

public class TestQuestSparqlSuite extends TestSuite {

	public static Test suite() throws Exception {
		TestSuite suite = new TestSuite("SPARQL Compliance Tests for Quest");
		suite.addTest(QuestMemorySPARQLQueryTest.suite());
		return suite;
	}
}
