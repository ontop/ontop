package org.obda.reformulation.tests;


import junit.framework.TestCase;
import org.obda.owlrefplatform.core.abox.DAG;
import org.obda.owlrefplatform.core.abox.DAGNode;
import org.obda.owlrefplatform.core.abox.TDAG;

import java.util.LinkedList;
import java.util.List;
import java.util.Map;

public class TDAGTest extends TestCase {

    public void test_1_0() {
        DAGNode a = new DAGNode("a");
        DAGNode er = new DAGNode(DAG.owl_exists_obj + "r");
        DAGNode ier = new DAGNode(DAG.owl_inverse_exists_obj + "r");
        DAGNode c = new DAGNode("c");

        a.getParents().add(er);
        er.getChildren().add(a);

        ier.getParents().add(c);
        c.getChildren().add(ier);

        List<DAGNode> ll = new LinkedList<DAGNode>();
        ll.add(a);
        ll.add(er);
        ll.add(ier);
        ll.add(c);

        DAG dag = new DAG(ll, new LinkedList<DAGNode>(), new LinkedList<DAGNode>());
        TDAG tdag = new TDAG(dag);
        Map<String, DAGNode> res = tdag.getTDAG();

        assertTrue(res.get("c").descendans.contains(res.get("a")));
    }
}
