/*
 * Copyright (C) 2009-2013, Free University of Bozen Bolzano
 * This source code is available under the terms of the Affero General Public
 * License v3.
 * 
 * Please see LICENSE.txt for full license terms, including the availability of
 * proprietary exceptions.
 */
package it.unibz.krdb.obda.testsuite;

import it.unibz.krdb.obda.quest.scenarios.Db2VirtualScenarioTest;
import it.unibz.krdb.obda.quest.scenarios.MssqlVirtualScenarioTest;
import it.unibz.krdb.obda.quest.scenarios.MysqlVirtualScenarioTest;
import it.unibz.krdb.obda.quest.scenarios.OracleVirtualScenarioTest;
import it.unibz.krdb.obda.quest.scenarios.PgsqlVirtualScenarioTest;

import junit.framework.Test;
import junit.framework.TestSuite;

public class TestVirtualScenarioSuite extends TestSuite {

	public static Test suite() throws Exception {
		TestSuite suite = new TestSuite("Scenario Tests for Quest in Virtual mode");
//		suite.addTest(MysqlVirtualScenarioTest.suite());
//		suite.addTest(PgsqlVirtualScenarioTest.suite());
//		suite.addTest(MssqlVirtualScenarioTest.suite());
//		suite.addTest(OracleVirtualScenarioTest.suite());
//		suite.addTest(Db2VirtualScenarioTest.suite());
		return suite;
	}
}
