package org.obda.owlrefplatform.core.abox;


import java.util.HashMap;
import java.util.Map;

/**
 * Reachability DAG
 */
public class TDAG {
    private final Map<String, DAGNode> dag_nodes = new HashMap<String, DAGNode>();

    public TDAG(DAG dag) {

        for (DAGNode node : dag.getClassIndex().values()) {
            String uri;
            DAGNode tnode;
            if (node.getUri().startsWith(DAG.owl_inverse_exists_data)) {
                String er_uri = DAG.owl_exists_data + node.getUri().substring(DAG.owl_inverse_exists_data.length());

                DAGNode er = dag_nodes.get(er_uri);
                if (er == null) {
                    // Create an additional ER node
                    er = new DAGNode(er_uri);
                    dag_nodes.put(er_uri, er);
                    // Make ER a parent of  current node ER-
                    er.getChildren().add(node);
                    node.getParents().add(er);
                }
                if (dag_nodes.get(node.getUri()) == null) {
                    dag_nodes.put(node.getUri(), node);
                }
                // Children will we added to ER-
                tnode = node;

            } else if (node.getUri().startsWith(DAG.owl_inverse_exists_obj)) {
                String er_uri = DAG.owl_exists_obj + node.getUri().substring(DAG.owl_inverse_exists_obj.length());
                DAGNode er = dag_nodes.get(er_uri);
                if (er == null) {
                    // Create an additional ER node
                    er = new DAGNode(er_uri);
                    dag_nodes.put(er_uri, er);
                    // Make ER a parent of  current node ER-
                    er.getChildren().add(node);
                    node.getParents().add(er);
                }
                if (dag_nodes.get(node.getUri()) == null) {
                    dag_nodes.put(node.getUri(), node);
                }
                // Children will we added to ER-
                tnode = node;
            } else if (node.getUri().startsWith(DAG.owl_exists_obj)) {
                String erm_uri = DAG.owl_inverse_exists_obj + node.getUri().substring(DAG.owl_exists_obj.length());
                DAGNode erm = dag_nodes.get(erm_uri);
                if (erm == null) {
                    // Create and additional ER- node
                    erm = new DAGNode(erm_uri);
                    dag_nodes.put(erm_uri, erm);

                    node.getParents().add(erm);
                    erm.getChildren().add(node);
                }
                if (dag_nodes.get(node.getUri()) == null) {
                    dag_nodes.put(node.getUri(), node);
                }
                // Children will ber added to ER-
                tnode = erm;
            } else if (node.getUri().startsWith(DAG.owl_exists_data)) {
                String erm_uri = DAG.owl_inverse_exists_data + node.getUri().substring(DAG.owl_exists_data.length());
                DAGNode erm = dag_nodes.get(erm_uri);
                if (erm == null) {
                    // Create and additional ER- node
                    erm = new DAGNode(erm_uri);
                    dag_nodes.put(erm_uri, erm);

                    node.getParents().add(erm);
                    erm.getChildren().add(node);
                }
                if (dag_nodes.get(node.getUri()) == null) {
                    dag_nodes.put(node.getUri(), node);
                }
                // Children will ber added to ER-
                tnode = erm;

            } else {
                DAGNode n = dag_nodes.get(node.getUri());
                if (n == null) {
                    n = new DAGNode(node.getUri());
                    dag_nodes.put(node.getUri(), n);
                }
                tnode = n;
            }

            for (DAGNode child_node : node.getChildren()) {
                String child_uri = child_node.getUri();

                DAGNode tchild_node = dag_nodes.get(child_uri);
                if (tchild_node == null) {
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
