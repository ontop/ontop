package it.unibz.inf.ontop.pivotalrepr;

import it.unibz.inf.ontop.executor.InternalProposalExecutor;
import it.unibz.inf.ontop.pivotalrepr.proposal.QueryOptimizationProposal;

import java.util.Optional;

/**
 * TODO: explain and find a better name
 *
 */
public interface OptimizationConfiguration {

    Optional<Class<? extends InternalProposalExecutor>> getProposalExecutorInterface(
            Class<? extends QueryOptimizationProposal> proposalClass);
}
