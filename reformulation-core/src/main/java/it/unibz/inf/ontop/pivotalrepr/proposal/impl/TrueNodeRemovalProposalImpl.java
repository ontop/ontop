package it.unibz.inf.ontop.pivotalrepr.proposal.impl;

import it.unibz.inf.ontop.pivotalrepr.TrueNode;
import it.unibz.inf.ontop.pivotalrepr.proposal.TrueNodeRemovalProposal;

/**
 * Created by jcorman on 06/10/16.
 */
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
