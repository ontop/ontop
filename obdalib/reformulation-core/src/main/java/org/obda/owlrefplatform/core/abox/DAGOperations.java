package org.obda.owlrefplatform.core.abox;


import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.*;

/**
 * Implement Graph specific operations like get all ancestors and get all descendants
 *
 * @author Sergejs Pugac
 */

public class DAGOperations {
    private static final Logger log = LoggerFactory.getLogger(DAGOperations.class);

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

    /**
     * Adds two edges between child and parent. The first edge is a child
     * hasParent edge, the second edge is a parent hasChild edge. The method
     * guarantees no duplicate edges in any single node, e.g., no node will have
     * the edge A hasParent B two times.
     *
     * @param childnode
     * @param parentnode
     */
    public static void addParentEdge(DAGNode childnode, DAGNode parentnode) {

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
    public static void removeParentEdge(DAGNode childnode, DAGNode parentnode) {
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

    /**
     * This method will make sure that any nodes in the list, as well as the
     * ones equivalent to those in the list are all properly declared as
     * equivalent.
     * <p/>
     * For example, if A = B, and C = D, then addAllEquivalences(A,C) will
     * result in A = C, A = D, A = B, C = A, C = B, C = D, B = A, B = C, B = D,
     * D = A, D =B , D = C.
     *
     * @param equivalentnodes
     */
    @SuppressWarnings("unchecked")
    public static void addAllEquivalences(List<DAGNode> equivalentnodes) {
        for (DAGNode n1 : equivalentnodes) {
            for (DAGNode n2 : equivalentnodes) {
                addEquivalence(n1, n2);
                for (DAGNode nequivalent : (LinkedList<DAGNode>) n2.getEquivalents().clone()) {
                    addEquivalence(n1, nequivalent);
                }
                for (DAGNode nequivalent : (LinkedList<DAGNode>) n1.getEquivalents().clone()) {
                    addEquivalence(n2, nequivalent);
                }
            }
        }
    }

    /**
     * This method removes all the cycles bellow the {@see currentnode}. All
     * cycles bellow (through the child relation) this node will be 'condensed'
     * into a single node (the first node of the cycle). All the nodes
     * participating in the cycle will be declared as equivalent.
     * <p/>
     * Moreover, only the first node in each cycle (the cycle's root) will
     * contain any parent/child edge. That is, all the parent/child edges from
     * any other node in the cycle will be moved to the cycle's root and they
     * will be removed from the nodes that originally held the edges.
     * <p/>
     * The method guarantees that there will be no trace of equivalent edges
     * except for one of them, that will have all the information contained in
     * all the equivalent nodes.
     *
     * @param currentnode The initial node to examine.
     * @param visited     A linked list that will hold all the nodes that have been
     *                    visited during the cycle removal (including the cyclic nodes)
     */
    public static void removeCycles(DAGNode currentnode, LinkedList<DAGNode> visited) {
        removeCycles(currentnode, new LinkedList<DAGNode>(), visited,
                new LinkedList<DAGNode>());
    }

    /**
     * This method removes all the cycles bellow the {@see currentnode}. All
     * cycles bellow (through the child relation) this node will be 'condensed'
     * into a single node (the first node of the cycle). All the nodes
     * participating in the cycle will be declared as equivalent.
     * <p/>
     * Moreover, only the first node in each cycle (the cycle's root) will
     * contain any parent/child edge. That is, all the parent/child edges from
     * any other node in the cycle will be moved to the cycle's root and they
     * will be removed from the nodes that originally held the edges.
     * <p/>
     * The method guarantees that there will be no trace of equivalent edges
     * except for one of them, that will have all the information contained in
     * all the equivalent nodes.
     *
     * @param currentnode The initial node to examine.
     * @param stack       A linked list that will act as a Stack for the recursive all
     *                    of this method. Initially, it should be an empty list.
     * @param visited     A linked list that will hold all the nodes that have been
     *                    visited during the cycle removal (including the cyclic nodes)
     * @param cycle       A linked list that will hold the first cycle found during the
     *                    search. Initially it should be given as an empty list.
     * @return null if there are no cycles, else it returns the first node of
     *         the first cycle found
     */
    @SuppressWarnings("unchecked")
    public static DAGNode removeCycles(DAGNode currentnode, LinkedList<DAGNode> stack,
                                       LinkedList<DAGNode> visited, LinkedList<DAGNode> cycle) {

        int positionInStack = stack.indexOf(currentnode);
        if (positionInStack != -1) {
            // Cycle found, the root of the cycle is basenode
            // Collecting the full cycle list.
            cycle.addAll(stack.subList(positionInStack, stack.size()));
            addAllEquivalences(cycle);
            return currentnode;
        }

        stack.addLast(currentnode);

        while (testCycles(currentnode, new LinkedList<DAGNode>(), visited)) {
            for (DAGNode child : new LinkedList<DAGNode>(currentnode.getChildren())) {

                DAGNode cyclicNode = removeCycles(child, stack, visited, cycle);
                if (cyclicNode != null) {
                    /*
                    * A cycle was found collecting we should merge the
                    * current child and the current node up until we the
                    * cyclic node
                    */
                    if (!stack.getLast().equals(cyclicNode)) {
                        /*
                        * this node is part of a cycle, and is finally
                        * going to be removed
                        */
                        removeParentEdge(child, currentnode);

                        /*
                        * merging the parents and children and making sure
                        * that everything points to the cyclic node and not
                        * to the equivalent ones.
                        */

                        for (DAGNode parent : new LinkedList<DAGNode>(child.getParents())) {
                            removeParentEdge(child, parent);
                            addParentEdge(cyclicNode, parent);
                        }

                        for (DAGNode childchild : new LinkedList<DAGNode>(child.getChildren())) {
                            removeParentEdge(childchild, child);
                            addParentEdge(childchild, cyclicNode);
                        }

                        stack.removeLast();
                        return cyclicNode;
                    } else {
                        // we have reached the top of the cycle again

                        removeParentEdge(child, currentnode);

                        cycle.clear();

                        // Adjusting the children and parents of the child

                        for (DAGNode parent : new LinkedList<DAGNode>(child.getParents())) {
                            removeParentEdge(child, parent);
                            addParentEdge(cyclicNode, parent);
                        }


                        for (DAGNode childchild : new LinkedList<DAGNode>(child.getChildren())) {
                            removeParentEdge(childchild, child);
                            addParentEdge(childchild, cyclicNode);
                        }

                        if (currentnode.getChildren().isEmpty()) {
                            stack.removeLast();
                            return null;
                        }
                    }
                }
            }
        }
        stack.removeLast();
        return null;
    }

    /**
     * Tests if there is a cycle in the child tree that spawns from the current
     * node.
     *
     * @param currentnode
     * @param stack
     * @param visited
     * @return true if there is a cycle, false otherwise
     */
    public static boolean testCycles(DAGNode currentnode, LinkedList<DAGNode> stack,
                                     LinkedList<DAGNode> visited) {
        visited.add(currentnode);
        int positionInStack = stack.indexOf(currentnode);
        if (positionInStack != -1) {
            return true;
        }

        if (currentnode.getChildren().isEmpty())
            return false;

        stack.addLast(currentnode);
        for (DAGNode child : currentnode.getChildren()) {

            if (testCycles(child, stack, visited)) {
                stack.removeLast();
                return true;
            }
        }
        stack.removeLast();
        return false;
    }

    /**
     * This method will look for the node testnode in all the "parent" paths
     * that spawn from the node currentpathelement. It will do this by
     * transitively calling itself for each parent of currenthpathelement.
     *
     * @param testnode           the node we want to check
     * @param currentpathelement a node to start checking for parent paths.
     * @return true if testnode is in the path of parents starting from
     *         currentpathelement, false otherwise.
     */
    public static boolean isInAncestorPath(DAGNode testnode, DAGNode currentpathelement) {
        if (currentpathelement.equals(testnode))
            return true;
        if (currentpathelement.getParents().isEmpty())
            return false;

        // log.debug("Test node: {}. Path node: {}", testnode.getId(),
        // currentpathelement.getId());
        for (DAGNode nextpathelement : currentpathelement.getParents()) {
            if (nextpathelement.equals(currentpathelement)) {
                throw new RuntimeException("Error, loop. testnode: "
                        + testnode + " path node: "
                        + currentpathelement);
            }
            if (isInAncestorPath(testnode, nextpathelement)) {
                return true;
            }
        }
        return false;
    }

    /**
     * This method will remove all redundant parent edges of this node.
     * <p/>
     * If the current node is a, then an edge a->b is redundant if b is a node
     * that appears in transitive parent path that starts in a with an edge
     * different from a->b. For example, given nodes and edges:
     * <p/>
     * a -> b, b -> c a -> c
     * <p/>
     * Then the edge a->c is redundant, since c is a node that is included in
     * the transitive parent relation a->b->c
     *
     * @param basenode The node to analyze
     * @return The number of edges that have been removed from this node due to
     *         redundancy.
     */
    public static int removeRedundantEdges(DAGNode basenode) {
        DAGNode testnode = null;
        int testindex = 0;
        int edgesRemoved = 0;
        if (basenode.getParents().isEmpty())
            return 0;

        //LinkedList<DAGNode> base_parents = new LinkedList<DAGNode>(basenode.getParents());
        while (testindex < basenode.getParents().size()) {
            testnode = basenode.getParents().get(testindex);
            DAGNode candidatepathnode = null;
            int candidateIndex = 0;
            while (candidateIndex < basenode.getParents().size()) {
                candidatepathnode = basenode.getParents().get(candidateIndex);
                // do not compare to the same edge
                if (testindex == candidateIndex) {
                    candidateIndex += 1;
                    continue;
                }

                boolean nodeIsInAnotherPath = isInAncestorPath(testnode, candidatepathnode);
                if (nodeIsInAnotherPath) {
                    /* The parent edge is redundant, so we remove it */
                    removeParentEdge(basenode, basenode.getParents().get(testindex));
                    edgesRemoved += 1;

                    /*
                          * now we have to get out of the while, but we have to
                          * adjust the index to take into account the left shift that
                          * just happened in 'parents' so that we dont skip any edges
                          */
                    testindex -= 1;
                    break;
                }
                candidateIndex += 1;
            }
            testindex += 1;
        }
        return edgesRemoved;
    }


    /**
     * This will examine all the relevant nodes in the DAG (those that have
     * multiple parent edges) to try to remove redundant edges. See {@see
     * removeRedundantEdges}
     *
     * @return the total number of redundant edges removed.
     */
    public static int computeTransitiveReduct(Map<String, DAGNode> dagnodes) {
        /*
           * First we collect all candidate redundant edges by asking for all
           * nodes that have at lest two parents
           */
        Collection<DAGNode> allnodes = dagnodes.values();
        LinkedList<DAGNode> potentialReundancyNodes = new LinkedList<DAGNode>();
        for (DAGNode node : allnodes) {
            if (node.getParents().size() > 1) {
                potentialReundancyNodes.add(node);
            }
        }

        int edgesRemoved = 0;
        int candidateCount = 0;
        int previousPercentage = 0;
        for (DAGNode node : potentialReundancyNodes) {
            candidateCount += 1;
            previousPercentage = logProgress("{}%", candidateCount,
                    potentialReundancyNodes.size(), previousPercentage, 25);
            /*
                * Now we check each parent edge of this node to see if it is
                * redundant
                */
            edgesRemoved += removeRedundantEdges(node);
        }
        return edgesRemoved;
    }

    /**
     * Removes all cycles in the graph, transforming it into a DAG. Cycles will
     * be compressed into equivalent relations. {@see removeCycles}
     *
     * @return
     */
    public static int removeCycles(Map<String, DAGNode> dagnodes) {
        int currentconcept;
        int currentpercentage;
        // Looking for the nodes with no parents (roots)
        Collection<DAGNode> allnodes = dagnodes.values();
        LinkedList<DAGNode> roots = new LinkedList<DAGNode>();
        for (DAGNode n : allnodes) {
            if (n.getParents().isEmpty()) {
                roots.add(n);
            }
        }
        log.debug("Found {} candidate root nodes.", roots.size());
        log.debug("Checking for cycles");

        currentpercentage = 0;

        HashSet<DAGNode> covered = new HashSet<DAGNode>(dagnodes.keySet().size());

        int ntotalparents = roots.size();
        while (!roots.isEmpty()) {
            DAGNode node = roots.iterator().next();
            LinkedList<DAGNode> visited = new LinkedList<DAGNode>();
            removeCycles(node, visited);
            covered.addAll(visited);

            currentconcept = ntotalparents - roots.size();
            currentpercentage = logProgress("{}%", currentconcept,
                    ntotalparents, currentpercentage, 25);
            roots.remove(node);
        }

        int nconcepts = dagnodes.values().size();
        log.debug("Covered nodes: {} Total nodes: {}", covered.size(),
                nconcepts);
        log.debug("Missing nodes: {}", nconcepts - covered.size());

        /***
         * looking for nodes that where not processed, these will be nodes that
         * are in a path like a -> b -> c -> b. Since there is no parentless
         * node in this path, it will not have been taken into account in the
         * previous search.
         */
        log.debug("Collecting missing nodes");
        Iterator<DAGNode> allit = dagnodes.values().iterator();
        HashSet<DAGNode> remainingnodes = new HashSet<DAGNode>(dagnodes.size());
        while (allit.hasNext()) {
            DAGNode node = allit.next();
            remainingnodes.add(node);
        }
        remainingnodes.removeAll(covered);

        log.debug("Processing the remaining nodes");
        ntotalparents = remainingnodes.size();
        int newtasksize = remainingnodes.size();
        while (!remainingnodes.isEmpty()) {
            DAGNode node = remainingnodes.iterator().next();
            LinkedList<DAGNode> visited = new LinkedList<DAGNode>();
            removeCycles(node, visited);
            covered.addAll(visited);

            currentconcept = newtasksize - remainingnodes.size();
            currentpercentage = logProgress("{}%", currentconcept, newtasksize,
                    currentpercentage, 25);
            remainingnodes.removeAll(visited);
        }

        nconcepts = dagnodes.values().size();
        log.debug("Covered nodes: {} Total nodes: {}", covered.size(),
                nconcepts);
        log.debug("Missing nodes: {}", nconcepts - covered.size());

        int missingnodes = nconcepts - covered.size();
        covered.clear();
        covered = null;

        return missingnodes;

    }

    private static int logProgress(String message, int currentvalue, int totalvalue,
                                   int previousPercentage, int step) {
        int currentpercentage = (currentvalue * 100) / totalvalue;
        if (currentpercentage - previousPercentage == step) {
            log.info(message, currentpercentage);
            return currentpercentage;
        }
        return previousPercentage;
    }

//
//    private static int index = 0;
//    private static ArrayList<DAGNode> stack;
//    private static ArrayList<ArrayList<DAGNode>> SCC;
//
//    private static Map<DAGNode, Integer> t_idx;
//    private static Map<DAGNode, Integer> t_low_idx;
//
//    public static ArrayList<ArrayList<DAGNode>> scc(Map<String, DAGNode> list) {
//        stack = new ArrayList<DAGNode>();
//        SCC = new ArrayList<ArrayList<DAGNode>>();
//        t_idx = new HashMap<DAGNode, Integer>();
//        t_low_idx = new HashMap<DAGNode, Integer>();
//        for (DAGNode node : list.values()) {
//            if (t_idx.get(node) == null) {
//                strongconnect(node);
//            }
//        }
//        return SCC;
//    }
//
//    public static void strongconnect(DAGNode v) {
//        t_idx.put(v, index);
//        t_low_idx.put(v, index);
//
//        index++;
//        stack.add(0, v);
//        for (DAGNode child : v.getChildren()) {
//            if (t_idx.get(child) == null) {
//                strongconnect(child);
//                t_low_idx.put(v, Math.min(t_low_idx.get(v), t_low_idx.get(child)));
//            } else if (stack.contains(child)) {
//                t_low_idx.put(v, Math.min(t_low_idx.get(v), t_idx.get(child)));
//            }
//        }
//        if (t_low_idx.get(v).equals(t_idx.get(v))) {
//            DAGNode n;
//            ArrayList<DAGNode> component = new ArrayList<DAGNode>();
//            do {
//                n = stack.remove(0);
//                component.add(n);
//            } while (!n.equals(v));
//            SCC.add(component);
//        }
//    }

}
