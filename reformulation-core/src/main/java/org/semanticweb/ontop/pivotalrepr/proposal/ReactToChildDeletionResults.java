package org.semanticweb.ontop.pivotalrepr.proposal;

import com.google.common.base.Optional;
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

    /**
     * First sibling found of a deleted node.
     * By definition, must be a child of the closest ancestor.
     */
    Optional<QueryNode> getOptionalNextSibling();
}
