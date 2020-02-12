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

import com.google.common.collect.ImmutableList;
import com.google.common.collect.ImmutableMap;
import com.google.common.collect.Maps;
import com.google.inject.Inject;
import com.google.inject.Singleton;
import it.unibz.inf.ontop.model.term.*;
import org.mapdb.Fun;

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

    private final TermFactory termFactory;

    @Inject
    public UnifierUtilities(TermFactory termFactory) {
        this.termFactory = termFactory;
    }

    /**
     * Computes the Most General Unifier (MGU) for two n-ary atoms.
     *
     * @param args1
     * @param args2
     * @return the substitution corresponding to this unification.
     */
    public Map<Variable, Term> getMGU(ImmutableList<? extends ImmutableTerm> args1,ImmutableList<? extends ImmutableTerm> args2) {
        Map<Variable,Term> mgu = new HashMap<>();
        if (composeTerms(mgu,
                convertToMutableTerms(args1),
                convertToMutableTerms(args2)))
            return mgu;
        return null;
    }

    /**
     * This method takes a immutable term and convert it into an old mutable function.
     */

    private List<Term> convertToMutableTerms(ImmutableList<? extends ImmutableTerm> terms) {
        List<Term> mutableList = new ArrayList<>(terms.size());
        for (ImmutableTerm nextTerm : terms) {
            if (nextTerm instanceof ImmutableFunctionalTerm) {
                ImmutableFunctionalTerm term2Change = (ImmutableFunctionalTerm) nextTerm;
                Function newTerm = termFactory.getFunction(
                        term2Change.getFunctionSymbol(),
                        convertToMutableTerms(term2Change.getTerms()));
                mutableList.add(newTerm);
            }
            else {
                // Variables and constants are Term-instances
                mutableList.add((Term) nextTerm);
            }
        }
        return mutableList;
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
    private boolean composeTerms(Map<Variable, Term> map, Term term1, Term term2) {


        // Special case: unification of two functional terms (possibly recursive)
        if ((term1 instanceof Function) && (term2 instanceof Function)) {
            Function first = (Function) term1;
            Function second = (Function) term2;
            if (!first.getFunctionSymbol().equals(second.getFunctionSymbol()))
                return false;

            return composeTerms(map, first.clone().getTerms(), second.clone().getTerms());
        }

        if (term1.equals(term2))
            return true;

        Map.Entry<Variable, Term> s;
        if (term1 instanceof Variable)
            s = Maps.immutableEntry((Variable) term1, term2);
        else if (term2 instanceof Variable)
            s = Maps.immutableEntry((Variable)term2, term1);
        else
            return false; // neither is a variable, impossible to unify distinct terms

        // x is unified with f(x)
        if (s.getValue() instanceof Function
                && ((Function)s.getValue()).getTerms().contains(s.getKey()))
            return false;

        // The substitution for the current variable has become
        // trivial, e.g., x/x with the current composition. We
        // remove it to keep only a non-trivial unifier
        if (map.containsKey(s.getValue()))
            map.remove(s.getValue());

        for (Map.Entry<Variable,Term> e : map.entrySet()) {
            // not circular x -> y & y -> x
            if (!(e.getValue().equals(s.getKey()) && s.getValue().equals(e.getKey())))
                map.put(e.getKey(), composeWithSingletonSubstitution(e.getValue(), s));
        }
        map.put(s.getKey(), s.getValue());
        return true;
    }

    /**
     * May alter the functionalTerm (mutable style)
     *
     * Recursive
     */
    private Term composeWithSingletonSubstitution(Term t, Map.Entry<Variable, Term> s) {
        if (t.equals(s.getKey()))
            return s.getValue();

        if (t instanceof Function) {
            Function f = (Function)t;
            List<Term> newTerms = new ArrayList<>(f.getTerms().size());
            for (Term tt : f.getTerms())
                newTerms.add(composeWithSingletonSubstitution(tt, s));
            return termFactory.getFunction(f.getFunctionSymbol(), newTerms);
        }

        return t;
    }

    private boolean composeTerms(Map<Variable, Term> map, List<Term> first, List<Term> second) {
        if (first.size() != second.size())
            return false;

        int arity = first.size();

        // Computing the disagreement set
        for (int termidx = 0; termidx < arity; termidx++) {

            // Checking if there are already substitutions calculated for the
            // current terms. If there are any, then we have to take the
            // substituted terms instead of the original ones.

            Term term1 = first.get(termidx);
            Term term2 = second.get(termidx);

            if (!composeTerms(map, term1, term2))
                return false;

            // Applying the newly computed substitution to the 'replacement' of
            // the existing substitutions
            applySubstitution(first, map, termidx + 1);
            applySubstitution(second, map, termidx + 1);
        }

        return true;
    }


    /**
     * Applies the substitution to all the terms in the list. Note that this
     * will not clone the list or the terms inside the list.
     *
     * @param terms
     * @param map
     */

    private static void applySubstitution(List<Term> terms, Map<Variable, Term> map, int fromIndex) {
        for (int i = fromIndex; i < terms.size(); i++) {
            Term t = terms.get(i);
            if (t instanceof Variable) {
                Term replacement = map.get(t);
                if (replacement != null)
                    terms.set(i, replacement);
            }
            else if (t instanceof Function) {
                applySubstitution(((Function) t).getTerms(), map, 0);
            }
        }
    }

}
