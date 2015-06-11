package org.semanticweb.ontop.pivotalrepr.impl;

import com.google.common.collect.ImmutableList;
import org.jgraph.graph.DefaultEdge;
import org.jgrapht.experimental.dag.DirectedAcyclicGraph;
import org.semanticweb.ontop.pivotalrepr.QueryNode;

import java.util.Set;

/**
 * Only for the IntermediateQueryBuilderImpl and IntermediateQueryImpl implementations !!!
 */
public class DAGUtils {

    protected static ImmutableList<QueryNode> getSubNodesOf(DirectedAcyclicGraph<QueryNode, DefaultEdge> queryDAG,
                                                            QueryNode node) {
        Set<DefaultEdge> outgoingEdges = queryDAG.outgoingEdgesOf(node);
        ImmutableList.Builder<QueryNode> nodeListBuilder = ImmutableList.builder();
        for (DefaultEdge edge : outgoingEdges) {
            nodeListBuilder.add((QueryNode) edge.getTarget());
        }

        return nodeListBuilder.build();
    }

}
