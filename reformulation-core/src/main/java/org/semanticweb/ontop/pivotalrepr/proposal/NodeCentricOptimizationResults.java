package org.semanticweb.ontop.pivotalrepr.proposal;

import com.google.common.base.Optional;
import org.semanticweb.ontop.pivotalrepr.QueryNode;

/**
 * TODO: explain
 */
public interface NodeCentricOptimizationResults extends ProposalResults {

    /**
     * TODO: explain
     */
    Optional<QueryNode> getOptionalNewParentNode();

    /**
     * TODO: explain
     */
    Optional<QueryNode> getOptionalNewNode();
}
