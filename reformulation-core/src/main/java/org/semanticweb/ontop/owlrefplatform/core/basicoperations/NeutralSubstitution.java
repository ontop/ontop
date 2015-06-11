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
import org.semanticweb.ontop.model.impl.VariableImpl;

/**
 * TODO: explain
 */
public class NeutralSubstitution extends LocallyImmutableSubstitutionImpl implements ImmutableSubstitution {

    @Override
    public Term get(VariableImpl var) {
        return null;
    }

    @Override
    public final ImmutableMap<VariableImpl, Term> getMap() {
        return (ImmutableMap<VariableImpl, Term>)(ImmutableMap<VariableImpl, ?>) getImmutableMap();
    }

    @Override
    public ImmutableMap<VariableImpl, ImmutableTerm> getImmutableMap() {
        return ImmutableMap.of();
    }

    @Override
    public boolean isEmpty() {
        return true;
    }

    @Override
    public ImmutableSet<VariableImpl> keySet() {
        return ImmutableSet.of();
    }

    @Override
	public String toString() {
		return "{-/-}";
	}


}
