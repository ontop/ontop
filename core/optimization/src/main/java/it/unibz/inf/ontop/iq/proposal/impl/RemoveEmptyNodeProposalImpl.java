package it.unibz.inf.ontop.iq.proposal.impl;

import it.unibz.inf.ontop.iq.node.EmptyNode;
import it.unibz.inf.ontop.iq.IntermediateQuery;
import it.unibz.inf.ontop.iq.proposal.NodeTracker;
import it.unibz.inf.ontop.iq.proposal.RemoveEmptyNodeProposal;

import java.util.Optional;

public class RemoveEmptyNodeProposalImpl implements RemoveEmptyNodeProposal {
    private final EmptyNode focusNode;
    private final Optional<NodeTracker> predefinedTracker;
    private final boolean isKeepingTrackOfAncestors;

    public RemoveEmptyNodeProposalImpl(EmptyNode emptyNode, boolean keepTrackOfAncestors) {
        this.focusNode = emptyNode;
        this.predefinedTracker = Optional.empty();
        this.isKeepingTrackOfAncestors = keepTrackOfAncestors;
    }

    public RemoveEmptyNodeProposalImpl(EmptyNode emptyNode, NodeTracker tracker) {
        this.focusNode = emptyNode;
        this.predefinedTracker = Optional.of(tracker);
        this.isKeepingTrackOfAncestors = true;
    }

    @Override
    public EmptyNode getFocusNode() {
        return focusNode;
    }

    @Override
    public Optional<NodeTracker> getOptionalTracker(IntermediateQuery query) {
        return predefinedTracker
                .map(Optional::of)
                .orElseGet(() ->
                    isKeepingTrackOfAncestors
                            ? Optional.of(new NodeTrackerImpl())
                            : Optional.empty());
    }
}
