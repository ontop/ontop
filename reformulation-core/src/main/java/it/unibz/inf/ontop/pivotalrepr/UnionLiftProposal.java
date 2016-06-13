package it.unibz.inf.ontop.pivotalrepr;

import it.unibz.inf.ontop.pivotalrepr.proposal.ProposalResults;
import it.unibz.inf.ontop.pivotalrepr.proposal.QueryOptimizationProposal;

public interface UnionLiftProposal extends QueryOptimizationProposal<ProposalResults> {

    UnionNode getUnionNode();

}
