package it.unibz.inf.ontop.answering.reformulation.rewriting.impl;

import it.unibz.inf.ontop.answering.reformulation.rewriting.ImmutableCQ;
import it.unibz.inf.ontop.answering.reformulation.rewriting.ImmutableCQContainmentCheck;

public class ImmutableCQSyntacticContainmentCheck implements ImmutableCQContainmentCheck {
    /**
     * Check if query cq1 is contained in cq2, syntactically. That is, if the
     * head of cq1 and cq2 are equal according to toString().equals and each
     * atom in cq2 is also in the body of cq1 (also by means of toString().equals().
     */

    @Override
    public boolean isContainedIn(ImmutableCQ cq1, ImmutableCQ cq2) {
        return cq2.getAnswerVariables().equals(cq1.getAnswerVariables())
                && !cq2.getAtoms().stream().anyMatch(a -> !cq1.getAtoms().contains(a));
    }

}
