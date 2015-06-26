package org.semanticweb.ontop.model.impl;

import org.semanticweb.ontop.model.*;

public class ImmutabilityTools {

    /**
     * In case the term is functional, creates an immutable copy of it.
     */
    public static ImmutableTerm convertIntoImmutableTerm(Term term) {
        if (term instanceof Function) {
            if (term instanceof ImmutableFunctionalTerm) {
                return (ImmutableTerm) term;
            } else {
                Function functionalTerm = (Function) term;
                return new ImmutableFunctionalTermImpl(functionalTerm);
            }
        }
        /**
         * Other terms (constant and variable) are immutable.
         */
        return (ImmutableTerm) term;
    }

    public static ImmutableBooleanExpression convertIntoImmutableBooleanExpression(BooleanExpression expression) {
        return new ImmutableBooleanExpressionImpl(expression);
    }
}
