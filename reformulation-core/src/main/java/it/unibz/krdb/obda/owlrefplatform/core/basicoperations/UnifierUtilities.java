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

import it.unibz.krdb.obda.model.Function;


/**
 * A Class that provides general utilities related to unification, of terms and
 * atoms.
 * <p/>
 * See also SubstitutionUtilities that contains methods that were initially present
 * in this class.
 *
 * @author mariano
 */
public class UnifierUtilities {

    /**
     * Computes the Most General Unifier (MGU) for two n-ary atoms.
     * <p/>
     * IMPORTANT: Function terms are supported as long as they are not nested.
     * <p/>
     *
     * @param first
     * @param second
     * @return the substitution corresponding to this unification.
     */
    public static Substitution getMGU(Function first, Function second) {
/*
        // Basic case: if predicates are different or their arity is different,
        // then no unifier
        if ((first.getArity() != second.getArity()
                || !first.getFunctionSymbol().equals(second.getFunctionSymbol()))) {
            return null;
        }

        Function firstAtom = (Function) first.clone();
        Function secondAtom = (Function) second.clone();

        int arity = first.getArity();
        Substitution mgu = new SubstitutionImpl();

        // Computing the disagreement set
        for (int termidx = 0; termidx < arity; termidx++) {

            // Checking if there are already substitutions calculated for the
            // current terms. If there are any, then we have to take the
            // substituted terms instead of the original ones.

            Term term1 = firstAtom.getTerm(termidx);
            Term term2 = secondAtom.getTerm(termidx);

            boolean changed = false;

            // We have two cases, unifying 'simple' terms, and unifying function terms.
            if (!(term1 instanceof Function) || !(term2 instanceof Function)) {
                if (!mgu.compose(term1, term2))
                    return null;

                changed = true;
            } 
            else {
                // if both of them are function terms then we need to do some
                // check in the inner terms
                Function fterm1 = (Function) term1;
                Function fterm2 = (Function) term2;

                if ((fterm1.getTerms().size() != fterm2.getTerms().size()) ||
                        !fterm1.getFunctionSymbol().equals(fterm2.getFunctionSymbol())) {
                    return null;
                }

                int innerarity = fterm1.getTerms().size();
                for (int innertermidx = 0; innertermidx < innerarity; innertermidx++) {

                    if (!mgu.compose(fterm1.getTerm(innertermidx), fterm2.getTerm(innertermidx)))
                        return null;

                    changed = true;

                    // Applying the newly computed substitution to the 'replacement' of
                    // the existing substitutions
                    SubstitutionUtilities.applySubstitution(fterm1, mgu, innertermidx + 1);
                    SubstitutionUtilities.applySubstitution(fterm2, mgu, innertermidx + 1);
                }
            }
            if (changed) {

                // Applying the newly computed substitution to the 'replacement' of
                // the existing substitutions
                SubstitutionUtilities.applySubstitution(firstAtom, mgu, termidx + 1);
                SubstitutionUtilities.applySubstitution(secondAtom, mgu, termidx + 1);
            }
        }
        return mgu;
*/        
        SubstitutionImpl mgu = new SubstitutionImpl();
        if (mgu.composeFunctions(first, second))
            return mgu;
        return null;
    }

}
