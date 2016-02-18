package unibz.inf.ontop.pivotalrepr;


import unibz.inf.ontop.executor.ProposalExecutor;
import unibz.inf.ontop.pivotalrepr.proposal.InvalidQueryOptimizationProposalException;
import unibz.inf.ontop.pivotalrepr.proposal.ProposalResults;
import unibz.inf.ontop.pivotalrepr.proposal.QueryOptimizationProposal;

public interface StandardProposalExecutor<P extends QueryOptimizationProposal<R>, R extends ProposalResults>
        extends ProposalExecutor<P, R> {

    R apply (P proposal, IntermediateQuery inputQuery) throws InvalidQueryOptimizationProposalException;

}
