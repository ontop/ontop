package it.unibz.inf.ontop.substitution;

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

import it.unibz.inf.ontop.model.term.Function;
import it.unibz.inf.ontop.model.term.Term;
import it.unibz.inf.ontop.model.term.Variable;

import java.util.Map;

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
 *
 */
public interface Substitution {

    /**
     * Map representation of this function.
     */
    Map<Variable, Term> getMap();


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
     * Composes two functional terms. Can be recursive.
     *
     * Side-effect method: might add to new entries to the substitution.
     *
     */
    boolean composeFunctions(Function term1, Function term2);
}

