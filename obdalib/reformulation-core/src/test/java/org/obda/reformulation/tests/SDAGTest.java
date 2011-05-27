package org.obda.reformulation.tests;


import junit.framework.TestCase;
import org.obda.owlrefplatform.core.abox.DAG;
import org.obda.owlrefplatform.core.abox.DAGNode;
import org.obda.owlrefplatform.core.abox.SDAG;
import org.obda.owlrefplatform.core.abox.TDAG;

import java.util.ArrayList;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;

public class SDAGTest extends TestCase {

    public void test_singe_isa() {
        DAGNode a = new DAGNode("a");
        DAGNode b = new DAGNode("b");
        b.getParents().add(a);
        a.getChildren().add(b);

        List<DAGNode> ll = new ArrayList<DAGNode>(3);
        ll.add(a);
        ll.add(b);
        DAG dag = new DAG(ll, new ArrayList<DAGNode>(), new ArrayList<DAGNode>());
        TDAG tdag = new TDAG(dag);
        SDAG sdag = new SDAG(tdag);

        Map<String, DAGNode> res = sdag.getTDAG();

        assertTrue(res.get("a").descendans.contains(res.get("b")));
        assertEquals(res.get("a").descendans.size(), 1);

        assertEquals(res.get("b").descendans.size(), 0);

    }

    public void test_exists_simple() {
        DAGNode a = new DAGNode("a");
        DAGNode er = new DAGNode(DAG.owl_exists_obj + "r");
        DAGNode ier = new DAGNode(DAG.owl_inverse_exists_obj + "r");
        DAGNode c = new DAGNode("c");

        a.getChildren().add(er);
        er.getParents().add(a);

        ier.getChildren().add(c);
        c.getParents().add(ier);

        List<DAGNode> ll = new LinkedList<DAGNode>();
        ll.add(a);
        ll.add(er);
        ll.add(ier);
        ll.add(c);

        DAG dag = new DAG(ll, new LinkedList<DAGNode>(), new LinkedList<DAGNode>());
        TDAG tdag = new TDAG(dag);
        SDAG sdag = new SDAG(tdag);

        Map<String, DAGNode> res = sdag.getTDAG();

        assertTrue(res.get("a").descendans.contains(res.get(DAG.owl_exists_obj + "r")));
        assertTrue(res.get("a").descendans.contains(res.get(DAG.owl_inverse_exists_obj + "r")));
        assertEquals(res.get("a").descendans.size(), 2);

        assertEquals(res.get(DAG.owl_exists_obj + "r").descendans.size(), 0);

        assertEquals(res.get(DAG.owl_inverse_exists_obj + "r").descendans.size(), 0);

        assertEquals(res.get("c").descendans.size(), 0);
    }

    public void test_exists_complex() {
        DAGNode a = new DAGNode("a");
        DAGNode er = new DAGNode(DAG.owl_exists_obj + "r");
        DAGNode ier = new DAGNode(DAG.owl_inverse_exists_obj + "r");
        DAGNode c = new DAGNode("c");
        DAGNode b = new DAGNode("b");
        DAGNode d = new DAGNode("d");

        a.getChildren().add(er);
        er.getParents().add(a);

        ier.getChildren().add(c);
        c.getParents().add(ier);

        er.getChildren().add(b);
        b.getParents().add(er);

        ier.getParents().add(d);
        d.getChildren().add(ier);

        List<DAGNode> ll = new LinkedList<DAGNode>();
        ll.add(a);
        ll.add(er);
        ll.add(ier);
        ll.add(c);
        ll.add(b);
        ll.add(d);

        DAG dag = new DAG(ll, new LinkedList<DAGNode>(), new LinkedList<DAGNode>());
        TDAG tdag = new TDAG(dag);
        SDAG sdag = new SDAG(tdag);
        Map<String, DAGNode> res = sdag.getTDAG();

        assertTrue(res.get("a").descendans.contains(res.get(DAG.owl_exists_obj + "r")));
        assertTrue(res.get("a").descendans.contains(res.get(DAG.owl_inverse_exists_obj + "r")));
        assertEquals(res.get("a").descendans.size(), 2);

        assertTrue(res.get("d").descendans.contains(res.get(DAG.owl_exists_obj + "r")));
        assertTrue(res.get("d").descendans.contains(res.get(DAG.owl_inverse_exists_obj + "r")));
        assertEquals(res.get("d").descendans.size(), 2);

        assertEquals(res.get(DAG.owl_exists_obj + "r").descendans.size(), 0);

        assertEquals(res.get(DAG.owl_inverse_exists_obj + "r").descendans.size(), 0);

        assertEquals(res.get("b").descendans.size(), 0);

        assertEquals(res.get("c").descendans.size(), 0);

    }
}
