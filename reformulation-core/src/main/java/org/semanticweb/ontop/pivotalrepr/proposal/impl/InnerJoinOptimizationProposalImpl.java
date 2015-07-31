package org.semanticweb.ontop.pivotalrepr.proposal.impl;

import org.semanticweb.ontop.pivotalrepr.InnerJoinNode;
import org.semanticweb.ontop.pivotalrepr.proposal.InnerJoinOptimizationProposal;
import org.semanticweb.ontop.pivotalrepr.proposal.NodeCentricOptimizationResults;
import org.semanticweb.ontop.pivotalrepr.proposal.ProposalResults;

public class InnerJoinOptimizationProposalImpl implements InnerJoinOptimizationProposal {
    private final InnerJoinNode topJoinNode;

    public InnerJoinOptimizationProposalImpl(InnerJoinNode topJoinNode) {
        this.topJoinNode = topJoinNode;
    }

    @Override
    public InnerJoinNode getTopJoinNode() {
        return topJoinNode;
    }

    @Override
    public NodeCentricOptimizationResults castResults(ProposalResults results) {
        return (NodeCentricOptimizationResults) results;
    }
}
