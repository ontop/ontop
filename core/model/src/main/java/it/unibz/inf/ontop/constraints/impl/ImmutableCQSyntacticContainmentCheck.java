package it.unibz.inf.ontop.constraints.impl;

import it.unibz.inf.ontop.constraints.ImmutableCQ;
import it.unibz.inf.ontop.constraints.ImmutableCQContainmentCheck;
import it.unibz.inf.ontop.model.atom.AtomPredicate;

public class ImmutableCQSyntacticContainmentCheck<P extends AtomPredicate> implements ImmutableCQContainmentCheck<P> {

    /**
     * Check if query cq1 is contained in cq2, syntactically. That is, if the
     * head of cq1 and cq2 are equal and each atom in cq2 is also in the body of cq1
     */
    @Override
    public boolean isContainedIn(ImmutableCQ<P> cq1, ImmutableCQ<P> cq2) {
        return cq2.getAnswerVariables().equals(cq1.getAnswerVariables())
                && !cq2.getAtoms().stream().anyMatch(a -> !cq1.getAtoms().contains(a));
    }

}
