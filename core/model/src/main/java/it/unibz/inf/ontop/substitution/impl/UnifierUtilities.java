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
import it.unibz.inf.ontop.utils.ImmutableCollectors;
import org.mapdb.Fun;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Stream;

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
        //System.out.println("MGU: " + args1 + " v " + args2);
        ImmutableMap<Variable, Term> r = composeTerms(ImmutableMap.of(), convertToMutableTerms(args1), convertToMutableTerms(args2));
        //System.out.println("RES: " + r);
        return r;
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
    private ImmutableMap<Variable, Term> composeTerms(ImmutableMap<Variable, Term> sub, Term term1, Term term2) {

        if (term1.equals(term2))
            return sub;

        // Special case: unification of two functional terms (possibly recursive)
        if ((term1 instanceof Function) && (term2 instanceof Function)) {
            Function first = (Function) term1;
            Function second = (Function) term2;
            if (!first.getFunctionSymbol().equals(second.getFunctionSymbol()))
                return null;

            return composeTerms(sub, first.clone().getTerms(), second.clone().getTerms());
        }

        ImmutableMap<Variable, Term> s;
        // avoid unifying x with f(g(x))
        if (term1 instanceof Variable && !variableOccursInTerm((Variable)term1, term2))
            s = ImmutableMap.of((Variable)term1, term2);
        else if (term2 instanceof Variable && !variableOccursInTerm((Variable)term2, term1))
            s = ImmutableMap.of((Variable)term2, term1);
        else
            return null; // neither is a variable, impossible to unify distinct terms

        return Stream.concat(s.entrySet().stream(), sub.entrySet().stream()
                .map(e -> Maps.immutableEntry(e.getKey(), apply(e.getValue(), s)))
                // The substitution for the current variable has become
                // trivial, e.g., x/x with the current composition. We
                // remove it to keep only a non-trivial unifier
                .filter(e -> !e.getValue().equals(e.getKey())))
                .collect(ImmutableCollectors.toMap());
    }

    private static boolean variableOccursInTerm(Variable v, Term t) {
        if (t instanceof Function)
            return ((Function)t).getTerms().stream().anyMatch(tt -> variableOccursInTerm(v, tt));
        return v.equals(t);
    }

    /**
     * May alter the functionalTerm (mutable style)
     *
     * Recursive
     */
    private Term apply(Term t, ImmutableMap<Variable, Term> sub) {
        if (t instanceof Function) {
            Function f = (Function)t;
            List<Term> newTerms = new ArrayList<>(f.getTerms().size());
            for (Term tt : f.getTerms())
                newTerms.add(apply(tt, sub));
            return termFactory.getFunction(f.getFunctionSymbol(), newTerms);
        }

        return sub.getOrDefault(t, t);
    }

    private ImmutableMap<Variable, Term> composeTerms(ImmutableMap<Variable, Term> sub, List<Term> first, List<Term> second) {
        if (first.size() != second.size())
            return null;

        int arity = first.size();
        for (int termidx = 0; termidx < arity; termidx++) {
            sub = composeTerms(sub, first.get(termidx), second.get(termidx));
            if (sub == null)
                return null;

            // Applying the newly computed substitution to the 'replacement' of
            // the existing substitutions
            for (int i = termidx + 1; i < arity; i++) {
                first.set(i, apply(first.get(i), sub));
                second.set(i, apply(second.get(i), sub));
            }
        }
        return sub;
    }
}
