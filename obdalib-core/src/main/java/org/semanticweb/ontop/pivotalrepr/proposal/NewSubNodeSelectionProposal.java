package org.semanticweb.ontop.pivotalrepr.proposal;

import com.google.common.collect.ImmutableList;
import org.semanticweb.ontop.pivotalrepr.QueryNode;
import org.semanticweb.ontop.pivotalrepr.proposal.LocalOptimizationProposal;

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
