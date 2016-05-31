package it.unibz.inf.ontop.pivotalrepr;

import it.unibz.inf.ontop.pivotalrepr.proposal.QueryOptimizationProposal;
import it.unibz.inf.ontop.pivotalrepr.proposal.ProposalResults;

public interface UnionLiftProposal extends QueryOptimizationProposal<ProposalResults> {

    UnionNode getUnionNode();

}
