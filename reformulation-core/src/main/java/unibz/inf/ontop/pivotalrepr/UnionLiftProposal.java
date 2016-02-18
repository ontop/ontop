package unibz.inf.ontop.pivotalrepr;

import unibz.inf.ontop.pivotalrepr.proposal.QueryOptimizationProposal;
import unibz.inf.ontop.pivotalrepr.proposal.ProposalResults;

public interface UnionLiftProposal extends QueryOptimizationProposal<ProposalResults> {

    UnionNode getUnionNode();

}
