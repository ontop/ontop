package it.unibz.inf.ontop.pivotalrepr.proposal.impl;

import it.unibz.inf.ontop.model.ImmutableSubstitution;
import it.unibz.inf.ontop.model.ImmutableTerm;
import it.unibz.inf.ontop.pivotalrepr.QueryNode;
import it.unibz.inf.ontop.pivotalrepr.proposal.SubstitutionUpPropagationProposal;


public class SubstitutionUpPropagationProposalImpl<N extends QueryNode> implements SubstitutionUpPropagationProposal<N> {

    private final N focusNode;
    private final ImmutableSubstitution<? extends ImmutableTerm> ascendingSubstitution;

    public SubstitutionUpPropagationProposalImpl(N focusNode,
                                                 ImmutableSubstitution<? extends ImmutableTerm> ascendingSubstitution) {
        this.focusNode = focusNode;
        this.ascendingSubstitution = ascendingSubstitution;
    }

    @Override
    public ImmutableSubstitution<? extends ImmutableTerm> getAscendingSubstitution() {
        return ascendingSubstitution;
    }

    @Override
    public N getFocusNode() {
        return focusNode;
    }
}
