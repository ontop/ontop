package it.unibz.inf.ontop.pivotalrepr;


import it.unibz.inf.ontop.executor.ProposalExecutor;
import it.unibz.inf.ontop.pivotalrepr.proposal.InvalidQueryOptimizationProposalException;
import it.unibz.inf.ontop.pivotalrepr.proposal.ProposalResults;
import it.unibz.inf.ontop.pivotalrepr.proposal.QueryOptimizationProposal;

public interface StandardProposalExecutor<P extends QueryOptimizationProposal<R>, R extends ProposalResults>
        extends ProposalExecutor<P, R> {

    R apply (P proposal, IntermediateQuery inputQuery) throws InvalidQueryOptimizationProposalException;

}
