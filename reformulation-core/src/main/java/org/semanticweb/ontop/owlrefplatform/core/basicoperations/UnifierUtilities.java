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

/*
 * Note: The the unifier does not distinguish between undistinguished variables
 * i.e.when we have an atom A(#,#) which should be unified with B(b,c) the
 * unifier willreturn two thetas #/b, #/c. So far so good but this will lead to
 * problems whenapplying the thetas because there is no distinction between
 * #-variables, sothe first theta is applied to all #-variables and the rest is
 * ignored.In order to avoid problems one can enumerate the undistinguished
 * variables ie. A(#1,#2)
 */

import org.semanticweb.ontop.model.Function;
import org.semanticweb.ontop.model.CQIE;
import org.semanticweb.ontop.model.Term;
import org.semanticweb.ontop.model.impl.*;

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
     * Unifies two atoms in a conjunctive query returning a new conjunctive
     * query. To to this we calculate the MGU for atoms, duplicate the query q
     * into q', remove i and j from q', apply the mgu to q', and
     *
     * @param q
     * @param i
     * @param j (j > i)
     * @return null if the two atoms are not unifiable, else a new conjunctive
     * query produced by the unification of j and i
     * @throws Exception
     */
    public static CQIE unify(CQIE q, int i, int j) {

        Substitution mgu = getMGU(q.getBody().get(i), q.getBody().get(j));
        if (mgu == null)
            return null;

        CQIE unifiedQ = SubstitutionUtilities.applySubstitution(q, mgu);
        unifiedQ.getBody().remove(i);
        unifiedQ.getBody().remove(j - 1);

        Function atom1 = q.getBody().get(i);
        Function atom2 = q.getBody().get(j);
        //Function newatom = unify((Function) atom1, (Function) atom2, mgu);

        // take care of anonymous variables
        Function newatom = (Function) atom1.clone();
        for (int ii = 0; ii < atom1.getTerms().size(); ii++) {
            Term t1 = atom1.getTerms().get(ii);
            if (t1 instanceof AnonymousVariable)
                newatom.getTerms().set(ii, atom2.getTerms().get(ii));
        }
        SubstitutionUtilities.applySubstitution(newatom, mgu);

        unifiedQ.getBody().add(i, newatom);

        return unifiedQ;
    }


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
    public static Substitution getMGU(Function first, Function second) {
        SubstitutionImpl mgu = new SubstitutionImpl();
        if (mgu.composeFunctions(first, second))
            return mgu;
        return null;
    }
}
