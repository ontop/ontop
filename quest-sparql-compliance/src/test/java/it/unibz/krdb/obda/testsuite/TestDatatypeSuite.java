/*
 * Copyright (C) 2009-2013, Free University of Bozen Bolzano
 * This source code is available under the terms of the Affero General Public
 * License v3.
 * 
 * Please see LICENSE.txt for full license terms, including the availability of
 * proprietary exceptions.
 */
package it.unibz.krdb.obda.testsuite;

import it.unibz.krdb.obda.quest.datatypes.MysqlDatatypeTest;
import it.unibz.krdb.obda.quest.datatypes.OracleDatatypeTest;
import it.unibz.krdb.obda.quest.datatypes.PgsqlDatatypeTest;
import junit.framework.Test;
import junit.framework.TestSuite;

public class TestDatatypeSuite extends TestSuite {
		
	public static Test suite() throws Exception {
		TestSuite suite = new TestSuite("Database Data-type Test Suite");
//		suite.addTest(MysqlDatatypeTest.suite());
//		suite.addTest(PgsqlDatatypeTest.suite());
//		suite.addTest(MssqlDatatypeTest.suite());
//		suite.addTest(OracleDatatypeTest.suite());
//		suite.addTest(Db2DatatypeTest.suite());
		return suite;
	}
}
