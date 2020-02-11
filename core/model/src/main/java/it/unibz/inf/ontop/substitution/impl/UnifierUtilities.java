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

import com.google.inject.Inject;
import com.google.inject.Singleton;
import it.unibz.inf.ontop.model.term.Function;
import it.unibz.inf.ontop.model.term.TermFactory;
import it.unibz.inf.ontop.substitution.Substitution;

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
    public Substitution getMGU(Function first, Function second) {
        SubstitutionImpl mgu = new SubstitutionImpl(termFactory);
        if (mgu.composeFunctions(first, second))
            return mgu;
        return null;
    }
}
