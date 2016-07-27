package it.unibz.inf.ontop.pivotalrepr.proposal;

import it.unibz.inf.ontop.pivotalrepr.IntermediateQuery;
import it.unibz.inf.ontop.pivotalrepr.QueryNode;

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
        Optional<QueryNode> getOptionalNextSibling();
        /**
         * May only be defined if no replacing node is present
         */
        Optional<QueryNode> getOptionalClosestAncestor();
    }

    void recordUpcomingReplacementByChild(IntermediateQuery query, QueryNode formerNode, QueryNode replacingChildNode);

    void recordReplacement(QueryNode formerNode, QueryNode newNode);

//    void recordUpcomingRemoval(QueryNode node, Optional<QueryNode> optionalNextSibling,
//                               Optional<QueryNode> optionalClosestAncestor);

    void recordUpcomingRemoval(IntermediateQuery query, QueryNode subTreeRootNode);

    //void recordResults(IntermediateQuery query, QueryNode focusNode, NodeCentricOptimizationResults<? extends QueryNode> propagationResults);

    //boolean hasChanged(QueryNode ancestorNode);

    <N extends QueryNode> NodeUpdate<N> getUpdate(N node);
}
