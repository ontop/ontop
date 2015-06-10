package org.semanticweb.ontop.pivotalrepr.impl;

import com.google.common.base.Optional;
import com.google.common.collect.ImmutableList;
import com.google.common.collect.ImmutableMap;
import com.google.common.collect.Lists;
import org.jgraph.graph.DefaultEdge;
import org.jgrapht.experimental.dag.DirectedAcyclicGraph;
import org.jgrapht.traverse.TopologicalOrderIterator;
import org.semanticweb.ontop.model.Function;
import org.semanticweb.ontop.model.Variable;
import org.semanticweb.ontop.owlrefplatform.core.optimization.DetypingOptimizer;
import org.semanticweb.ontop.pivotalrepr.*;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.HashSet;
import java.util.List;
import java.util.Set;

/**
 * TODO: describe
 *
 * BEWARE: this class has a non-trivial mutable internal state!
 */
public class IntermediateQueryImpl implements IntermediateQuery {

    /**
     * TODO: explain
     */
    protected static class IllegalDAGException extends RuntimeException {
        protected IllegalDAGException(String message) {
            super(message);
        }
    }

    /**
     * TODO: explain.
     *
     * Implementation detail: this object must NOT BE SHARED with the other classes.
     */
    private final DirectedAcyclicGraph<QueryNode, DefaultEdge> queryDAG;
    private static final Logger LOGGER = LoggerFactory.getLogger(IntermediateQueryImpl.class);

    /**
     * MAKE SURE it remains the "root" of the tree/DAG.
     * MAY BE NULL!
     *
     * TODO: mark it as Nullable.
     */
    private ProjectionNode rootProjectionNode;

    /**
     * Cached value (non final). MAY BE NULL
     *
     * * TODO: mark it as Nullable.
     */
    private ImmutableList<QueryNode> nodesInAntiTopologicalOrder;

    /**
     * For IntermediateQueryBuilders ONLY!!
     */
    protected IntermediateQueryImpl(DirectedAcyclicGraph<QueryNode, DefaultEdge> queryDAG)
            throws IllegalDAGException {
        this.queryDAG = queryDAG;

        /**
         * Cache attributes.
         * May throw an IllegalDAGException during their computation.
         *
         */
        this.nodesInAntiTopologicalOrder = null;
        this.rootProjectionNode = null;
        computeNodeTopologyCache();
    }

    @Override
    public ProjectionNode getRootProjectionNode() throws IllegalDAGException {
        if (rootProjectionNode == null) {
            computeNodeTopologyCache();
        }
        return rootProjectionNode;
    }

    @Override
    public ImmutableList<QueryNode> getNodesInBottomUpOrder() {

        /**
         * Computes the list if not cached
         */
        if (nodesInAntiTopologicalOrder == null) {
            computeNodeTopologyCache();
        }

        return nodesInAntiTopologicalOrder;
    }

    @Override
    public ImmutableList<QueryNode> getCurrentSubNodesOf(QueryNode node) {
        Set<DefaultEdge> outgoingEdges = queryDAG.outgoingEdgesOf(node);
        ImmutableList.Builder<QueryNode> nodeListBuilder = ImmutableList.builder();
        for (DefaultEdge edge : outgoingEdges) {
            nodeListBuilder.add((QueryNode) edge.getTarget());
        }

        return nodeListBuilder.build();
    }

    @Override
    public boolean contains(QueryNode node) {
        return queryDAG.containsVertex(node);
    }


    /**
     * The order of sub-node selection is ignored.
     */
    @Override
    public QueryNode applySubNodeSelectionProposal(NewSubNodeSelectionProposal proposal)
            throws InvalidLocalOptimizationProposalException {
        resetNodeTopologyCache();
        QueryNode currentNode = proposal.getQueryNode();

        Set<QueryNode> proposedSubNodesToConsider = new HashSet<>(proposal.getSubNodes());

        /**
         * Existing sub-nodes: keep or remove
         */
        Set<DefaultEdge> outgoingEdges = queryDAG.outgoingEdgesOf(currentNode);
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
                queryDAG.addDagEdge(currentNode, newSubNode);
            } catch (DirectedAcyclicGraph.CycleFoundException ex) {
                // Inconsistent proposal (should not introduce a cycle in the DAG) --> throw an exception.
                throw new InvalidLocalOptimizationProposalException(ex.getMessage());
            }
        }

        return currentNode;
    }

    /**
     * TODO: implement it
     *
     */
    @Override
    public QueryNode applyReplaceNodeProposal(ReplaceNodeProposal proposal)
            throws InvalidLocalOptimizationProposalException {
        resetNodeTopologyCache();
        return null;
    }

    @Override
    @Deprecated
    public void detypeNode(QueryNode nodeToDetype) {

        if (!contains(nodeToDetype)) {
            throw new IllegalArgumentException("The node is not contained in the query");
        }

        DetypingOptimizer optimizer = new DetypingOptimizer(this);
        Optional<LocalOptimizationProposal> optionalProposal = nodeToDetype.acceptOptimizer(optimizer);

        if (!optionalProposal.isPresent()) {
            LOGGER.debug(nodeToDetype + " was not typed (thus nothing to detype).");
        }
        else {
            try {
                optionalProposal.get().apply();
            }
            /**
             * Should not happen since we created the proposal here
             */
            catch (InvalidLocalOptimizationProposalException e) {
                throw new RuntimeException("Internal error while detyping a node: " + e.getLocalizedMessage());
            }
        }
    }

    @Override
    @Deprecated
    public QueryNode applyDetypingProposal(DetypingProposal proposal)
            throws InvalidLocalOptimizationProposalException {
        return null;
    }

    @Override
    public void mergeSubQuery(IntermediateQuery subQuery) throws QueryMergingException {
        resetNodeTopologyCache();
        // TODO: implement it
    }

    @Override
    public boolean isTyped() {
        return getRootProjectionNode().isTyped();
    }

    /**
     * Dependency: edge from a QueryNode to its sub-node.
     */
    private void removeDependency(DefaultEdge dependencyEdge) {
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
     * TODO: describe
     */
    private void computeNodeTopologyCache() throws IllegalDAGException {
        nodesInAntiTopologicalOrder = extractNodeOrder(queryDAG);
        rootProjectionNode = extractRootProjectionNode(nodesInAntiTopologicalOrder);
    }

    /**
     * TODO: describe
     */
    private void resetNodeTopologyCache() {
        nodesInAntiTopologicalOrder = null;
        rootProjectionNode = null;
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
    private static ProjectionNode extractRootProjectionNode(ImmutableList<QueryNode> nodesInAntiTopologicalOrder)
        throws IllegalDAGException{
        if (nodesInAntiTopologicalOrder.isEmpty()) {
            throw new IllegalDAGException("Empty DAG!");
        }

        QueryNode rootNode = nodesInAntiTopologicalOrder.get(0);
        if (!(rootNode instanceof ProjectionNode)) {
            throw new IllegalDAGException("The root node is not a ProjectionNode: " + rootNode);
        }

        return (ProjectionNode) rootNode;
    }

    /**
     * TODO: implement it
     */
    @Override
    public Variable createNewVariable() {
        throw new RuntimeException("TODO: implement it");
    }

}
