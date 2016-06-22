package it.unibz.inf.ontop.pivotalrepr.proposal;

import it.unibz.inf.ontop.pivotalrepr.UnionNode;
import it.unibz.inf.ontop.pivotalrepr.proposal.QueryOptimizationProposal;
import it.unibz.inf.ontop.pivotalrepr.proposal.ProposalResults;

public interface LiftUnionAsHighAsPossibleProposal extends QueryOptimizationProposal<ProposalResults> {

    UnionNode getUnionNode();

}
