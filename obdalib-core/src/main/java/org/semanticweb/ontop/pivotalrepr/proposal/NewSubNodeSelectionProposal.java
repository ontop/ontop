package org.semanticweb.ontop.pivotalrepr.proposal;

import com.google.common.collect.ImmutableList;
import org.semanticweb.ontop.pivotalrepr.QueryNode;

/**
 * TODO: explain
 */
@Deprecated
public interface NewSubNodeSelectionProposal extends QueryOptimizationProposal {

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
