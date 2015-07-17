package org.semanticweb.ontop.pivotalrepr.proposal;

import org.semanticweb.ontop.pivotalrepr.QueryNode;

/**
 * TODO: describe
 */
public interface ReplaceNodeProposal extends LocalOptimizationProposal {
    /**
     * Query node on which to apply the optimization proposal.
     */
    QueryNode getNodeToReplace();

    QueryNode getReplacingNode();
}
