package it.unibz.krdb.obda.owlrefplatform.core.abox;


import java.util.HashMap;
import java.util.Map;


/**
 * Filter ER on the right side
 */
public class SDAG {

    private final Map<String, DAGNode> dag_nodes = new HashMap<String, DAGNode>();


    public SDAG(TDAG tdag) {


        for (DAGNode node : tdag.getTDAG().values()) {
            if (node.getUri().startsWith(DAG.owl_inverse_exists_data) ||
                    node.getUri().startsWith(DAG.owl_inverse_exists_obj) ||
                    node.getUri().startsWith(DAG.owl_exists_data) ||
                    node.getUri().startsWith(DAG.owl_exists_obj)) {
                continue;
            }
            DAGNode sigma_node = dag_nodes.get(node.getUri());
            if (sigma_node == null) {
                sigma_node = new DAGNode(node.getUri());
                dag_nodes.put(sigma_node.getUri(), sigma_node);
            }
            for (DAGNode child : node.getChildren()) {
                DAGNode sigma_child = dag_nodes.get(child.getUri());
                if (sigma_child == null) {
                    sigma_child = new DAGNode(child.getUri());
                    dag_nodes.put(sigma_child.getUri(), sigma_child);
                }

                sigma_child.getParents().add(sigma_node);
                sigma_node.getChildren().add(sigma_child);
            }
        }
        DAGOperations.buildDescendants(dag_nodes);
    }

    public Map<String, DAGNode> getTDAG() {
        return dag_nodes;
    }
}
