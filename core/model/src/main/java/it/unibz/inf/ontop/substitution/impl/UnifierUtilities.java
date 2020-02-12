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
import it.unibz.inf.ontop.substitution.ImmutableSubstitution;
import it.unibz.inf.ontop.substitution.SubstitutionFactory;
import it.unibz.inf.ontop.utils.ImmutableCollectors;

import java.util.HashMap;
import java.util.Optional;
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
    private final SubstitutionFactory substitutionFactory;

    @Inject
    public UnifierUtilities(TermFactory termFactory, SubstitutionFactory substitutionFactory) {
        this.termFactory = termFactory;
        this.substitutionFactory = substitutionFactory;
    }

    /**
     * Computes the Most General Unifier (MGU) for two n-ary atoms.
     *
     * @param args1
     * @param args2
     * @return the substitution corresponding to this unification.
     */
    public <T extends ImmutableTerm> Optional<ImmutableSubstitution<T>> getMGU(ImmutableList<? extends ImmutableTerm> args1, ImmutableList<? extends ImmutableTerm> args2) {
        if (args1.equals(args2))
            return Optional.of(substitutionFactory.getSubstitution());

        ImmutableMap<Variable, ImmutableTerm> sub = unify(ImmutableMap.of(), args1, args2);
        if (sub == null)
            return Optional.empty();

        // quick hack to fix the order
        return Optional.of(substitutionFactory.getSubstitution(
                (ImmutableMap)ImmutableMap.copyOf(new HashMap<>(sub))));
    }

    private static boolean variableOccursInTerm(Variable v, ImmutableTerm term) {
        if (term instanceof ImmutableFunctionalTerm)
            return ((ImmutableFunctionalTerm)term).getTerms().stream()
                    .anyMatch(t -> variableOccursInTerm(v, t));
        return v.equals(term);
    }

    /**
     * Recursive
     */
    private ImmutableTerm apply(ImmutableTerm t, ImmutableMap<Variable, ImmutableTerm> sub) {
        if (t instanceof ImmutableFunctionalTerm) {
            ImmutableFunctionalTerm f = (ImmutableFunctionalTerm)t;
            ImmutableList<ImmutableTerm> terms = f.getTerms().stream()
                    .map(tt -> apply(tt, sub))
                    .collect(ImmutableCollectors.toList());
            return termFactory.getImmutableFunctionalTerm(f.getFunctionSymbol(), terms);
        }
        return sub.getOrDefault(t, t);
    }


    /**
     * Creates a unifier for args1 and args2
     *
     * The operation is as follows
     *
     * {x/y, m/y} composed with (y,z) is equal to {x/z, m/z, y/z}
     *
     * @return true the substitution (of null if it does not)
     */

    private ImmutableMap<Variable, ImmutableTerm> unify(ImmutableMap<Variable, ImmutableTerm> sub, ImmutableList<? extends ImmutableTerm> args1, ImmutableList<? extends ImmutableTerm> args2) {
        if (args1.size() != args2.size())
            return null;

        int arity = args1.size();
        for (int i = 0; i < arity; i++) {
            // applying the computed substitution first
            ImmutableTerm term1 = apply(args1.get(i), sub);
            ImmutableTerm term2 = apply(args2.get(i), sub);

            if (term1.equals(term2))
                continue;

            // Special case: unification of two functional terms (possibly recursive)
            if ((term1 instanceof ImmutableFunctionalTerm) && (term2 instanceof ImmutableFunctionalTerm)) {
                ImmutableFunctionalTerm f1 = (ImmutableFunctionalTerm) term1;
                ImmutableFunctionalTerm f2 = (ImmutableFunctionalTerm) term2;
                if (!f1.getFunctionSymbol().equals(f2.getFunctionSymbol()))
                    return null;

                sub = unify(sub, f1.getTerms(), f2.getTerms());
                if (sub == null)
                    return null;
            }
            else {
                ImmutableMap<Variable, ImmutableTerm> s;
                // avoid unifying x with f(g(x))
                if (term1 instanceof Variable && !variableOccursInTerm((Variable) term1, term2))
                    s = ImmutableMap.of((Variable) term1, term2);
                else if (term2 instanceof Variable && !variableOccursInTerm((Variable) term2, term1))
                    s = ImmutableMap.of((Variable) term2, term1);
                else
                    return null; // neither is a variable, impossible to unify distinct terms

                sub = Stream.concat(s.entrySet().stream(), sub.entrySet().stream()
                        .map(e -> Maps.immutableEntry(e.getKey(), apply(e.getValue(), s)))
                        // The substitution for the current variable has become
                        // trivial, e.g., x/x with the current composition. We
                        // remove it to keep only a non-trivial unifier
                        .filter(e -> !e.getValue().equals(e.getKey())))
                        .collect(ImmutableCollectors.toMap());
            }
        }
        return sub;
    }
}
