package org.semanticweb.ontop.pivotalrepr.impl;

import com.google.common.collect.ImmutableList;
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
 * Basic implementation based on a JGrapht DAG.
 *
 * TODO: debug it
 *
 */
public class JgraphtQueryTreeComponent implements QueryTreeComponent {

    /**
     * TODO: explain.
     *
     * Implementation detail: this object must NOT BE SHARED with the other classes.
     */
    private final DirectedAcyclicGraph<QueryNode, DefaultEdge> queryDAG;

    /**
     * MAKE SURE it remains the "root" of the tree.
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
    private ImmutableList<QueryNode> bottomUpOrderedNodes;


    protected JgraphtQueryTreeComponent(DirectedAcyclicGraph<QueryNode, DefaultEdge> queryDAG)
            throws IllegalTreeException {
        this.queryDAG = queryDAG;
        /**
         * Cache attributes.
         * May throw an IllegalTreeException during their computation.
         *
         */
        this.bottomUpOrderedNodes = null;
        this.rootConstructionNode = null;
        computeNodeTopologyCache();
    }

    @Override
    public ImmutableList<QueryNode> getCurrentSubNodesOf(QueryNode node) {
        return getSubNodesOf(queryDAG, node);
    }

    @Override
    public ConstructionNode getRootConstructionNode() throws IllegalTreeException {
        if (rootConstructionNode == null) {
            computeNodeTopologyCache();
        }
        return rootConstructionNode;
    }

    @Override
    public ImmutableList<QueryNode> getNodesInBottomUpOrder() throws IllegalTreeException {

        /**
         * Computes the list if not cached
         */
        if (bottomUpOrderedNodes == null) {
            computeNodeTopologyCache();
        }

        return bottomUpOrderedNodes;
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
        resetNodeTopologyCache();

        queryDAG.addVertex(replacingNode);
        try {
            for (DefaultEdge incomingEdge : queryDAG.incomingEdgesOf(previousNode)) {
                QueryNode child = queryDAG.getEdgeSource(incomingEdge);
                queryDAG.addDagEdge(child, replacingNode);
            }

            for (DefaultEdge outgoingEdge : queryDAG.outgoingEdgesOf(previousNode)) {
                QueryNode parent = queryDAG.getEdgeTarget(outgoingEdge);
                queryDAG.addDagEdge(replacingNode, parent);
            }

        } catch (DirectedAcyclicGraph.CycleFoundException e) {
            throw new RuntimeException("BUG: " + e.getLocalizedMessage());
        }
        queryDAG.removeVertex(previousNode);
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
                queryDAG.addDagEdge(childNode, parentNode);
            } catch (DirectedAcyclicGraph.CycleFoundException e) {
                throw new RuntimeException("BUG (internal error)" + e.getLocalizedMessage());
            }
            // Recursive call
            addSubTree(subQuery, childNode);
        }
    }

    @Override
    public void setChildrenNodes(QueryNode parentNode, List<QueryNode> allChildrenNodes) throws IllegalTreeException {

        Set<QueryNode> proposedSubNodesToConsider = new HashSet<>(allChildrenNodes);

        /**
         * Existing sub-nodes: keep or remove
         */
        Set<DefaultEdge> incomingEdges = queryDAG.incomingEdgesOf(parentNode);
        for (DefaultEdge subNodeEdge : incomingEdges) {
            QueryNode subNode = queryDAG.getEdgeSource(subNodeEdge);
            // Kept
            if (proposedSubNodesToConsider.contains(subNode)) {
                proposedSubNodesToConsider.remove(subNode);
            }
            // Removed
            else {
                removeSubTree(subNode);
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
                throw new IllegalTreeException(ex.getMessage());
            }
        }
    }

    /**
     * Removes all the nodes of a sub-tree,
     * all the edges between them and WITH THE REST OF TREE.
     *
     * Recursive
     */
    private void removeSubTree(QueryNode subTreeRoot) {
        for (DefaultEdge subNodeEdge : queryDAG.incomingEdgesOf(subTreeRoot)) {
            QueryNode childNode = queryDAG.getEdgeSource(subNodeEdge);
            /**
             * Recursive call.
             * Removing this edge is the responsibility of the child node.
             */
            removeSubTree(childNode);
        }

        for (DefaultEdge parentEdge : queryDAG.outgoingEdgesOf(subTreeRoot)) {
            queryDAG.removeEdge(parentEdge);
        }

        queryDAG.removeVertex(subTreeRoot);
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
    private void computeNodeTopologyCache() throws IllegalTreeException {
        bottomUpOrderedNodes = extractBottomUpOrderedNodes(queryDAG);
        rootConstructionNode = extractRootProjectionNode(bottomUpOrderedNodes);
    }

    /**
     * TODO: describe
     */
    private void resetNodeTopologyCache() {
        bottomUpOrderedNodes = null;
        rootConstructionNode = null;
    }

    /**
     * TODO: describe
     */
    private static ImmutableList<QueryNode> extractBottomUpOrderedNodes(DirectedAcyclicGraph<QueryNode, DefaultEdge> queryDAG) {
        TopologicalOrderIterator<QueryNode, DefaultEdge> it =
                new TopologicalOrderIterator<>(queryDAG);

        return ImmutableList.copyOf(it);
    }

    /**
     * TODO: describe
     */
    private static ConstructionNode extractRootProjectionNode(ImmutableList<QueryNode> topDownOrderedNodes)
            throws IllegalTreeException {
        if (topDownOrderedNodes.isEmpty()) {
            throw new IllegalTreeException("Empty DAG!");
        }

        QueryNode rootNode = topDownOrderedNodes.get(topDownOrderedNodes.size() - 1);
        if (!(rootNode instanceof ConstructionNode)) {
            throw new IllegalTreeException("The root node is not a ConstructionNode: " + rootNode);
        }

        return (ConstructionNode) rootNode;
    }

    @Override
    public String toString() {
        return queryDAG.toString();
    }


    /**
     * Edges are directed from the child to the parent.
     */
    protected static ImmutableList<QueryNode> getSubNodesOf(DirectedAcyclicGraph<QueryNode, DefaultEdge> queryDAG,
                                                            QueryNode node) {
        Set<DefaultEdge> incomingEdges = queryDAG.incomingEdgesOf(node);
        ImmutableList.Builder<QueryNode> nodeListBuilder = ImmutableList.builder();
        for (DefaultEdge edge : incomingEdges) {
            nodeListBuilder.add(queryDAG.getEdgeSource(edge));
        }

        return nodeListBuilder.build();
    }

}
