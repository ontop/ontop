package org.semanticweb.ontop.pivotalrepr;

import com.google.common.collect.ImmutableList;

/**
 * TODO: explain
 */
public interface NewSubNodeSelectionProposal extends LocalOptimizationProposal {

    /**
     * Query node on which to apply the optimization proposal.
     */
    QueryNode getQueryNode();

    /**
     * List of ALL its sub-nodes.
     *
     * --> other nodes are not sub-nodes (anymore).
     */
    ImmutableList<QueryNode> getSubNodes();
}
