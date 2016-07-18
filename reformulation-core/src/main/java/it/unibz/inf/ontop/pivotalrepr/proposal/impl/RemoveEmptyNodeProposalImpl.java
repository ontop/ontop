package it.unibz.inf.ontop.pivotalrepr.proposal.impl;

import it.unibz.inf.ontop.pivotalrepr.EmptyNode;
import it.unibz.inf.ontop.pivotalrepr.proposal.RemoveEmptyNodeProposal;

public class RemoveEmptyNodeProposalImpl implements RemoveEmptyNodeProposal {
    private final EmptyNode focusNode;
    private final boolean isKeepingTrackOfAncestors;

    public RemoveEmptyNodeProposalImpl(EmptyNode emptyNode, boolean keepTrackOfAncestors) {
        this.focusNode = emptyNode;
        this.isKeepingTrackOfAncestors = keepTrackOfAncestors;
    }

    @Override
    public EmptyNode getFocusNode() {
        return focusNode;
    }

    @Override
    public boolean isKeepingTrackOfAncestors() {
        return isKeepingTrackOfAncestors;
    }
}
