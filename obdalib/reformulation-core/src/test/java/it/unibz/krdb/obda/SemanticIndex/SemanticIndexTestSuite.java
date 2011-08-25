package it.unibz.krdb.obda.SemanticIndex;

import it.unibz.krdb.obda.reformulation.tests.DAGChainTest;
import it.unibz.krdb.obda.reformulation.tests.SemanticReductionTest;
import it.unibz.krdb.obda.reformulation.tests.SigmaTest;
import junit.framework.TestSuite;


public class SemanticIndexTestSuite {

    public static TestSuite suite() {

        TestSuite suite = new TestSuite();

        suite.addTestSuite(DAGTest.class);
        suite.addTestSuite(SigmaTest.class);
        suite.addTestSuite(DAGChainTest.class);
        suite.addTestSuite(SemanticReductionTest.class);

        return suite;
    }


    public static void main(String[] args) {
        junit.textui.TestRunner.run(suite());
    }

}
