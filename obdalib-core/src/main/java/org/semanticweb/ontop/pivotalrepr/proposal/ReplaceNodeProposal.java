package org.semanticweb.ontop.pivotalrepr.proposal;

import org.semanticweb.ontop.pivotalrepr.QueryNode;

/**
 * TODO: describe
 *
 * Proposal: DEPRECATED IT. Too low-level for a proposal
 *
 */
@Deprecated
public interface ReplaceNodeProposal extends QueryOptimizationProposal {
    /**
     * Query node on which to apply the optimization proposal.
     */
    QueryNode getNodeToReplace();

    QueryNode getReplacingNode();
}
