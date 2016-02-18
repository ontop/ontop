package unibz.inf.ontop.pivotalrepr.proposal.impl;

import unibz.inf.ontop.pivotalrepr.InnerJoinNode;
import unibz.inf.ontop.pivotalrepr.proposal.InnerJoinOptimizationProposal;

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
