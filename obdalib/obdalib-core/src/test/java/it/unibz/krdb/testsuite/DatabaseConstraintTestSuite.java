package it.unibz.krdb.testsuite;

import it.unibz.krdb.sql.Db2ConstraintTest;
import it.unibz.krdb.sql.MssqlConstraintTest;
import it.unibz.krdb.sql.MySqlConstraintTest;
import it.unibz.krdb.sql.OracleConstraintTest;
import it.unibz.krdb.sql.PgSqlConstraintTest;

import junit.framework.Test;
import junit.framework.TestSuite;

public class DatabaseConstraintTestSuite extends TestSuite {

	public static Test suite() {
		TestSuite suite = new TestSuite("Database Constraint Test Suite");
		suite.addTestSuite(MySqlConstraintTest.class);
		suite.addTestSuite(PgSqlConstraintTest.class);
		suite.addTestSuite(MssqlConstraintTest.class);
		suite.addTestSuite(OracleConstraintTest.class);
		suite.addTestSuite(Db2ConstraintTest.class);
		return suite;
	}
}
