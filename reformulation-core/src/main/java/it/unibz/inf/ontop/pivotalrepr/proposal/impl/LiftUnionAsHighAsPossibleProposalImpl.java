package it.unibz.inf.ontop.pivotalrepr.proposal.impl;

import it.unibz.inf.ontop.pivotalrepr.UnionNode;
import it.unibz.inf.ontop.pivotalrepr.proposal.LiftUnionAsHighAsPossibleProposal;

public class LiftUnionAsHighAsPossibleProposalImpl implements LiftUnionAsHighAsPossibleProposal {

    private final UnionNode unionNode;

    public LiftUnionAsHighAsPossibleProposalImpl(UnionNode unionNode) {
        this.unionNode = unionNode;
    }

    @Override
    public UnionNode getUnionNode() {
        return unionNode;
    }

}
