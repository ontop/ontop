package org.semanticweb.ontop.pivotalrepr;


import org.semanticweb.ontop.executor.ProposalExecutor;
import org.semanticweb.ontop.pivotalrepr.proposal.InvalidQueryOptimizationProposalException;
import org.semanticweb.ontop.pivotalrepr.proposal.ProposalResults;
import org.semanticweb.ontop.pivotalrepr.proposal.QueryOptimizationProposal;

public interface StandardProposalExecutor<P extends QueryOptimizationProposal<R>, R extends ProposalResults>
        extends ProposalExecutor<P, R> {

    R apply (P proposal, IntermediateQuery inputQuery) throws InvalidQueryOptimizationProposalException;

}
