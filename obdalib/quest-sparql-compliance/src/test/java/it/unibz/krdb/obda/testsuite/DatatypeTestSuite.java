package it.unibz.krdb.obda.testsuite;

import it.unibz.krdb.obda.quest.datatypes.MysqlDatatypeTest;
import it.unibz.krdb.obda.quest.datatypes.OracleDatatypeTest;
import it.unibz.krdb.obda.quest.datatypes.PgsqlDatatypeTest;
import junit.framework.Test;
import junit.framework.TestSuite;

public class DatatypeTestSuite extends TestSuite {
		
	public static Test suite() throws Exception {
		TestSuite suite = new TestSuite("Database Data-type Test Suite");
		suite.addTest(MysqlDatatypeTest.suite());
		suite.addTest(PgsqlDatatypeTest.suite());
//		suite.addTest(MssqlDatatypeTest.suite());
		suite.addTest(OracleDatatypeTest.suite());
//		suite.addTest(Db2DatatypeTest.suite());
		return suite;
	}
}
