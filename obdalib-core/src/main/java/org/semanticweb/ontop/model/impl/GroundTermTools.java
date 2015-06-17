package org.semanticweb.ontop.model.impl;

import com.google.common.collect.ImmutableList;
import org.semanticweb.ontop.model.Function;
import org.semanticweb.ontop.model.GroundTerm;
import org.semanticweb.ontop.model.Term;
import org.semanticweb.ontop.model.Variable;

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
        if (term instanceof GroundTerm)
            return true;

        if (term instanceof Function) {
            return term.getReferencedVariables().isEmpty();
        }

        return false;
    }

}
