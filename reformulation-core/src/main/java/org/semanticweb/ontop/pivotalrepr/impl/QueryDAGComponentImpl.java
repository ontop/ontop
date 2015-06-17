package org.semanticweb.ontop.pivotalrepr.impl;

import com.google.common.collect.ImmutableList;
import com.google.common.collect.Lists;
import org.jgraph.graph.DefaultEdge;
import org.jgrapht.experimental.dag.DirectedAcyclicGraph;
import org.jgrapht.traverse.TopologicalOrderIterator;
import org.semanticweb.ontop.pivotalrepr.IntermediateQuery;
import org.semanticweb.ontop.pivotalrepr.ConstructionNode;
import org.semanticweb.ontop.pivotalrepr.QueryNode;

import java.util.HashSet;
import java.util.List;
import java.util.Set;

/**
 * Basic implementation
 *
 */
public class QueryDAGComponentImpl implements QueryDAGComponent {

    /**
     * TODO: explain.
     *
     * Implementation detail: this object must NOT BE SHARED with the other classes.
     */
    private final DirectedAcyclicGraph<QueryNode, DefaultEdge> queryDAG;

    /**
     * MAKE SURE it remains the "root" of the tree/DAG.
     * MAY BE NULL!
     *
     * TODO: mark it as Nullable.
     */
    private ConstructionNode rootConstructionNode;

    /**
     * Cached value (non final). MAY BE NULL
     *
     * * TODO: mark it as Nullable.
     */
    private ImmutableList<QueryNode> nodesInAntiTopologicalOrder;


    protected QueryDAGComponentImpl(DirectedAcyclicGraph<QueryNode, DefaultEdge> queryDAG)
            throws IllegalDAGException {
        this.queryDAG = queryDAG;
        /**
         * Cache attributes.
         * May throw an IllegalDAGException during their computation.
         *
         */
        this.nodesInAntiTopologicalOrder = null;
        this.rootConstructionNode = null;
        computeNodeTopologyCache();
    }

    @Override
    public ImmutableList<QueryNode> getCurrentSubNodesOf(QueryNode node) {
        return DAGUtils.getSubNodesOf(queryDAG, node);
    }

    @Override
    public ConstructionNode getRootConstructionNode() throws IllegalDAGException {
        if (rootConstructionNode == null) {
            computeNodeTopologyCache();
        }
        return rootConstructionNode;
    }

    @Override
    public ImmutableList<QueryNode> getNodesInBottomUpOrder() throws IllegalDAGException {

        /**
         * Computes the list if not cached
         */
        if (nodesInAntiTopologicalOrder == null) {
            computeNodeTopologyCache();
        }

        return nodesInAntiTopologicalOrder;
    }

    @Override
    public boolean contains(QueryNode node) {
        return queryDAG.containsVertex(node);
    }


    /**
     * TODO: explain
     */
    @Override
    public void replaceNode(QueryNode previousNode, QueryNode replacingNode) {
        queryDAG.addVertex(replacingNode);
        try {
            for (DefaultEdge incomingEdge : queryDAG.incomingEdgesOf(previousNode)) {
                queryDAG.addDagEdge((QueryNode)incomingEdge.getSource(), replacingNode);
            }

            for (DefaultEdge outgoingEdge : queryDAG.outgoingEdgesOf(previousNode)) {
                queryDAG.addDagEdge(replacingNode, (QueryNode)outgoingEdge.getTarget());
            }

        } catch (DirectedAcyclicGraph.CycleFoundException e) {
            throw new RuntimeException("BUG: " + e.getLocalizedMessage());
        }
        queryDAG.removeVertex(previousNode);
    }

    /**
     * Dependency: edge from a QueryNode to its sub-node.
     *
     * TODO: simplify it if we decide to switch to a tree.
     *
     */
    @Override
    public void removeDependency(DefaultEdge dependencyEdge) {
        resetNodeTopologyCache();

        QueryNode subNode = (QueryNode) dependencyEdge.getTarget();
        queryDAG.removeEdge(dependencyEdge);

        /**
         * Checks if the sub-node is still a dependency.
         *
         * If not, removes it.
         */
        if (queryDAG.incomingEdgesOf(subNode).isEmpty()) {
            queryDAG.removeVertex(subNode);
        }
    }

