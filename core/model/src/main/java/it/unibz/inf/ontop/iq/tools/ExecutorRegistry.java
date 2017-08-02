package it.unibz.inf.ontop.iq.tools;

import it.unibz.inf.ontop.iq.executor.ProposalExecutor;
import it.unibz.inf.ontop.iq.proposal.ProposalResults;
import it.unibz.inf.ontop.iq.proposal.QueryOptimizationProposal;

/**
 * TODO: explain
 */
public interface ExecutorRegistry {

    <R extends ProposalResults, P extends QueryOptimizationProposal<R>>
    ProposalExecutor<P, R> getExecutor(P proposal);

}
