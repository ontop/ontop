package it.unibz.inf.ontop.pivotalrepr.proposal.impl;

import it.unibz.inf.ontop.pivotalrepr.EmptyNode;
import it.unibz.inf.ontop.pivotalrepr.proposal.RemoveEmptyNodesProposal;

public class RemoveEmptyNodesProposalImpl implements RemoveEmptyNodesProposal {
    private final EmptyNode focusNode;

    public RemoveEmptyNodesProposalImpl(EmptyNode emptyNode) {
        this.focusNode = emptyNode;
    }

    @Override
    public EmptyNode getFocusNode() {
        return focusNode;
    }
}
