package org.semanticweb.ontop.owlrefplatform.core.basicoperations;

import com.google.common.collect.ImmutableMap;
import org.semanticweb.ontop.model.Term;
import org.semanticweb.ontop.model.impl.VariableImpl;

import java.util.Map;

/**
 * Temporary class (before refactoring of Unifier and Substitution).
 *
 * TODO: rename it Substitution (complies to the unification theory. Is a function).
 */
public class Substitutions {

    /**
     * Exception occurring when computing a substitution.
     */
    public static class SubstitutionException extends Exception {
    }

    /**
     * Happens when union of two substitution functions is inconsistent
     * (it does not produce a new substitution function).
     *
     */
    public static class SubstitutionUnionException extends SubstitutionException {
    }

    /**
     * Computes the union of two substitution functions.
     *
     * This union is a little bit that the plain union of their corresponding set: it
     * fails if some "atomic substitutions" are conflicting. --> Throws a SubstitutionUnionException.
     *
     * Returns a new substitution function.
     *
     * TODO: In the future, should manipulate  and return (refactored) Substitution *functions*, not unifiers.
     * Why? Because (some of) these are not valid unifiers according to its formal definition.
     *
     */
    public static Unifier union(Unifier substitutionFct1, Unifier substitutionFct2) throws SubstitutionUnionException {
        ImmutableMap.Builder<VariableImpl, Term> substitutionMapBuilder = ImmutableMap.builder();
        substitutionMapBuilder.putAll(substitutionFct1.toMap());

        for (Map.Entry<VariableImpl, Term> newEntry : substitutionFct2.toMap().entrySet()) {

            /**
             * Checks if the variable is part of the domain
             * of the first substitution function.
             *
             * If not, adds the entry.
             * Otherwise, throws a exception if the two entries
             * are not equivalent (remind that a substitution must be a function).
             */
            Term substitutionTerm = substitutionFct1.get(newEntry.getKey());
            if (substitutionTerm == null) {
                substitutionMapBuilder.put(newEntry);
            }
            /**
             * TODO: consider merging some non-equal but compatible terms.
             * Use case in mind: type promotion.
             *
             */
            else if (!substitutionTerm.equals(newEntry.getValue())) {
                throw new SubstitutionUnionException();
            }
        }

        Unifier newUnifier = new Unifier(substitutionMapBuilder.build());
        return newUnifier;
    }
}
