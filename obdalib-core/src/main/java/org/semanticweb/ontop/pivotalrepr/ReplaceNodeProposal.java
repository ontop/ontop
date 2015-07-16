package org.semanticweb.ontop.pivotalrepr;

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
