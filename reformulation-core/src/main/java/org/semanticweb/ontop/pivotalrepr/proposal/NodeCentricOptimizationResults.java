package org.semanticweb.ontop.pivotalrepr.proposal;

import org.semanticweb.ontop.pivotalrepr.QueryNode;

import java.util.Optional;

/**
 * TODO: explain
 */
public interface NodeCentricOptimizationResults<N extends QueryNode> extends ProposalResults {

    /**
     * TODO: explain
     */
    Optional<N> getOptionalNewNode();

    /**
     * TODO: explain
     */
    Optional<QueryNode> getOptionalNextSibling();

    /**
     * TODO: explain
     *
     */
    Optional<QueryNode> getOptionalClosestAncestor();

    /**
     * When the focus node is officially replaced by one
     * of its children
     */
    Optional<QueryNode> getOptionalReplacingChild();
}
