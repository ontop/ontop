package it.unibz.krdb.obda.reformulation.semindex.tests;

/*
 * #%L
 * ontop-quest-owlapi3
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

//import it.unibz.krdb.obda.reformulation.tests.SigmaTest;
import junit.framework.TestSuite;


public class SemanticIndexTestSuite {

    public static TestSuite suite() {

        TestSuite suite = new TestSuite();

        suite.addTestSuite(DAGTest.class);
//        suite.addTestSuite(SigmaTest.class);
        suite.addTestSuite(DAGChainTest.class);
        suite.addTestSuite(SemanticReductionTest.class);

        return suite;
    }


    public static void main(String[] args) {
        junit.textui.TestRunner.run(suite());
    }

}
