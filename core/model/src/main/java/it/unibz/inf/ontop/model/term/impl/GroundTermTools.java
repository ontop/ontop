package it.unibz.inf.ontop.model.term.impl;

import com.google.common.collect.ImmutableList;
import it.unibz.inf.ontop.model.term.*;
import it.unibz.inf.ontop.model.term.functionsymbol.FunctionSymbol;


import java.util.Collection;
import java.util.List;
import java.util.Optional;

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
            FunctionSymbol functionSymbol = Optional.of(functionalTerm.getFunctionSymbol())
                    .filter(p -> p instanceof FunctionSymbol)
                    .map(p -> (FunctionSymbol)p)
                    .orElseThrow(() -> new NonGroundTermException(term + "is not using a function symbol but a"
                            + functionalTerm.getFunctionSymbol().getClass()));

            return new GroundFunctionalTermImpl(castIntoGroundTerms(functionalTerm.getTerms()), functionSymbol);
        }

        throw new NonGroundTermException(term + " is not a ground term");
    }

    /**
     * Returns true if is a ground term (even if it is not explicitly typed as such).
     */
    public static boolean isGroundTerm(Term term) {
        if (term instanceof Function) {
            return ((Function)term).getVariables().isEmpty();
        }
        return term instanceof Constant;
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
