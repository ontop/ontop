package org.obda.owlrefplatform.core.abox;


import java.util.HashMap;
import java.util.Map;

/**
 * Reachability DAG
 * Replaces all -ER with ER (treats ER and -ER as equivalent nodes)
 */
public class TDAG {
    private final Map<String, DAGNode> dag_nodes = new HashMap<String, DAGNode>();

    public TDAG(DAG dag) {

        for (DAGNode node : dag.getClassIndex().values()) {
            String uri;
            if (node.getUri().startsWith(DAG.owl_inverse_exists_data)) {
                uri = DAG.owl_exists_data +
                        node.getUri().substring(DAG.owl_inverse_exists_data.length());
            } else if (node.getUri().startsWith(DAG.owl_inverse_exists_obj)) {
                uri = DAG.owl_exists_obj +
                        node.getUri().substring(DAG.owl_inverse_exists_obj.length());
            } else {
                uri = node.getUri();
            }

            DAGNode tnode = dag_nodes.get(uri);
            if (tnode == null) {
                // Use modified URI when constructing DAGNode, this is important
                tnode = new DAGNode(uri);
                dag_nodes.put(uri, tnode);
            }
            for (DAGNode child_node : node.getChildren()) {
                String child_uri;
                if (child_node.getUri().startsWith(DAG.owl_inverse_exists_data)) {
                    child_uri = DAG.owl_exists_data +
                            child_node.getUri().substring(DAG.owl_inverse_exists_data.length());
                } else if (child_node.getUri().startsWith(DAG.owl_inverse_exists_obj)) {
                    child_uri = DAG.owl_exists_obj +
                            child_node.getUri().substring(DAG.owl_inverse_exists_obj.length());
                } else {
                    child_uri = child_node.getUri();
                }
                DAGNode tchild_node = dag_nodes.get(child_uri);
                if (tchild_node == null) {
                    // Use modified URI when constructing DAGNode, this is important
                    tchild_node = new DAGNode(child_uri);
                    dag_nodes.put(child_uri, tchild_node);
                }
                // Construct edge
                tnode.getChildren().add(tchild_node);
                tchild_node.getParents().add(tnode);
            }
        }
        DAGOperations.buildDescendants(dag_nodes);
    }

    public Map<String, DAGNode> getTDAG() {
        return dag_nodes;
    }


}
