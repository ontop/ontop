package it.unibz.krdb.obda.owlrefplatform.core.basicoperations;

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

import com.google.common.base.Joiner;
import it.unibz.krdb.obda.model.Term;
import it.unibz.krdb.obda.model.impl.*;

import java.util.*;


/**
 * Mutable reference implementation of a Substitution.
 */
public class SubstitutionImpl implements Substitution {

    private final Map<VariableImpl, Term> map;

    public SubstitutionImpl() {
        this.map = new HashMap<>();
    }

    public SubstitutionImpl(Map<VariableImpl, Term> substitutionMap) {
        this.map = substitutionMap;
    }

    @Override
    public Term get(VariableImpl var) {
        return map.get(var);
    }

    @Override
    public Map<VariableImpl, Term> getMap() {
        return map;
    }

    @Override
    public boolean isEmpty() {
        return map.isEmpty();
    }

    @Override
    @Deprecated
    public void put(VariableImpl var, Term term) {
        map.put(var, term);
    }

    @Override
    public String toString() {
        return Joiner.on(", ").withKeyValueSeparator("/").join(map);
    }

    /**
     * Composes the current substitution with another substitution function.
     *
     * Remind that composition is not commutative.
     *
     * TODO: implement it
     */
    @Override
    public boolean compose(Substitution otherSubstitution) {
        throw new UnsupportedOperationException("Not implemented yet! But looks interesting.");
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
    @Override
    public boolean compose(Term term1, Term term2) {

        Substitution s = createUnifier(term1, term2);

        // Rejected substitution (conflicts)
        if (s == null)
            return false;

        // Neutral substitution
        if (s.isEmpty())
            return true;

        // Not neutral, not null --> should be a singleton.
        SingletonSubstitution substitution = (SingletonSubstitution) s;


        List<VariableImpl> forRemoval = new ArrayList<>();
        for (Map.Entry<VariableImpl,Term> entry : map.entrySet()) {
            VariableImpl v = entry.getKey();
            Term t = entry.getValue();
            if (substitution.getVariable().equals(t)) { // ROMAN: no need in isEqual(t, s.getVariable())
                if (v.equals(substitution.getTerm())) {  // ROMAN: no need in isEqual(v, s.getTerm())
                    // The substitution for the current variable has become
                    // trivial, e.g., x/x with the current composition. We
                    // remove it to keep only a non-trivial unifier
                    forRemoval.add(v);
                } else
                    map.put(v, substitution.getTerm());

            }
            else if (t instanceof FunctionalTermImpl) {
                FunctionalTermImpl fclone = (FunctionalTermImpl)t.clone();
                List<Term> innerTerms = fclone.getTerms();
                boolean innerchanges = false;
                // TODO this ways of changing inner terms in functions is not
                // optimal, modify

                for (int i = 0; i < innerTerms.size(); i++) {
                    Term innerTerm = innerTerms.get(i);

                    if (substitution.getVariable().equals(innerTerm)) { // ROMAN: no need in isEqual(innerTerm, s.getVariable())
                        fclone.getTerms().set(i, substitution.getTerm());
                        innerchanges = true;
                    }
                }
                if (innerchanges)
                    map.put(v, fclone);
            }
        }
        map.keySet().removeAll(forRemoval);
        map.put(substitution.getVariable(), substitution.getTerm());
        return true;
    }

    /***
     * Computes the unifier that makes two terms equal.
     *
     * ROMAN: careful -- does not appear to work correctly with AnonymousVariables
     *
     * @param term1
     * @param term2
     * @return
     */
    private static Substitution createUnifier(Term term1, Term term2) {

        if (!(term1 instanceof VariableImpl) && !(term2 instanceof VariableImpl)) {

            // neither is a variable, impossible to unify unless the two terms are
            // equal, in which case there the substitution is empty
            if ((term1 instanceof AnonymousVariable) || (term2 instanceof AnonymousVariable)) {
                // this is questionable -- consider R(_,_) and R(c,c)
                return new NeutralSubstitution();
            }

            if ((term1 instanceof VariableImpl) || (term1 instanceof FunctionalTermImpl)
                    || (term1 instanceof ValueConstantImpl) || (term1 instanceof URIConstantImpl)) {

                // ROMAN: why is BNodeConstantImpl not mentioned?

                if (term1.equals(term2))
                    return new NeutralSubstitution();
                else
                    return null;
            }
            throw new RuntimeException("Exception comparing two terms, unknown term class. Terms: "
                    + term1 + ", " + term2 + " Classes: " + term1.getClass()
                    + ", " + term2.getClass());
        }

        // arranging the terms so that the first is always a variable
        VariableImpl t1;
        Term t2;
        if (term1 instanceof VariableImpl) {
            t1 = (VariableImpl)term1;
            t2 = term2;
        } else {
            t1 = (VariableImpl)term2;
            t2 = term1;
        }

        // Undistinguished variables do not need a substitution,
        // the unifier knows about this
        if (t2 instanceof AnonymousVariable) { // ROMAN: no need in (t1 instanceof AnonymousVariable)
            return new NeutralSubstitution();
        }
        else if (t2 instanceof VariableImpl) {
            if (t1.equals(t2))   // ROMAN: no need in isEqual(t1, t2) -- both are proper variables
                return new NeutralSubstitution();
            else
                return new SingletonSubstitution(t1, t2);
        }
        else if ((t2 instanceof ValueConstantImpl) || (t2 instanceof URIConstantImpl)) {
            return new SingletonSubstitution(t1, t2);
        }
        else if (t2 instanceof FunctionalTermImpl) {
            FunctionalTermImpl fterm = (FunctionalTermImpl) t2;
            if (fterm.containsTerm(t1))
                return null;
            else
                return new SingletonSubstitution(t1, t2);
        }
        // this should never happen
        throw new RuntimeException("Unsupported unification case: " + term1 + " " + term2);
    }
}
