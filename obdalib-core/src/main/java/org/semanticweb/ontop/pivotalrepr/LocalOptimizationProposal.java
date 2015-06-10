package org.semanticweb.ontop.pivotalrepr;

/**
 * TODO: develop
 */
public interface LocalOptimizationProposal {

    /**
     * Query node on which to apply the optimization proposal.
     */
    QueryNode getQueryNode();

    IntermediateQuery getTargetQuery();

    QueryNode apply() throws InvalidLocalOptimizationProposalException;
}
