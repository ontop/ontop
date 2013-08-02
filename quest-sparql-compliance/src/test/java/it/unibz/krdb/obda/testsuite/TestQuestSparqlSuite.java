/*
 * Copyright (C) 2009-2013, Free University of Bozen Bolzano
 * This source code is available under the terms of the Affero General Public
 * License v3.
 * 
 * Please see LICENSE.txt for full license terms, including the availability of
 * proprietary exceptions.
 */
package it.unibz.krdb.obda.testsuite;

import it.unibz.krdb.obda.quest.sparql.QuestMemorySPARQLQueryTest;
import junit.framework.Test;
import junit.framework.TestSuite;

public class TestQuestSparqlSuite extends TestSuite {

	public static Test suite() throws Exception {
		TestSuite suite = new TestSuite("SPARQL Compliance Tests for Quest");
		suite.addTest(QuestMemorySPARQLQueryTest.suite());
		return suite;
	}
}
