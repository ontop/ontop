package org.obda.SemanticIndex;

import junit.framework.TestCase;
import org.obda.owlrefplatform.core.abox.DAG;
import org.semanticweb.owl.model.OWLOntologyCreationException;

public class DAGTest extends TestCase {

    SemanticIndexHelper helper = new SemanticIndexHelper();

    public void test_1_0_0() throws OWLOntologyCreationException {
        String testname = "test_1_0_0";
        DAG res = helper.load_dag(testname);
        DAG exp = new DAG(helper.get_results(testname));
        assertEquals(exp, res);
    }

    public void test_1_0_1() throws OWLOntologyCreationException {
        String testname = "test_1_0_1";
        DAG res = helper.load_dag(testname);
        DAG exp = new DAG(helper.get_results(testname));
        assertEquals(exp, res);
    }

    public void test_1_1_0() throws OWLOntologyCreationException {
        String testname = "test_1_1_0";
        DAG res = helper.load_dag(testname);
        DAG exp = new DAG(helper.get_results(testname));
        assertEquals(exp, res);
    }

    public void test_1_2_0() throws OWLOntologyCreationException {
        String testname = "test_1_2_0";
        DAG res = helper.load_dag(testname);
        DAG exp = new DAG(helper.get_results(testname));
        assertEquals(exp, res);
    }

    public void test_1_3_0() throws OWLOntologyCreationException {
        String testname = "test_1_3_0";
        DAG res = helper.load_dag(testname);
        DAG exp = new DAG(helper.get_results(testname));
        assertEquals(exp, res);
    }

    public void test_1_4_0() throws OWLOntologyCreationException {
        String testname = "test_1_4_0";
        DAG res = helper.load_dag(testname);
        DAG exp = new DAG(helper.get_results(testname));
        assertEquals(exp, res);
    }
}
