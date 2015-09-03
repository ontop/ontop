package org.semanticweb.ontop.pivotalrepr.proposal;

import org.semanticweb.ontop.pivotalrepr.QueryNode;

/**
 * TODO: explain
 */
public interface ReactToChildDeletionResults extends ProposalResults {

//    /**
//     * These nodes were belonging to the FORMER intermediate query.
//     *
//     * Does NOT CONTAIN the first deleted child that has started this reaction
//     * (the latter is accessible through the proposal).
//     * May be empty.
//     */
//    ImmutableList<QueryNode> getDeletedAncestry();

    /**
     * The closest ancestor of the deleted node(s)
     * that has not been deleted.
     */
    QueryNode getClosestRemainingAncestor();
}
