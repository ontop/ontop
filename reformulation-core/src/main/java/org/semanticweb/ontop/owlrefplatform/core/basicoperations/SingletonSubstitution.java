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

import com.google.common.collect.ImmutableMap;
import com.google.common.collect.ImmutableSet;
import org.semanticweb.ontop.model.*;

/**
 * An atomic substitution accepts only one variable in its domain.
 */
public class SingletonSubstitution extends LocallyImmutableSubstitutionImpl {

    private final Variable variable;
    private final Term term;

    public SingletonSubstitution(Variable var, Term term) {
        this.variable = var;
        this.term = term;
    }

    public Term getTerm() {
        return term;
    }

    public Variable getVariable() {
        return variable;
    }

    @Override
    public Term get(Variable var) {
        if (var.equals(variable))
            return term;
        return null;
    }

    @Override
    public ImmutableMap<Variable, Term> getMap() {
        return ImmutableMap.of(variable, term);
    }

    @Override
    public boolean isEmpty() {
        return false;
    }
}
