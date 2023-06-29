package it.unibz.inf.ontop.model.term.impl;

import it.unibz.inf.ontop.model.term.*;

public class GroundTermTools {

    public static void checkNonGroundTermConstraint(NonGroundFunctionalTerm term) throws IllegalArgumentException {
        if (term.getVariables().isEmpty()) {
            throw new IllegalArgumentException("A NonGroundFunctionalTerm must contain at least one variable: " + term);
        }
    }
}
