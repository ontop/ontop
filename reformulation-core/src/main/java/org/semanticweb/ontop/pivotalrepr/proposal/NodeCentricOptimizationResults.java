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
    Optional<QueryNode> getOptionalNewNode();

    /**
     * TODO: explain
     */
    Optional<QueryNode> getOptionalNextSibling();

    /**
     * TODO: explain
     */
    Optional<QueryNode> getOptionalParentNode();
}
