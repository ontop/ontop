package org.semanticweb.ontop.pivotalrepr.proposal.impl;

import org.semanticweb.ontop.pivotalrepr.InnerJoinNode;
import org.semanticweb.ontop.pivotalrepr.proposal.InnerJoinOptimizationProposal;

public class InnerJoinOptimizationProposalImpl implements InnerJoinOptimizationProposal {
    private final InnerJoinNode topJoinNode;

    public InnerJoinOptimizationProposalImpl(InnerJoinNode topJoinNode) {
        this.topJoinNode = topJoinNode;
    }

    @Override
    public InnerJoinNode getTopJoinNode() {
        return topJoinNode;
    }
}
