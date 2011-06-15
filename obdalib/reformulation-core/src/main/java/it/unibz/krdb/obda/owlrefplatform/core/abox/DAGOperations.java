package it.unibz.krdb.obda.owlrefplatform.core.abox;


import it.unibz.krdb.obda.owlrefplatform.core.ontology.Description;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.Map;
import java.util.Queue;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Implement Graph specific operations like get all ancestors and get all descendants
 *
 * @author Sergejs Pugac
 */

public class DAGOperations {
    private static final Logger log = LoggerFactory.getLogger(DAGOperations.class);


    /**
     * Calculate the descendants for all nodes in the given DAG
     *
     * @param dagnodes a DAG
     * @return Map from uri to the Set of their descendants
     */
    public static void buildDescendants(Map<Description, DAGNode> dagnodes) {
        Queue<DAGNode> stack = new LinkedList<DAGNode>();

        // Start with bottom nodes, that don't have children
        for (DAGNode n : dagnodes.values()) {
            if (n.getChildren().isEmpty()) {
                stack.add(n);
            }
        }
        if (stack.isEmpty() && !dagnodes.isEmpty()) {
            log.error("Can not build descendants for graph with cycles");
        }
        while (!stack.isEmpty()) {
            DAGNode cur_el = stack.remove();

            for (DAGNode eq_node : cur_el.equivalents) {
                cur_el.descendans.add(eq_node);
            }

            for (DAGNode par_node : cur_el.getParents()) {

                // add child to descendants list
                par_node.descendans.add(cur_el);

                // add child children to descendants list
                for (DAGNode cur_el_descendant : cur_el.descendans) {
                    par_node.descendans.add(cur_el_descendant);
                }
                stack.add(par_node);
            }
        }
    }

    /**
     * Adds two edges between child and parent. The first edge is a child
     * hasParent edge, the second edge is a parent hasChild edge. The method
     * guarantees no duplicate edges in any single node, e.g., no node will have
     * the edge A hasParent B two times.
     *
     * @param childnode
     * @param parentnode
     */
    private static void addParentEdge(DAGNode childnode, DAGNode parentnode) {

        if (childnode.equals(parentnode)) {
            return;
        }

        childnode.getParents().add(parentnode);
        parentnode.getChildren().add(childnode);
    }

    /**
     * Removes the hasParent and hasChild edges between the two nodes, if they
     * exist. The method also guarantees that if as a result of this operation
     * the Node's children or parents list becomes empty, then they will be
     * assigned NULL. This is done in order to gurantee that at any time, if a
     * Node has no children or parents, then the correspondent collections will
     * be NULL.
     *
     * @param childnode
     * @param parentnode
     */
    private static void removeParentEdge(DAGNode childnode, DAGNode parentnode) {
        childnode.getParents().remove(parentnode);
        parentnode.getChildren().remove(childnode);
    }


    /**
     * The methods setups the equivalent relations between n1 and n2. Note that
     * this doesn't guarantee that nodes that are equivalent to n2 and n1 are
     * also set. For that purpose see {@see addAllEquivalences}.
     *
     * @param node1
     * @param node2
     */
    public static void addEquivalence(DAGNode node1, DAGNode node2) {
        if (node1.equals(node2))
            return;

        node1.getEquivalents().remove(node2);
        node1.getEquivalents().add(node2);

        node2.getEquivalents().remove(node1);
        node2.getEquivalents().add(node1);
    }


    public static void computeTransitiveReduct(Map<Description, DAGNode> dagnodes) {
        buildDescendants(dagnodes);

        LinkedList<Edge> redundantEdges = new LinkedList<Edge>();
        for (DAGNode node : dagnodes.values()) {
            for (DAGNode child : node.getChildren()) {
                for (DAGNode child_desc : child.descendans) {
                    redundantEdges.add(new Edge(node, child_desc));
                }
            }
        }
        for (Edge edge : redundantEdges) {
            DAGNode from = edge.getLeft();
            DAGNode to = edge.getRight();

            from.getChildren().remove(to);
            to.getParents().remove(from);
        }
    }

    public static void removeCycles(Map<Description, DAGNode> dagnodes, Map<Description, Description> equi_mapp) {
        for (ArrayList<DAGNode> component : scc(dagnodes)) {

            DAGNode cyclehead = component.get(0);

            for (int i = 1; i < component.size(); i++) {
                DAGNode equivnode = component.get(i);

                for (DAGNode parent : new LinkedList<DAGNode>(equivnode.getParents())) {
                    removeParentEdge(equivnode, parent);
                    addParentEdge(cyclehead, parent);
                }

                for (DAGNode childchild : new LinkedList<DAGNode>(equivnode.getChildren())) {
                    removeParentEdge(childchild, equivnode);
                    addParentEdge(childchild, cyclehead);
                }

                dagnodes.remove(equivnode.getDescription());
                equi_mapp.put(equivnode.getDescription(), cyclehead.getDescription());
                cyclehead.equivalents.add(equivnode);
            }
        }
    }

    private static int index = 0;
    private static ArrayList<DAGNode> stack;
    private static ArrayList<ArrayList<DAGNode>> SCC;

    private static Map<DAGNode, Integer> t_idx;
    private static Map<DAGNode, Integer> t_low_idx;

    private static ArrayList<ArrayList<DAGNode>> scc(Map<Description, DAGNode> list) {
        stack = new ArrayList<DAGNode>();
        SCC = new ArrayList<ArrayList<DAGNode>>();
        t_idx = new HashMap<DAGNode, Integer>();
        t_low_idx = new HashMap<DAGNode, Integer>();
        for (DAGNode node : list.values()) {
            if (t_idx.get(node) == null) {
                strongconnect(node);
            }
        }
        return SCC;
    }

    private static void strongconnect(DAGNode v) {
        t_idx.put(v, index);
        t_low_idx.put(v, index);

        index++;
        stack.add(0, v);
        for (DAGNode child : v.getChildren()) {
            if (t_idx.get(child) == null) {
                strongconnect(child);
                t_low_idx.put(v, Math.min(t_low_idx.get(v), t_low_idx.get(child)));
            } else if (stack.contains(child)) {
                t_low_idx.put(v, Math.min(t_low_idx.get(v), t_idx.get(child)));
            }
        }
        if (t_low_idx.get(v).equals(t_idx.get(v))) {
            DAGNode n;
            ArrayList<DAGNode> component = new ArrayList<DAGNode>();
            do {
                n = stack.remove(0);
                component.add(n);
            } while (!n.equals(v));
            SCC.add(component);
        }
    }

}
