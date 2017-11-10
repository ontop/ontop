package it.unibz.inf.ontop.model.term.impl;

import com.google.common.collect.ImmutableList;
import it.unibz.inf.ontop.model.term.*;


import java.util.Collection;
import java.util.List;

public class GroundTermTools {

    public static class NonGroundTermException extends RuntimeException {
        protected  NonGroundTermException(String message) {
            super(message);
        }
    }


    public static ImmutableList<GroundTerm> castIntoGroundTerms(List<? extends Term> terms)
            throws NonGroundTermException {
        ImmutableList.Builder<GroundTerm> termBuilder = ImmutableList.builder();
        for (Term term : terms) {
            termBuilder.add(castIntoGroundTerm(term));
        }

        return termBuilder.build();
    }

    public static GroundTerm castIntoGroundTerm(Term term) throws NonGroundTermException{
        if (term instanceof GroundTerm)
            return (GroundTerm) term;

        if (term instanceof Function) {
            Function functionalTerm = (Function) term;
            // Recursive
            return new GroundFunctionalTermImpl(functionalTerm.getFunctionSymbol(),
                    castIntoGroundTerms(functionalTerm.getTerms()));
        }

        throw new NonGroundTermException(term + " is not a ground term");
    }

    /**
     * Returns true if is a ground term (even if it is not explicitly typed as such).
     */
    public static boolean isGroundTerm(Term term) {
        if (term instanceof ImmutableTerm)
            return ((ImmutableTerm) term).isGround();

        if (term instanceof Function) {
            return ((Function)term).getVariables().isEmpty();
        }

        return false;
    }

    public static boolean areGroundTerms(Collection<? extends ImmutableTerm> terms) {
        for (ImmutableTerm term : terms) {
            if (!term.isGround()) {
                return false;
            }
        }
        return true;
    }

    public static void checkNonGroundTermConstraint(NonGroundFunctionalTerm term) throws IllegalArgumentException {
        if (term.getVariables().isEmpty()) {
            throw new IllegalArgumentException("A NonGroundFunctionalTerm must contain at least one variable: " + term);
        }
    }
}
