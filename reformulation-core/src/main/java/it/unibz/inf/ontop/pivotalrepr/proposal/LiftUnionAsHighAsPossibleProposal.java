package it.unibz.inf.ontop.pivotalrepr.proposal;

import it.unibz.inf.ontop.pivotalrepr.UnionNode;

public interface LiftUnionAsHighAsPossibleProposal extends QueryOptimizationProposal<ProposalResults> {

    UnionNode getUnionNode();

}
