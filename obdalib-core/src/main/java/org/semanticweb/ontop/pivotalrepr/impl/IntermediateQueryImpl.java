package org.semanticweb.ontop.pivotalrepr.impl;

import com.google.common.collect.ImmutableList;
import com.google.common.collect.Lists;
import org.jgraph.graph.DefaultEdge;
import org.jgrapht.experimental.dag.DirectedAcyclicGraph;
import org.jgrapht.traverse.TopologicalOrderIterator;
import org.semanticweb.ontop.pivotalrepr.*;

import java.util.HashSet;
import java.util.List;
import java.util.Set;

/**
 * TODO: describe
 */
public class IntermediateQueryImpl implements IntermediateQuery {

    /**
     * TODO: explain.
     *
     * Implementation detail: this object must NOT BE SHARED with the other classes.
     */
    private final DirectedAcyclicGraph<QueryNode, DefaultEdge> queryDAG;

    /**
     * Cached value (non final)
     */
    private ImmutableList<QueryNode> nodesInAntiTopologicalOrder;

    /**
     * TODO: integrate with Guice
     */
    public IntermediateQueryImpl() {
        nodesInAntiTopologicalOrder = null;
        queryDAG = new DirectedAcyclicGraph<>(DefaultEdge.class);
    }

    /**
     * For IntermediateQueryBuilders ONLY!!
     */
    protected IntermediateQueryImpl(DirectedAcyclicGraph<QueryNode, DefaultEdge> queryDAG) {
        nodesInAntiTopologicalOrder = null;
        this.queryDAG = queryDAG;
    }

    @Override
    public ImmutableList<QueryNode> getNodesInBottomUpOrder() {

        /**
         * Computes the list if not cached
         */
        if (nodesInAntiTopologicalOrder == null) {
            TopologicalOrderIterator<QueryNode, DefaultEdge> it =
                    new TopologicalOrderIterator<>(queryDAG);

            List<QueryNode> nodesInTopologicalOrder = Lists.newArrayList(it);
            nodesInAntiTopologicalOrder = ImmutableList.copyOf(Lists.reverse(
                    nodesInTopologicalOrder));
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


    /**
     * The order of sub-node selection is ignored.
     */
    @Override
    public QueryNode applySubNodeSelectionProposal(NewSubNodeSelectionProposal proposal) throws InvalidLocalOptimizationProposalException {
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
        return null;
    }

    @Override
    public void mergeRule(Rule rule) {
        // TODO: implement it
    }

    /**
     * Dependency: edge from a QueryNode to its sub-node.
     */
    private void removeDependency(DefaultEdge dependencyEdge) {
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

}
