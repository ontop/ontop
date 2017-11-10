package it.unibz.inf.ontop.iq.proposal.impl;

import it.unibz.inf.ontop.iq.node.TrueNode;
import it.unibz.inf.ontop.iq.proposal.TrueNodeRemovalProposal;

public class TrueNodeRemovalProposalImpl implements TrueNodeRemovalProposal {

    private final TrueNode focusNode;

    public TrueNodeRemovalProposalImpl(TrueNode focusNode) {
        this.focusNode = focusNode;
    }

    @Override
    public TrueNode getFocusNode() {
        return focusNode;
    }
}
