package it.unibz.inf.ontop.iq.proposal.impl;

import it.unibz.inf.ontop.iq.node.LeftJoinNode;
import it.unibz.inf.ontop.iq.proposal.LeftJoinOptimizationProposal;

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
