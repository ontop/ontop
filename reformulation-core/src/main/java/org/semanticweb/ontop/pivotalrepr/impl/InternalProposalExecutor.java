package org.semanticweb.ontop.pivotalrepr.impl;

import org.semanticweb.ontop.executor.ProposalExecutor;
import org.semanticweb.ontop.pivotalrepr.proposal.InvalidQueryOptimizationProposalException;
import org.semanticweb.ontop.pivotalrepr.proposal.QueryOptimizationProposal;

/**
 * TODO: explain
 *
 * InternalProposalExecutor are expected to manipulate directly a QueryTreeComponent
 *
 */
public interface InternalProposalExecutor<T extends QueryOptimizationProposal> extends ProposalExecutor<T> {

    /**
     * TODO: explain
     */
    public void apply(T proposal, QueryTreeComponent treeComponent) throws InvalidQueryOptimizationProposalException;

}
