package org.semanticweb.ontop.pivotalrepr.impl;

import com.google.common.collect.ImmutableList;
import com.google.common.collect.Lists;
import org.jgraph.graph.DefaultEdge;
import org.jgrapht.experimental.dag.DirectedAcyclicGraph;
import org.jgrapht.traverse.TopologicalOrderIterator;
import org.semanticweb.ontop.pivotalrepr.IntermediateQuery;
import org.semanticweb.ontop.pivotalrepr.QueryNode;

import java.util.List;
import java.util.Set;

/**
 * TODO: describe
 */
public class IntermediateQueryImpl implements IntermediateQuery {

    /**
     * TODO: explain
     */
    private final DirectedAcyclicGraph<QueryNode, DefaultEdge> queryDAG;

    /**
     * Cached value (non final)
     *
     */
    private ImmutableList<QueryNode> nodesInAntiTopologicalOrder;

    /**
     * TODO: integrate with Guice
     */
    public IntermediateQueryImpl() {
        nodesInAntiTopologicalOrder = null;
        queryDAG = new DirectedAcyclicGraph<>(DefaultEdge.class);
    }


    @Override
    public ImmutableList<QueryNode> getNodesInAntiTopologicalOrder() {

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

}
