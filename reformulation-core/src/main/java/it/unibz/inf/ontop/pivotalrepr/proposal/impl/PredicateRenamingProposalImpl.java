package it.unibz.inf.ontop.pivotalrepr.proposal.impl;

import it.unibz.inf.ontop.model.AtomPredicate;
import it.unibz.inf.ontop.pivotalrepr.proposal.PredicateRenamingProposal;

public class PredicateRenamingProposalImpl implements PredicateRenamingProposal {

    private final AtomPredicate formerPredicate;
    private final AtomPredicate newPredicate;

    public PredicateRenamingProposalImpl(AtomPredicate formerPredicate, AtomPredicate newPredicate) {
        this.formerPredicate = formerPredicate;
        this.newPredicate = newPredicate;
    }

    @Override
    public AtomPredicate getFormerPredicate() {
        return formerPredicate;
    }

    @Override
    public AtomPredicate getNewPredicate() {
        return newPredicate;
    }
}
