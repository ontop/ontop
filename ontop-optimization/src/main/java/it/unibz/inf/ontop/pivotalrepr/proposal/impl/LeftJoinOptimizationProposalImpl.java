package it.unibz.inf.ontop.pivotalrepr.proposal.impl;

import it.unibz.inf.ontop.pivotalrepr.LeftJoinNode;
import it.unibz.inf.ontop.pivotalrepr.proposal.LeftJoinOptimizationProposal;

public class LeftJoinOptimizationProposalImpl implements LeftJoinOptimizationProposal {
    private final LeftJoinNode leftJoinNode;

    public LeftJoinOptimizationProposalImpl(LeftJoinNode leftJoinNode) {
        this.leftJoinNode = leftJoinNode;
    }

    @Override
    public LeftJoinNode getFocusNode() {
        return leftJoinNode;
    }
}
