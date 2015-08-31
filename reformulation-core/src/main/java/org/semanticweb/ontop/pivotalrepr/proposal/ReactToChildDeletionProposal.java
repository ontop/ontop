package org.semanticweb.ontop.pivotalrepr.proposal;

import org.semanticweb.ontop.pivotalrepr.QueryNode;

/**
 * TODO: explain
 *
 * Please note they are initial nodes. A cascade of deletion may appear.
 *
 */
public interface ReactToChildDeletionProposal extends QueryOptimizationProposal {

    /**
     * Parent of the child that has been removed from the query.
     */
    QueryNode getParentNode();

    QueryNode getDeletedChild();
}
