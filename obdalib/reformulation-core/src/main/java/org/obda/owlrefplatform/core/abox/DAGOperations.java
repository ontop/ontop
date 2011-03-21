package org.obda.owlrefplatform.core.abox;


import java.util.*;

/**
 * Implement Graph specific operations like get all ancestors and get all descendants
 *
 * @author Sergejs Pugac
 */

public class DAGOperations {

    /**
     * Calculate the ancestors for all nodes in the given DAG
     *
     * @param dagnodes a DAG
     * @return Map from uri to the Set of their ancestors
     */
    public static Map<String, Set<DAGNode>> buildAncestor(Map<String, DAGNode> dagnodes) {
        Map<String, Set<DAGNode>> ancestors = new HashMap<String, Set<DAGNode>>();
        LinkedList<DAGNode> stack = new LinkedList<DAGNode>();

        // Start with top nodes, that don't have parents
        for (DAGNode n : dagnodes.values()) {
            if (n.getParents().isEmpty()) {
                stack.add(n);
            }
            // Initialize all ancestors to an empty set
            ancestors.put(n.getUri(), new LinkedHashSet<DAGNode>());
        }
        while (!stack.isEmpty()) {
            DAGNode cur_el = stack.pop();
            for (DAGNode child_node : cur_el.getChildren()) {

                // add parent to ancestor list
                ancestors.get(child_node.getUri()).add(cur_el);

                // add parent parents to ancestor list
                for (DAGNode cur_node_ancestor : ancestors.get(cur_el.getUri())) {
                    ancestors.get(child_node.getUri()).add(cur_node_ancestor);
                }
                stack.add(child_node);
            }
        }
        return ancestors;
    }

    /**
     * Calculate the descendants for all nodes in the given DAG
     *
     * @param dagnodes a DAG
     * @return Map from uri to the Set of their descendants
     */
    public static Map<String, Set<DAGNode>> buildDescendants(Map<String, DAGNode> dagnodes) {
        Map<String, Set<DAGNode>> descendants = new HashMap<String, Set<DAGNode>>();
        LinkedList<DAGNode> stack = new LinkedList<DAGNode>();

        // Start with bottom nodes, that don't have children
        for (DAGNode n : dagnodes.values()) {
            if (n.getChildren().isEmpty()) {
                stack.add(n);
            }
            // Initialize all descendants to an empty set
            descendants.put(n.getUri(), new LinkedHashSet<DAGNode>());
        }
        while (!stack.isEmpty()) {
            DAGNode cur_el = stack.pop();
            for (DAGNode par_node : cur_el.getParents()) {

                // add child to descendants list
                descendants.get(par_node.getUri()).add(cur_el);

                // add child children to descendants list
                for (DAGNode cur_el_descendant : descendants.get(cur_el.getUri())) {
                    descendants.get(par_node.getUri()).add(cur_el_descendant);
                }
                stack.add(par_node);
            }
        }
        return descendants;
    }


}
