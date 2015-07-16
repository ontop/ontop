package org.semanticweb.ontop.pivotalrepr;

/**
 * TODO: develop
 */
public interface LocalOptimizationProposal {

    QueryNode apply() throws InvalidLocalOptimizationProposalException;
}
