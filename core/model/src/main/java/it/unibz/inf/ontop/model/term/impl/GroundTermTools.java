package it.unibz.inf.ontop.model.term.impl;

import it.unibz.inf.ontop.model.term.*;


import java.util.*;

public class GroundTermTools {

    public static VariableOrGroundTerm convertIntoVariableOrGroundTerm(ImmutableTerm term) {
        if (term instanceof Variable) {
            return (Variable) term;
        }
        else if (term.isGround()) {
            return (GroundTerm) term;
        }
        else {
            throw new IllegalArgumentException("Not a variable nor a ground term: " + term);
        }
    }

    public static boolean areGroundTerms(Collection<? extends ImmutableTerm> terms) {
        return terms.stream().allMatch(ImmutableTerm::isGround);
    }

    public static void checkNonGroundTermConstraint(NonGroundFunctionalTerm term) throws IllegalArgumentException {
        if (term.getVariables().isEmpty()) {
            throw new IllegalArgumentException("A NonGroundFunctionalTerm must contain at least one variable: " + term);
        }
    }
}
