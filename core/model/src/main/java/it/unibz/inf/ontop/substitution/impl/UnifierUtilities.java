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
import java.util.stream.Collectors;
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
    public ImmutableMap<Variable, ImmutableTerm> getMGU(ImmutableList<? extends ImmutableTerm> args1,ImmutableList<? extends ImmutableTerm> args2) {
        ImmutableMap<Variable, ImmutableTerm> r = composeTerms(ImmutableMap.of(), args1, args2);
        if (r == null)
            return null;
        return ImmutableMap.copyOf(new HashMap<>(r)); // quick hack to fix the order
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
     * @return true if the substitution exists (false if it does not)
     */

    private ImmutableMap<Variable, ImmutableTerm> composeTerms(ImmutableMap<Variable, ImmutableTerm> sub, ImmutableList<? extends ImmutableTerm> args1, ImmutableList<? extends ImmutableTerm> args2) {
        if (args1.size() != args2.size())
            return null;

        int arity = args1.size();
        List<ImmutableTerm> t1 = new ArrayList<>(args1);
        List<ImmutableTerm> t2 = new ArrayList<>(args2); // mutable copies
        for (int i = 0; i < arity; i++) {
            ImmutableTerm term1 = t1.get(i);
            ImmutableTerm term2 = t2.get(i);

            if (term1.equals(term2))
                continue;

            // Special case: unification of two functional terms (possibly recursive)
            if ((term1 instanceof ImmutableFunctionalTerm) && (term2 instanceof ImmutableFunctionalTerm)) {
                ImmutableFunctionalTerm f1 = (ImmutableFunctionalTerm) term1;
                ImmutableFunctionalTerm f2 = (ImmutableFunctionalTerm) term2;
                if (!f1.getFunctionSymbol().equals(f2.getFunctionSymbol()))
                    return null;

                sub = composeTerms(sub, f1.getTerms(), f2.getTerms());
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
            // Applying the newly computed substitution to the 'replacement' of
            // the existing substitutions
            for (int j = i + 1; j < arity; j++) {
                t1.set(j, apply(t1.get(j), sub));
                t2.set(j, apply(t2.get(j), sub));
            }
        }
        return sub;
    }
}
