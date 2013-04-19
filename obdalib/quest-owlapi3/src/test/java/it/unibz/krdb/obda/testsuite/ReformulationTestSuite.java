package it.unibz.krdb.obda.testsuite;
import it.unibz.krdb.obda.reformulation.tests.ReformulationTest;

import junit.framework.Test;
import junit.framework.TestSuite;

public class ReformulationTestSuite extends TestSuite {

	public static Test suite() {
		TestSuite suite = new TestSuite("Reformulation Test Suite");
		suite.addTestSuite(ReformulationTest.class);
		return suite;
	}
}
