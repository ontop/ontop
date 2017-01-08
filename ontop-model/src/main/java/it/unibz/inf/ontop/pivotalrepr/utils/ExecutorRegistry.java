package it.unibz.inf.ontop.pivotalrepr.utils;

import it.unibz.inf.ontop.executor.InternalProposalExecutor;
import it.unibz.inf.ontop.pivotalrepr.proposal.ProposalResults;
import it.unibz.inf.ontop.pivotalrepr.proposal.QueryOptimizationProposal;

/**
 * TODO: explain
 */
public interface ExecutorRegistry {

    <R extends ProposalResults, P extends QueryOptimizationProposal<R>>
    InternalProposalExecutor<P, R> getExecutor(P proposal);

}
