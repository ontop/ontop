package it.unibz.inf.ontop.pivotalrepr.proposal;

import it.unibz.inf.ontop.pivotalrepr.QueryNode;

import java.util.Optional;

/**
 * Low-level proposal, to be used by ProposalExecutors only!
 *
 * TODO: explain
 *
 *
 * The child is already deleted.
 *
 * Please note they are initial nodes. A cascade of deletion may appear.
 *
 */
public interface ReactToChildDeletionProposal extends QueryOptimizationProposal<ReactToChildDeletionResults> {

    /**
     * Parent of the child that has been removed from the query.
     */
    QueryNode getParentNode();

    Optional<QueryNode> getOptionalNextSibling();
}
