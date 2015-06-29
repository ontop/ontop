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
    private ImmutableList<QueryNode> nodesInAntiTopologicalOrder;


    protected JgraphtQueryTreeComponent(DirectedAcyclicGraph<QueryNode, DefaultEdge> queryDAG)
            throws IllegalTreeException {
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

                // UGLY but was happening...
                if (outgoingEdge.getTarget() == null || outgoingEdge.getSource() == null) {
                    continue;
                }
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
    private void removeDependency(DefaultEdge dependencyEdge) {
        resetNodeTopologyCache();

        QueryNode subNode = (QueryNode) dependencyEdge.getSource();
        queryDAG.removeEdge(dependencyEdge);

        /**
         * Checks if the sub-node is still a dependency.
         *
         * If not, removes it.
         */
        if (queryDAG.outgoingEdgesOf(subNode).isEmpty()) {
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
        for (DefaultEdge dependencyEdge : incomingEdges) {
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
                throw new IllegalTreeException(ex.getMessage());
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
    private void computeNodeTopologyCache() throws IllegalTreeException {
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
            throws IllegalTreeException {
        if (nodesInAntiTopologicalOrder.isEmpty()) {
            throw new IllegalTreeException("Empty DAG!");
        }

        QueryNode rootNode = nodesInAntiTopologicalOrder.get(0);
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

            // UGLY!!!
            if (edge.getSource() == null || edge.getTarget() == null) {
                throw new RuntimeException("Internal error! Edge where the source or the target is missing!" + edge);
            }

            nodeListBuilder.add((QueryNode) edge.getSource());
        }

        return nodeListBuilder.build();
    }

}
