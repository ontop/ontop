package it.unibz.inf.ontop.substitution.impl;

/*
 * #%L
 * ontop-reformulation-core
 * %%
 * Copyright (C) 2009 - 2014 Free University of Bozen-Bolzano
 * %%
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 * 
 *      http://www.apache.org/licenses/LICENSE-2.0
 * 
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 * #L%
 */

/*
 * Note: The the unifier does not distinguish between undistinguished variables
 * i.e.when we have an atom A(#,#) which should be unified with B(b,c) the
 * unifier willreturn two thetas #/b, #/c. So far so good but this will lead to
 * problems whenapplying the thetas because there is no distinction between
 * #-variables, sothe first theta is applied to all #-variables and the rest is
 * ignored.In order to avoid problems one can enumerate the undistinguished
 * variables ie. A(#1,#2)
 */

import com.google.common.collect.ImmutableMap;
import com.google.inject.Singleton;
import it.unibz.inf.ontop.model.term.*;
import it.unibz.inf.ontop.model.term.impl.FunctionalTermImpl;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * A Class that provides general utilities related to unification, of terms and
 * atoms.
 * <p/>
 *
 * @author mariano
 */
@Singleton
public class UnifierUtilities {

    /**
     * Computes the Most General Unifier (MGU) for two n-ary atoms.
     * <p/>
     * IMPORTANT: Function terms are supported as long as they are not nested.
     * <p/>
     * IMPORTANT: handling of AnonymousVariables is questionable --
     * much is left to UnifierUtilities.apply (and only one version handles them)
     *
     * @param first
     * @param second
     * @return the substitution corresponding to this unification.
     */
    public Map<Variable, Term> getMGU(Function first, Function second) {
        Map<Variable,Term> mgu = new HashMap<>();
        if (composeFunctions(mgu, first, second))
            return mgu;
        return null;
    }

    /***
     * Creates a unifier (singleton substitution) out of term1 and term2.
     *
     * Then, composes the current substitution with this unifier.
     * (remind that composition is not commutative).
     *
     *
     * Note that this Substitution object will be modified in this process.
     *
     * The operation is as follows
     *
     * {x/y, m/y} composed with (y,z) is equal to {x/z, m/z, y/z}
     *
     * @param term1
     * @param term2
     * @return true if the substitution exists (false if it does not)
     */
    private static boolean composeTerms(Map<Variable, Term> map, Term term1, Term term2) {

        /**
         * Special case: unification of two functional terms (possibly recursive)
         */
        if ((term1 instanceof Function) && (term2 instanceof Function)) {
            return composeFunctions(map, (Function) term1, (Function) term2);
        }

        ImmutableMap<Variable, Term> s = createUnifier(term1, term2);

        // Rejected substitution (conflicts)
        if (s == null)
            return false;

        // Neutral substitution
        if (s.isEmpty())
            return true;

        // Not neutral, not null --> should be a singleton.
        Map.Entry<Variable, Term> substitution = s.entrySet().iterator().next();

        List<Variable> forRemoval = new ArrayList<>();
        for (Map.Entry<Variable,Term> entry : map.entrySet()) {
            Variable v = entry.getKey();
            Term t = entry.getValue();
            if (substitution.getKey().equals(t)) {
                if (v.equals(substitution.getValue())) {
                    // The substitution for the current variable has become
                    // trivial, e.g., x/x with the current composition. We
                    // remove it to keep only a non-trivial unifier
                    forRemoval.add(v);
                }
                else
                    map.put(v, substitution.getValue());

            }
            else if (t instanceof FunctionalTermImpl) {
                FunctionalTermImpl fclone = (FunctionalTermImpl)t.clone();
                boolean innerchanges = applySingletonSubstitution(fclone, substitution);
                if (innerchanges)
                    map.put(v, fclone);
            }
        }
        map.keySet().removeAll(forRemoval);
        map.put(substitution.getKey(), substitution.getValue());
        return true;
    }

