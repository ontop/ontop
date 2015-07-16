package org.semanticweb.ontop.pivotalrepr;

import com.google.common.base.Optional;

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
