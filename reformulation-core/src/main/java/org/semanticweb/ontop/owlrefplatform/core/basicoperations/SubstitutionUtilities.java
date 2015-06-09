package org.semanticweb.ontop.owlrefplatform.core.basicoperations;

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


import org.semanticweb.ontop.model.*;
import org.semanticweb.ontop.model.impl.OBDADataFactoryImpl;
import org.semanticweb.ontop.model.impl.OBDAVocabulary;
import org.semanticweb.ontop.model.impl.VariableImpl;

import java.util.Collection;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * This utility class provides common operations
 * on substitution functions.
 *
 */
public class SubstitutionUtilities {

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


    private static OBDADataFactory ofac = OBDADataFactoryImpl.getInstance();

    /**
     * This method will return a new query, resulting from the application of
     * the substitution function to the original query q. To do this, we will call the clone()
     * method of the original query and then will call applySubstitution to each atom
     * of the cloned query.
     *
     * @param q
     * @param substitution
     * @return
     */
    public static CQIE applySubstitution(CQIE q, Substitution substitution, boolean clone) {

        CQIE newq;
        if (clone)
            newq = q.clone();
        else
            newq = q;

        Function head = newq.getHead();
        applySubstitution(head, substitution);
        for (Function bodyatom : newq.getBody())
            applySubstitution(bodyatom, substitution);

        return newq;
    }

    public static CQIE applySubstitution(CQIE q, Substitution substitution) {
        return applySubstitution(q, substitution, true);
    }

    /**
     * Applies the substitution to all the terms in the list. Note that this
     * will not clone the list or the terms inside the list.
     *
     * @param atom
     * @param unifier
     */

    public static void applySubstitution(Function atom, Substitution unifier) {
        applySubstitution(atom, unifier, 0);
    }

    public static void applySubstitution(Function atom, Substitution unifier, int fromIndex) {

        List<Term> terms = atom.getTerms();

        for (int i = fromIndex; i < terms.size(); i++) {
            Term t = terms.get(i);

            // unifiers only apply to variables, simple or inside functional terms

            if (t instanceof VariableImpl) {
                Term replacement = unifier.get((VariableImpl) t);
                if (replacement != null)
                    terms.set(i, replacement);
            } else if (t instanceof Function) {
                Function t2 = (Function) t;
                applySubstitution(t2, unifier);
            }
        }
    }

    /**
     * @param atom
     * @param unifier
     */
    public static void applySubstitutionToGetFact(Function atom, Substitution unifier) {

        List<Term> terms = atom.getTerms();
        for (int i = 0; i < terms.size(); i++) {
            Term t = terms.get(i);
            /*
             * unifiers only apply to variables, simple or inside functional
             * terms
             */
            if (t instanceof VariableImpl) {
                Term replacement = unifier.get((VariableImpl) t);
                if (replacement != null) {
                    terms.set(i, replacement);
                } else {
                    terms.set(i, ofac.getConstantFreshLiteral());
                }
            } else if (t instanceof Function) {
                Function t2 = (Function) t;
                applySubstitution(t2, unifier);
            }
        }
    }

    /**
     * returns a substitution that assigns NULLs to all variables in the list
     *
     * @param vars the list of variables
     * @return substitution
     */

    public static Substitution getNullifier(Collection<Variable> vars) {
        Map<VariableImpl, Term> entries = new HashMap<>();

        for (Variable var : vars) {
            entries.put((VariableImpl) var, OBDAVocabulary.NULL);
        }
        Substitution substitution = new SubstitutionImpl(entries);
        return substitution;
    }

    /**
     * Computes the union of two substitution functions.
     *
     * This union is a little bit more than the simple union of their corresponding set: it
     * fails if some singleton substitutions are conflicting. --> Throws a SubstitutionUnionException.
     *
     * Returns a new substitution function.
     *
     */
    public static Substitution union(Substitution substitution1, Substitution substitution2)
            throws SubstitutionUnionException {
        Map<VariableImpl, Term> substitutionMap = new HashMap<>();
        substitutionMap.putAll(substitution1.getMap());

        for (Map.Entry<VariableImpl, Term> newEntry : substitution2.getMap().entrySet()) {

            /**
             * Checks if the variable is part of the domain
             * of the first substitution function.
             *
             * If not, adds the entry.
             * Otherwise, throws a exception if the two entries
             * are not equivalent (remind that a substitution must be a function).
             */
            Term substitutionTerm = substitution1.get(newEntry.getKey());
            if (substitutionTerm == null) {
                substitutionMap.put(newEntry.getKey(), newEntry.getValue());
            }

            else if (!substitutionTerm.equals(newEntry.getValue())) {
                throw new SubstitutionUnionException();
            }
        }

        Substitution newSubstitution = new SubstitutionImpl(substitutionMap);
        return newSubstitution;
    }
}
