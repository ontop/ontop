package org.semanticweb.ontop.pivotalrepr.impl;

import com.google.common.collect.ImmutableList;
import com.google.common.collect.Lists;
import org.jgraph.graph.DefaultEdge;
import org.jgrapht.experimental.dag.DirectedAcyclicGraph;
import org.jgrapht.traverse.TopologicalOrderIterator;
import org.semanticweb.ontop.pivotalrepr.IntermediateQuery;
import org.semanticweb.ontop.pivotalrepr.NodeView;
import java.util.List;

/**
 * TODO: describe
 */
public class IntermediateQueryImpl implements IntermediateQuery {

    /**
     * TODO: explain
     */
    private final DirectedAcyclicGraph<NodeView, DefaultEdge> queryDAG;

    /**
     * Cached value (non final)
     *
     */
    private ImmutableList<NodeView> nodesInAntiTopologicalOrder;

    /**
     * TODO: integrate with Guice
     */
    public IntermediateQueryImpl() {
        nodesInAntiTopologicalOrder = null;
        queryDAG = new DirectedAcyclicGraph<>(DefaultEdge.class);
    }


    @Override
    public ImmutableList<NodeView> getNodesInAntiTopologicalOrder() {

        /**
         * Computes the list if not cached
         */
        if (nodesInAntiTopologicalOrder == null) {
            TopologicalOrderIterator<NodeView, DefaultEdge> it =
                    new TopologicalOrderIterator<>(queryDAG);

            List<NodeView> nodesInTopologicalOrder = Lists.newArrayList(it);
            nodesInAntiTopologicalOrder = ImmutableList.copyOf(Lists.reverse(
                    nodesInTopologicalOrder));
        }

        return nodesInAntiTopologicalOrder;
    }

    public
}