    /**
     * May alter the functionalTerm (mutable style)
     *
     * Recursive
     */
    private static boolean applySingletonSubstitution(Function functionalTerm, Map.Entry<Variable, Term> substitution) {
        List<Term> innerTerms = functionalTerm.getTerms();
        boolean innerchanges = false;
        // TODO this ways of changing inner terms in functions is not
        // optimal, modify

        for (int i = 0; i < innerTerms.size(); i++) {
            Term innerTerm = innerTerms.get(i);

            if (innerTerm instanceof Function) {
                // Recursive call
                boolean newChange = applySingletonSubstitution((Function) innerTerm, substitution);
                innerchanges = innerchanges || newChange;
            }
            else if (substitution.getKey().equals(innerTerm)) {
                functionalTerm.getTerms().set(i, substitution.getValue());
                innerchanges = true;
            }
        }
        return innerchanges;
    }


    public static boolean composeFunctions(Map<Variable, Term> map, Function first, Function second) {
        // Basic case: if predicates are different or their arity is different, then no unifier
        if ((first.getArity() != second.getArity()
                || !first.getFunctionSymbol().equals(second.getFunctionSymbol()))) {
            return false;
        }

        Function firstAtom = (Function) first.clone();
        Function secondAtom = (Function) second.clone();

        int arity = first.getArity();

        // Computing the disagreement set
        for (int termidx = 0; termidx < arity; termidx++) {

            // Checking if there are already substitutions calculated for the
            // current terms. If there are any, then we have to take the
            // substituted terms instead of the original ones.

            Term term1 = firstAtom.getTerm(termidx);
            Term term2 = secondAtom.getTerm(termidx);

            if (!composeTerms(map, term1, term2))
                return false;

            // Applying the newly computed substitution to the 'replacement' of
            // the existing substitutions
            applySubstitution(firstAtom, map, termidx + 1);
            applySubstitution(secondAtom, map, termidx + 1);
        }

        return true;
    }

    /***
     * Computes the unifier that makes two terms equal.
     *
     * @param term1
     * @param term2
     * @return
     */
    private static ImmutableMap<Variable, Term> createUnifier(Term term1, Term term2) {
        if (term1 instanceof Variable || term2 instanceof Variable) {
            // arranging the terms so that the first is always a variable
            Variable v;
            Term t;
            if (term1 instanceof Variable) {
                v = (Variable)term1;
                t = term2;
            }
            else {
                v = (Variable)term2;
                t = term1;
            }

            // Undistinguished variables do not need a substitution, the unifier knows about this
            if  (t instanceof Variable) {
                if (v.equals(t))
                    return ImmutableMap.of();
                else
                    return ImmutableMap.of(v, t);
            }
            else if (t instanceof Constant) {
                return ImmutableMap.of(v, t);
            }
            else if (t instanceof Function) {
                Function fterm = (Function) t;
                if (fterm.containsTerm(v))
                    return null;
                else
                    return ImmutableMap.of(v, t);
            }
        }
        else {
            // neither is a variable, impossible to unify unless the two terms are
            // equal, in which case there the substitution is empty
            if (term1.equals(term2))
                return ImmutableMap.of();
            else
                return null;
        }
        // this should never happen
        throw new RuntimeException("Exception comparing two terms, unknown term class. Terms: "
                + term1 + ", " + term2 + " Classes: " + term1.getClass()
                + ", " + term2.getClass());
    }

    /**
     * Applies the substitution to all the terms in the list. Note that this
     * will not clone the list or the terms inside the list.
     *
     * @param atom
     * @param map
     */

    private static void applySubstitution(Function atom, Map<Variable, Term> map, int fromIndex) {
        List<Term> terms = atom.getTerms();
        for (int i = fromIndex; i < terms.size(); i++) {
            Term t = terms.get(i);
            if (t instanceof Variable) {
                Term replacement = map.get(t);
                if (replacement != null)
                    terms.set(i, replacement);
            }
            else if (t instanceof Function) {
                applySubstitution((Function) t, map, 0);
            }
        }
    }

}
