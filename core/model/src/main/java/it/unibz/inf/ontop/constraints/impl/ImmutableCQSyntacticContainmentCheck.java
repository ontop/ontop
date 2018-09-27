package it.unibz.inf.ontop.constraints.impl;

import it.unibz.inf.ontop.constraints.ImmutableCQ;
import it.unibz.inf.ontop.constraints.ImmutableCQContainmentCheck;

public class ImmutableCQSyntacticContainmentCheck implements ImmutableCQContainmentCheck {

    /**
     * Check if query cq1 is contained in cq2, syntactically. That is, if the
     * head of cq1 and cq2 are equal and each atom in cq2 is also in the body of cq1
     */
    @Override
    public boolean isContainedIn(ImmutableCQ cq1, ImmutableCQ cq2) {
        return cq2.getAnswerVariables().equals(cq1.getAnswerVariables())
                && !cq2.getAtoms().stream().anyMatch(a -> !cq1.getAtoms().contains(a));
    }

}
