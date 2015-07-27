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


import com.google.common.base.Optional;
import com.google.common.collect.ImmutableMap;
import com.google.common.collect.ImmutableSet;
import org.semanticweb.ontop.model.*;
import org.semanticweb.ontop.model.impl.VariableImpl;

/**
 * TODO: explain
 */
public class NeutralSubstitution extends LocallyImmutableSubstitutionImpl implements ImmutableSubstitution<ImmutableTerm> {

    @Override
    public ImmutableTerm get(VariableImpl var) {
        return null;
    }

    @Override
    public ImmutableTerm apply(ImmutableTerm term) {
        return term;
    }

    @Override
    public ImmutableTerm applyToVariable(VariableImpl variable) {
        return variable;
    }

    @Override
    public ImmutableFunctionalTerm applyToFunctionalTerm(ImmutableFunctionalTerm functionalTerm) {
        return functionalTerm;
    }

    @Override
    public ImmutableBooleanExpression applyToBooleanExpression(ImmutableBooleanExpression booleanExpression) {
        return booleanExpression;
    }

    @Override
    public DataAtom applyToDataAtom(DataAtom atom) throws ConversionException {
        return atom;
    }

    @Override
    public ImmutableSubstitution<ImmutableTerm> composeWith(ImmutableSubstitution<? extends ImmutableTerm> g) {
        return (ImmutableSubstitution<ImmutableTerm>)g;
    }

    @Override
    public Optional<ImmutableSubstitution<ImmutableTerm>> union(ImmutableSubstitution<ImmutableTerm> otherSubstitution) {
        return Optional.of(otherSubstitution);
    }

    @Override
    public ImmutableSubstitution<ImmutableTerm> applyToTarget(ImmutableSubstitution<? extends ImmutableTerm> otherSubstitution) {
        ImmutableMap<VariableImpl, ImmutableTerm> map = ImmutableMap.copyOf(otherSubstitution.getImmutableMap());
        return new ImmutableSubstitutionImpl<>(map);
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
    public boolean isDefining(VariableImpl variable) {
        return false;
    }

    @Override
    public boolean isEmpty() {
        return true;
    }

    @Override
    @Deprecated
    public ImmutableSet<VariableImpl> keySet() {
        return ImmutableSet.of();
    }

    @Override
	public String toString() {
		return "{-/-}";
	}

    @Override
    public boolean equals(Object other) {
        if (other instanceof ImmutableSubstitution) {
            return ((ImmutableSubstitution) other).isEmpty();
        }
        else {
            return false;
        }
    }
}
