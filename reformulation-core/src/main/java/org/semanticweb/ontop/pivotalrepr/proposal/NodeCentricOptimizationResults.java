package org.semanticweb.ontop.pivotalrepr.proposal;

import com.google.common.base.Optional;
import com.google.common.collect.ImmutableList;
import org.semanticweb.ontop.pivotalrepr.QueryNode;

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
     * No ob
     *
     */
    Optional<QueryNode> getOptionalClosestAncestor();
}
