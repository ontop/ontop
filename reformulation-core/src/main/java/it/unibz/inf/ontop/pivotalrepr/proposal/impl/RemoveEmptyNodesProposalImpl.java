package it.unibz.inf.ontop.pivotalrepr.proposal.impl;

import it.unibz.inf.ontop.pivotalrepr.QueryNode;
import it.unibz.inf.ontop.pivotalrepr.proposal.RemoveEmptyNodesProposal;

public class RemoveEmptyNodesProposalImpl<N extends QueryNode> implements RemoveEmptyNodesProposal<N> {
    private final N focusNode;

    public RemoveEmptyNodesProposalImpl(N focusNode) {
        this.focusNode = focusNode;
    }

    @Override
    public N getFocusNode() {
        return focusNode;
    }
}
