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

import org.semanticweb.ontop.model.Term;
import org.semanticweb.ontop.model.impl.VariableImpl;

import java.util.Map;
import java.util.Set;

/**
 * A substitution is a function of variables to terms.
 *
 * Terms can be:
 *  - Variables
 *  - Constants
 *  - Composite terms (Function).
 *
 *  Typical algebraic operations on substitution functions are composition and union.
 *
 *  See the SubstitutionUtilities
 *  {@link org.semanticweb.ontop.owlrefplatform.core.basicoperations.SubstitutionUtilities}
 *
 */
public interface Substitution {

    /**
     * TODO: replace VariableImpl by Variable
     */
    Term get(VariableImpl var);

    /**
     * Map representation of this function.
     */
    Map<VariableImpl, Term> getMap();

    boolean isEmpty();

    /**
     * Composes the current substitution with another substitution function.
     *
     * Remind that composition is not commutative.
     *
     * CURRENTLY NOT IMPLEMENTED!
     * TODO: provide implementations for this method.
     */
    boolean compose(Substitution s);


    /***
     * Creates a singleton substitution out of term1 and term2.
     *
     * Then, composes the current substitution with the latter.
     * (remind that composition is not commutative).
     *
     *
     * Note that the unifier will be modified in this process.
     *
     * The operation is as follows
     *
     * {x/y, m/y} composed with (y,z) is equal to {x/z, m/z, y/z}
     *
     * @param term1
     * @param term2
     * @return true if the substitution exists (false if it does not)
     */
    boolean compose(Term term1, Term term2);

    @Deprecated
    Set<VariableImpl> keySet();
}
