package org.obda.SemanticIndex;

import junit.framework.TestCase;
import org.obda.owlrefplatform.core.abox.DAG;
import org.obda.owlrefplatform.core.abox.DAGNode;
import org.semanticweb.owl.model.OWLOntologyCreationException;

import java.util.List;

public class DAGTest extends TestCase {

    SemanticIndexHelper helper = new SemanticIndexHelper();

    public void test_1_0_0() throws OWLOntologyCreationException {
        String testname = "test_1_0_0";
        DAG res = helper.load_dag(testname);
        List<List<DAGNode>> exp_idx = helper.get_results(testname);

        DAG exp = new DAG(exp_idx.get(0), exp_idx.get(1), exp_idx.get(2));
        assertEquals(exp, res);
    }

    public void test_1_0_1() throws OWLOntologyCreationException {
        String testname = "test_1_0_1";
        DAG res = helper.load_dag(testname);
        List<List<DAGNode>> exp_idx = helper.get_results(testname);

        DAG exp = new DAG(exp_idx.get(0), exp_idx.get(1), exp_idx.get(2));
        assertEquals(exp, res);
    }

    public void test_1_1_0() throws OWLOntologyCreationException {
        String testname = "test_1_1_0";
        DAG res = helper.load_dag(testname);
        List<List<DAGNode>> exp_idx = helper.get_results(testname);

        DAG exp = new DAG(exp_idx.get(0), exp_idx.get(1), exp_idx.get(2));
        assertEquals(exp, res);
    }

    public void test_1_2_0() throws OWLOntologyCreationException {
        String testname = "test_1_2_0";
        DAG res = helper.load_dag(testname);
        List<List<DAGNode>> exp_idx = helper.get_results(testname);

        DAG exp = new DAG(exp_idx.get(0), exp_idx.get(1), exp_idx.get(2));
        assertEquals(exp, res);
    }

    public void test_1_3_0() throws OWLOntologyCreationException {
        String testname = "test_1_3_0";
        DAG res = helper.load_dag(testname);
        List<List<DAGNode>> exp_idx = helper.get_results(testname);

        DAG exp = new DAG(exp_idx.get(0), exp_idx.get(1), exp_idx.get(2));
        assertEquals(exp, res);
    }

    public void test_1_4_0() throws OWLOntologyCreationException {
        String testname = "test_1_4_0";
        DAG res = helper.load_dag(testname);
        List<List<DAGNode>> exp_idx = helper.get_results(testname);

        DAG exp = new DAG(exp_idx.get(0), exp_idx.get(1), exp_idx.get(2));
        assertEquals(exp, res);
    }

    public void test_1_5_0() throws OWLOntologyCreationException {
        String testname = "test_1_5_0";
        DAG res = helper.load_dag(testname);
        List<List<DAGNode>> exp_idx = helper.get_results(testname);

        DAG exp = new DAG(exp_idx.get(0), exp_idx.get(1), exp_idx.get(2));
        assertEquals(exp, res);
    }
}
