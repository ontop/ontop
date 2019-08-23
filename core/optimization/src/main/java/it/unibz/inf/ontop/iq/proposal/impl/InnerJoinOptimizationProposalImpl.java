package it.unibz.inf.ontop.iq.proposal.impl;

import it.unibz.inf.ontop.iq.node.InnerJoinNode;
import it.unibz.inf.ontop.iq.proposal.InnerJoinOptimizationProposal;

public class InnerJoinOptimizationProposalImpl implements InnerJoinOptimizationProposal {
    private final InnerJoinNode topJoinNode;

    public InnerJoinOptimizationProposalImpl(InnerJoinNode topJoinNode) {
        this.topJoinNode = topJoinNode;
    }

    @Override
    public InnerJoinNode getFocusNode() {
        return topJoinNode;
    }
}
