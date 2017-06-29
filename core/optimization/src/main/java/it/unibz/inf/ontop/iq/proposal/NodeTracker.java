package it.unibz.inf.ontop.iq.proposal;

import it.unibz.inf.ontop.iq.IntermediateQuery;
import it.unibz.inf.ontop.iq.node.QueryNode;

import java.util.Optional;

/**
 * TODO: explain
 *
 * Tracks the node updates
 *
 */
public interface NodeTracker {

    interface NodeUpdate<N extends QueryNode> {
        Optional<N> getNewNode();
        Optional<QueryNode> getReplacingChild();

        Optional<QueryNode> getNewNodeOrReplacingChild();


        /**
         * May only be defined if no replacing node is present
         */
        Optional<QueryNode> getOptionalNextSibling(IntermediateQuery query);
        /**
         * May only be defined if no replacing node is present
         */
        Optional<QueryNode> getOptionalClosestAncestor(IntermediateQuery query);
    }

    void recordUpcomingReplacementByChild(IntermediateQuery query, QueryNode formerNode, QueryNode replacingChildNode);

    void recordReplacement(QueryNode formerNode, QueryNode newNode);

//    void recordUpcomingRemoval(QueryNode node, Optional<QueryNode> optionalNextSibling,
//                               Optional<QueryNode> optionalClosestAncestor);

    void recordUpcomingRemoval(IntermediateQuery query, QueryNode subTreeRootNode);

    //void recordResults(IntermediateQuery query, QueryNode focusNode, NodeCentricOptimizationResults<? extends QueryNode> propagationResults);

    //boolean hasChanged(QueryNode ancestorNode);

    <N extends QueryNode> NodeUpdate<N> getUpdate(IntermediateQuery query, N node);
}
