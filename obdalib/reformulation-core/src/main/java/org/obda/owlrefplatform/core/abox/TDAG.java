package org.obda.owlrefplatform.core.abox;


import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;

import static org.obda.owlrefplatform.core.abox.DAGOperations.buildDescendants;
import static org.obda.owlrefplatform.core.abox.DAGOperations.removeCycles;

/**
 * Reachability DAG
 */
public class TDAG {

    private final Logger log = LoggerFactory.getLogger(this.getClass());

    private final Map<String, DAGNode> dag_nodes = new HashMap<String, DAGNode>();
    private final Map<String, String> equi_map = new HashMap<String, String>();

    public TDAG(DAG dag) {

        for (DAGNode node : dag.getClassIndex().values()) {

            DAGNode tnode = dag_nodes.get(node.getUri());

            if (tnode == null) {
                tnode = new DAGNode(node.getUri());
                dag_nodes.put(node.getUri(), tnode);
            }

            String er = null;
            if (node.getUri().startsWith(DAG.owl_inverse_exists_obj)) {
                er = DAG.owl_exists_obj + node.getUri().substring(DAG.owl_inverse_exists_obj.length());
            } else if (node.getUri().startsWith(DAG.owl_inverse_exists_data)) {
                er = DAG.owl_exists_data + node.getUri().substring(DAG.owl_inverse_exists_data.length());
            } else if (node.getUri().startsWith(DAG.owl_exists_obj)) {
                er = DAG.owl_inverse_exists_obj + node.getUri().substring(DAG.owl_exists_obj.length());
            } else if (node.getUri().startsWith(DAG.owl_exists_data)) {
                er = DAG.owl_inverse_exists_data + node.getUri().substring(DAG.owl_exists_data.length());
            }
            DAGNode ern = null;
            if (er != null) {
                if ((ern = dag_nodes.get(er)) == null) {
                    ern = new DAGNode(er);
                    dag_nodes.put(er, ern);
                }
                if (tnode.getChildren() != ern.getChildren() ||
                        tnode.getParents() != ern.getParents()) {
                    // Both ER and ER- share the same children set object
                    assert (tnode.getChildren().size() == 0);
                    assert (tnode.getParents().size() == 0);
                    tnode.setChildren(ern.getChildren());
                    tnode.setParents(ern.getParents());
                }
            }

            DAGNode child_ern = null;
            for (final DAGNode child_node : node.getChildren()) {
                DAGNode tchild_node = dag_nodes.get(child_node.getUri());
                if (tchild_node == null) {
                    tchild_node = new DAGNode(child_node.getUri());
                    dag_nodes.put(tchild_node.getUri(), tchild_node);
                }
                String child_er = null;
                if (child_node.getUri().startsWith(DAG.owl_inverse_exists_obj)) {
                    child_er = DAG.owl_exists_obj + child_node.getUri().substring(DAG.owl_inverse_exists_obj.length());
                } else if (child_node.getUri().startsWith(DAG.owl_inverse_exists_data)) {
                    child_er = DAG.owl_exists_data + child_node.getUri().substring(DAG.owl_inverse_exists_data.length());
                } else if (child_node.getUri().startsWith(DAG.owl_exists_obj)) {
                    child_er = DAG.owl_inverse_exists_obj + child_node.getUri().substring(DAG.owl_exists_obj.length());
                } else if (child_node.getUri().startsWith(DAG.owl_exists_data)) {
                    child_er = DAG.owl_inverse_exists_data + child_node.getUri().substring(DAG.owl_exists_data.length());
                }

                if (child_er != null) {
                    if ((child_ern = dag_nodes.get(child_er)) == null) {
                        child_ern = new DAGNode(child_er);
                        dag_nodes.put(child_er, child_ern);
                    }
                    if (tchild_node.getChildren() != child_ern.getChildren() ||
                            tchild_node.getParents() != child_ern.getParents()) {
                        // Both ER and ER- share the same children set object
                        assert (tchild_node.getChildren().size() == 0);
                        assert (tchild_node.getParents().size() == 0);
                        tchild_node.setChildren(child_ern.getChildren());
                        tchild_node.setParents(child_ern.getParents());
                    }
                }

                // Construct edge
                tnode.getChildren().add(tchild_node);
                tchild_node.getParents().add(tnode);

                // Black Magic with ER and ER-
                if (ern != null) {
                    tchild_node.getParents().add(ern);
                }
                if (child_ern != null) {
                    tnode.getChildren().add(child_ern);
                }
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
        removeCycles(dag_nodes, equi_map);


        for (String uri : equi_map.keySet()) {
            DAGNode n = new DAGNode(uri);

            DAGNode equi_n = dag_nodes.get(equi_map.get(uri));


            equi_n.descendans = new HashSet<DAGNode>(equi_n.equivalents);
            equi_n.descendans.add(equi_n);

            n.setChildren(equi_n.getChildren());
            n.setParents(equi_n.getParents());
            n.descendans = equi_n.descendans;

            dag_nodes.put(uri, n);

        }
        buildDescendants(dag_nodes);
    }

    public Map<String, DAGNode> getTDAG() {
        return dag_nodes;
    }


}
