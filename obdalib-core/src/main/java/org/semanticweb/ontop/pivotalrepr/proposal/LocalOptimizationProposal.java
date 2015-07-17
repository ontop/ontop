package org.semanticweb.ontop.pivotalrepr.proposal;

import com.google.common.base.Optional;
import org.semanticweb.ontop.pivotalrepr.QueryNode;

/**
 * TODO: develop
 */
public interface LocalOptimizationProposal {

    /**
     * Returns a QueryNode if it makes sense for the concrete proposal
     * to return such one.
     */
    Optional<QueryNode> apply() throws InvalidLocalOptimizationProposalException;
}
