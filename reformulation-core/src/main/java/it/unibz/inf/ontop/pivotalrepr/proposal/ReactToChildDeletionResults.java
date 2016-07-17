package it.unibz.inf.ontop.pivotalrepr.proposal;

import it.unibz.inf.ontop.pivotalrepr.QueryNode;

import java.util.Optional;

/**
 * TODO: remove
 */
public interface ReactToChildDeletionResults {


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