    /**
     * TODO: explain
     * TODO: replace this recursive implementation but iterative one
     * Low-level. Tail recursive.
     */
    @Override
    public void addSubTree(IntermediateQuery subQuery, QueryNode parentNode) {
        for (QueryNode childNode : subQuery.getCurrentSubNodesOf(parentNode)) {
            queryDAG.addVertex(childNode);
            try {
                queryDAG.addDagEdge(parentNode, childNode);
            } catch (DirectedAcyclicGraph.CycleFoundException e) {
                throw new RuntimeException("BUG (internal error)" + e.getLocalizedMessage());
            }
            // Recursive call
            addSubTree(subQuery, childNode);
        }
    }

    @Override
    public void setChildrenNodes(QueryNode parentNode, List<QueryNode> allChildrenNodes) throws IllegalDAGException {

        Set<QueryNode> proposedSubNodesToConsider = new HashSet<>(allChildrenNodes);

        /**
         * Existing sub-nodes: keep or remove
         */
        Set<DefaultEdge> outgoingEdges = queryDAG.outgoingEdgesOf(parentNode);
        for (DefaultEdge dependencyEdge : outgoingEdges) {
            QueryNode subNode = (QueryNode) dependencyEdge.getTarget();
            // Kept
            if (proposedSubNodesToConsider.contains(subNode)) {
                proposedSubNodesToConsider.remove(subNode);
            }
            // Removed
            else {
                removeDependency(dependencyEdge);
            }
        }

        /**
         * New sub-nodes: added to the DAG
         */
        for (QueryNode newSubNode : proposedSubNodesToConsider) {
            if (!queryDAG.containsVertex(newSubNode)) {
                queryDAG.addVertex(newSubNode);
            }
            try {
                queryDAG.addDagEdge(parentNode, newSubNode);
            } catch (DirectedAcyclicGraph.CycleFoundException ex) {
                // Inconsistent proposal (should not introduce a cycle in the DAG) --> throw an exception.
                // TODO: return a non- RuntimeException.
                throw new IllegalDAGException(ex.getMessage());
            }
        }
    }

    /**
     * TODO: implement it
     */
    @Override
    public ImmutableList<QueryNode> getSubTreeNodesInTopDownOrder(QueryNode currentNode) {
        throw new RuntimeException("TODO: implement it");
    }

    /**
     * TODO: describe
     */
    private void computeNodeTopologyCache() throws IllegalDAGException {
        nodesInAntiTopologicalOrder = extractNodeOrder(queryDAG);
        rootConstructionNode = extractRootProjectionNode(nodesInAntiTopologicalOrder);
    }

    /**
     * TODO: describe
     */
    private void resetNodeTopologyCache() {
        nodesInAntiTopologicalOrder = null;
        rootConstructionNode = null;
    }

    /**
     * TODO: describe
     */
    private static ImmutableList<QueryNode> extractNodeOrder(DirectedAcyclicGraph<QueryNode, DefaultEdge> queryDAG) {
        TopologicalOrderIterator<QueryNode, DefaultEdge> it =
                new TopologicalOrderIterator<>(queryDAG);

        List<QueryNode> nodesInTopologicalOrder = Lists.newArrayList(it);
        ImmutableList<QueryNode> nodesInAntiTopologicalOrder = ImmutableList.copyOf(Lists.reverse(
                nodesInTopologicalOrder));
        return nodesInAntiTopologicalOrder;
    }

    /**
     * TODO: describe
     */
    private static ConstructionNode extractRootProjectionNode(ImmutableList<QueryNode> nodesInAntiTopologicalOrder)
            throws IllegalDAGException{
        if (nodesInAntiTopologicalOrder.isEmpty()) {
            throw new IllegalDAGException("Empty DAG!");
        }

        QueryNode rootNode = nodesInAntiTopologicalOrder.get(0);
        if (!(rootNode instanceof ConstructionNode)) {
            throw new IllegalDAGException("The root node is not a ProjectionNode: " + rootNode);
        }

        return (ConstructionNode) rootNode;
    }

}
