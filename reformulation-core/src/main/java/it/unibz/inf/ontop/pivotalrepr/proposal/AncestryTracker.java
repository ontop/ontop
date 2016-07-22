package it.unibz.inf.ontop.pivotalrepr.proposal;

import it.unibz.inf.ontop.pivotalrepr.QueryNode;

import java.util.Optional;

/**
 * TODO: explain
 *
 * Tracks the events related to ancestors
 *
 */
public interface AncestryTracker {

    void recordReplacementByChild(QueryNode formerNode, QueryNode replacingChildNode);

    void recordReplacement(QueryNode formerNode, QueryNode newNode);

    void recordEmptinessDeclaration(QueryNode ancestorNode, Optional<QueryNode> optionalNextSibling,
                                    Optional<QueryNode> optionalClosestAncestor);

    void recordResults(QueryNode focusNode, NodeCentricOptimizationResults<? extends QueryNode> propagationResults);

    //boolean hasChanged(QueryNode ancestorNode);

    /**
     * Returns the same node if node updated
     */
    <N extends QueryNode> Optional<N> getCurrentNode(N ancestorNode);
}
