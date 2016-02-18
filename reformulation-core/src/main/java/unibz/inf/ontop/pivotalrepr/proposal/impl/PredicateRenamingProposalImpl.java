package unibz.inf.ontop.pivotalrepr.proposal.impl;

import unibz.inf.ontop.model.AtomPredicate;
import unibz.inf.ontop.pivotalrepr.proposal.PredicateRenamingProposal;

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
