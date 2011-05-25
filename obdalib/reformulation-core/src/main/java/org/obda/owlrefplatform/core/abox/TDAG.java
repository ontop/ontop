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

            DAGNode tnode = dag_nodes.get(node.getUri());
            if (tnode == null) {
                tnode = new DAGNode(node.getUri());
                dag_nodes.put(tnode.getUri(), tnode);

                // Link the ER- child set to ER child set
                String er = null;
                if (tnode.getUri().equals(DAG.owl_inverse_exists_obj)) {
                    er = DAG.owl_exists_obj + tnode.getUri().substring(DAG.owl_inverse_exists_obj.length());
                } else if (tnode.getUri().equals(DAG.owl_inverse_exists_data)) {
                    er = DAG.owl_exists_data + tnode.getUri().substring(DAG.owl_inverse_exists_data.length());
                } else if (tnode.getUri().equals(DAG.owl_exists_obj)) {
                    er = DAG.owl_inverse_exists_obj + tnode.getUri().substring(DAG.owl_exists_obj.length());
                } else if (tnode.getUri().equals(DAG.owl_exists_data)) {
                    er = DAG.owl_inverse_exists_data + tnode.getUri().substring(DAG.owl_exists_data.length());
                }

                if (er != null) {
                    DAGNode ern = dag_nodes.get(er);
                    if (ern == null) {
                        ern = new DAGNode(er);
                        dag_nodes.put(er, ern);
                        // Both ER and ER- share the same children set object
                        tnode.setChildren(ern.getChildren());
                    } else {
                        // ER- exists
                        tnode.setChildren(ern.getChildren());
                    }
                }
            }

            for (DAGNode child_node : node.getChildren()) {
                DAGNode tchild_node = dag_nodes.get(child_node.getUri());

                if (tchild_node == null) {
                    tchild_node = new DAGNode(child_node.getUri());
                    dag_nodes.put(tchild_node.getUri(), tchild_node);

                    // Link the ER- child set to ER child set
                    String er = null;
                    if (tchild_node.getUri().equals(DAG.owl_inverse_exists_obj)) {
                        er = DAG.owl_exists_obj + tchild_node.getUri().substring(DAG.owl_inverse_exists_obj.length());
                    } else if (tchild_node.getUri().equals(DAG.owl_inverse_exists_data)) {
                        er = DAG.owl_exists_data + tchild_node.getUri().substring(DAG.owl_inverse_exists_data.length());
                    } else if (tchild_node.getUri().equals(DAG.owl_exists_obj)) {
                        er = DAG.owl_inverse_exists_obj + tchild_node.getUri().substring(DAG.owl_exists_obj.length());
                    } else if (tchild_node.getUri().equals(DAG.owl_exists_data)) {
                        er = DAG.owl_inverse_exists_data + tchild_node.getUri().substring(DAG.owl_exists_data.length());
                    }

                    if (er != null) {
                        DAGNode ern = dag_nodes.get(er);
                        if (ern == null) {
                            ern = new DAGNode(er);
                            dag_nodes.put(er, ern);
                            // Both ER and ER- share the same children set object
                            tchild_node.setChildren(ern.getChildren());
                        } else {
                            // ER- exists
                            tchild_node.setChildren(ern.getChildren());
                        }
                    }
                }
                // Construct edge
                tnode.getChildren().add(tchild_node);
                tchild_node.getParents().add(tnode);
            }


        }

        for (DAGNode node : dag.getObjectPropertyIndex().values()) {
            DAGNode tnode = dag_nodes.get(node.getUri());
            if (tnode == null) {
                tnode = new DAGNode(node.getUri());
                dag_nodes.put(tnode.getUri(), tnode);
            }
            for (DAGNode child_node : node.getChildren()) {
                String child_uri = child_node.getUri();
                DAGNode tchild_node = dag_nodes.get(child_uri);
                if (tchild_node == null) {
                    tchild_node = new DAGNode(child_uri);
                    dag_nodes.put(child_uri, tchild_node);
                }
                tnode.getChildren().add(tchild_node);
                tchild_node.getParents().add(tnode);
            }
        }

        for (DAGNode node : dag.getDataPropertyIndex().values()) {
            DAGNode tnode = dag_nodes.get(node.getUri());
            if (tnode == null) {
                tnode = new DAGNode(node.getUri());
                dag_nodes.put(node.getUri(), tnode);
            }
            for (DAGNode child_node : node.getChildren()) {
                String child_uri = child_node.getUri();
                DAGNode tchild_node = dag_nodes.get(child_uri);
                if (tchild_node == null) {
                    tchild_node = new DAGNode(child_uri);
                    dag_nodes.put(child_uri, tchild_node);
                }
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
