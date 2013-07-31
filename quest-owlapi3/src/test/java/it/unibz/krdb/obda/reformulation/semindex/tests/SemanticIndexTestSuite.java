/*
 * Copyright (C) 2009-2013, Free University of Bozen Bolzano
 * This source code is available under the terms of the Affero General Public
 * License v3.
 * 
 * Please see LICENSE.txt for full license terms, including the availability of
 * proprietary exceptions.
 */
package it.unibz.krdb.obda.reformulation.semindex.tests;

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
