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


import com.google.common.collect.ImmutableMap;
import com.google.common.collect.ImmutableSet;

import it.unibz.krdb.obda.model.Function;
import it.unibz.krdb.obda.model.Term;
import it.unibz.krdb.obda.model.Variable;

import java.util.Map;
import java.util.Set;

/**
 * TODO: explain
 */
public class NeutralSubstitution implements Substitution {

    @Override
    public Term get(Variable var) {
        return null;
    }

    @Override
    public Map<Variable, Term> getMap() {
        return ImmutableMap.of();
    }

    @Override
    public boolean isEmpty() {
        return true;
    }

    @Override
    public void put(Variable var, Term term) {
        throw new UnsupportedOperationException("Every SingletonSubstitution is immutable.");
    }

    @Override
    public boolean composeFunctions(Function term1, Function term2) {
        throw new UnsupportedOperationException("Every SingletonSubstitution is immutable.");
    }

    @Override
	public String toString() {
		return "{-/-}";
	}

    @Override
    public boolean compose(Substitution s) {
        throw new UnsupportedOperationException();
    }

    @Override
    public boolean composeTerms(Term term1, Term term2) {
        throw new UnsupportedOperationException();
    }

}
