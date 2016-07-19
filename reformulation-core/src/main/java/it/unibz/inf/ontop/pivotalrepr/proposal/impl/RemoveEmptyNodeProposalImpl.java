package it.unibz.inf.ontop.pivotalrepr.proposal.impl;

import it.unibz.inf.ontop.pivotalrepr.EmptyNode;
import it.unibz.inf.ontop.pivotalrepr.IntermediateQuery;
import it.unibz.inf.ontop.pivotalrepr.proposal.AncestryTracker;
import it.unibz.inf.ontop.pivotalrepr.proposal.RemoveEmptyNodeProposal;

import javax.sound.midi.Track;
import java.util.Optional;

public class RemoveEmptyNodeProposalImpl implements RemoveEmptyNodeProposal {
    private final EmptyNode focusNode;
    private final Optional<AncestryTracker> predefinedTracker;
    private final boolean isKeepingTrackOfAncestors;

    public RemoveEmptyNodeProposalImpl(EmptyNode emptyNode, boolean keepTrackOfAncestors) {
        this.focusNode = emptyNode;
        this.predefinedTracker = Optional.empty();
        this.isKeepingTrackOfAncestors = keepTrackOfAncestors;
    }

    public RemoveEmptyNodeProposalImpl(EmptyNode emptyNode, AncestryTracker tracker) {
        this.focusNode = emptyNode;
        this.predefinedTracker = Optional.of(tracker);
        this.isKeepingTrackOfAncestors = true;
    }

    @Override
    public EmptyNode getFocusNode() {
        return focusNode;
    }

    @Override
    public Optional<AncestryTracker> getOptionalTracker(IntermediateQuery query) {
        return predefinedTracker
                .map(Optional::of)
                .orElseGet(() ->
                    isKeepingTrackOfAncestors
                            ? Optional.of(new AncestryTrackerImpl(query, focusNode))
                            : Optional.empty());
    }
}
