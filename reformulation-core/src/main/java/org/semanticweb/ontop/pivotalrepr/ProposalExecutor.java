package org.semanticweb.ontop.pivotalrepr;


import org.semanticweb.ontop.pivotalrepr.proposal.QueryOptimizationProposal;

public interface ProposalExecutor<T extends QueryOptimizationProposal> {

    IntermediateQuery apply (T proposal, IntermediateQuery inputQuery);

}
