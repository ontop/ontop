package org.semanticweb.ontop.pivotalrepr;


import org.semanticweb.ontop.executor.ProposalExecutor;
import org.semanticweb.ontop.pivotalrepr.proposal.InvalidQueryOptimizationProposalException;
import org.semanticweb.ontop.pivotalrepr.proposal.QueryOptimizationProposal;

public interface StandardProposalExecutor<T extends QueryOptimizationProposal> extends ProposalExecutor<T> {

    IntermediateQuery apply (T proposal, IntermediateQuery inputQuery) throws InvalidQueryOptimizationProposalException;

}
