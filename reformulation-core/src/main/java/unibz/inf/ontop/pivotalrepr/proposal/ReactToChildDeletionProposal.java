package unibz.inf.ontop.pivotalrepr.proposal;

import unibz.inf.ontop.pivotalrepr.QueryNode;

import java.util.Optional;

/**
 * TODO: explain
 *
 * Please note they are initial nodes. A cascade of deletion may appear.
 *
 */
public interface ReactToChildDeletionProposal extends QueryOptimizationProposal<ReactToChildDeletionResults> {

    /**
     * Parent of the child that has been removed from the query.
     */
    QueryNode getParentNode();

    QueryNode getDeletedChild();

    Optional<QueryNode> getOptionalNextSibling();
}
