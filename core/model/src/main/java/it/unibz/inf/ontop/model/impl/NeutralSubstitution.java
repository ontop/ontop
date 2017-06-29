package it.unibz.inf.ontop.model.impl;

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


import java.util.Optional;

import com.google.common.collect.ImmutableList;
import com.google.common.collect.ImmutableMap;
import com.google.common.collect.ImmutableSet;
import it.unibz.inf.ontop.exception.ConversionException;
import it.unibz.inf.ontop.model.atom.DataAtom;
import it.unibz.inf.ontop.model.atom.DistinctVariableDataAtom;
import it.unibz.inf.ontop.model.atom.DistinctVariableOnlyDataAtom;
import it.unibz.inf.ontop.model.term.*;
import it.unibz.inf.ontop.substitution.ImmutableSubstitution;
import it.unibz.inf.ontop.substitution.Var2VarSubstitution;
import it.unibz.inf.ontop.substitution.impl.LocallyImmutableSubstitutionImpl;


/**
 * TODO: explain
 */
public class NeutralSubstitution extends LocallyImmutableSubstitutionImpl implements ImmutableSubstitution<ImmutableTerm> {

    @Override
    public ImmutableTerm get(Variable var) {
        return null;
    }

    @Override
    public ImmutableTerm apply(ImmutableTerm term) {
        return term;
    }

    @Override
    public ImmutableTerm applyToVariable(Variable variable) {
        return variable;
    }

    @Override
    public ImmutableFunctionalTerm applyToFunctionalTerm(ImmutableFunctionalTerm functionalTerm) {
        return functionalTerm;
    }

    @Override
    public Function applyToMutableFunctionalTerm(Function mutableFunctionalTerm) {
        return mutableFunctionalTerm;
    }

    @Override
    public ImmutableExpression applyToBooleanExpression(ImmutableExpression booleanExpression) {
        return booleanExpression;
    }

    @Override
    public DataAtom applyToDataAtom(DataAtom atom) throws ConversionException {
        return atom;
    }

    @Override
    public DistinctVariableDataAtom applyToDistinctVariableDataAtom(DistinctVariableDataAtom dataAtom) {
        return dataAtom;
    }

    @Override
    public DistinctVariableOnlyDataAtom applyToDistinctVariableOnlyDataAtom(DistinctVariableOnlyDataAtom dataAtom) {
        return dataAtom;
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
    public Optional<ImmutableSubstitution<? extends ImmutableTerm>> unionHeterogeneous(
            ImmutableSubstitution<? extends ImmutableTerm> other) {
        return Optional.of(other);
    }

    @Override
    public ImmutableSubstitution<ImmutableTerm> applyToTarget(ImmutableSubstitution<? extends ImmutableTerm> otherSubstitution) {
        ImmutableMap<Variable, ImmutableTerm> map = ImmutableMap.copyOf(otherSubstitution.getImmutableMap());
        return new ImmutableSubstitutionImpl<>(map);
    }

    @Override
    public ImmutableSubstitution<ImmutableTerm> orientate(ImmutableList<Variable> priorityVariables) {
        return this;
    }

    @Override
    public Optional<ImmutableExpression> convertIntoBooleanExpression() {
        return AbstractImmutableSubstitutionImpl.convertIntoBooleanExpression(this);
    }

    @Override
    public Var2VarSubstitution getVar2VarFragment() {
        return new Var2VarSubstitutionImpl(ImmutableMap.of());
    }

    @Override
    public ImmutableSubstitution<GroundTerm> getVar2GroundTermFragment() {
        return new ImmutableSubstitutionImpl<>(ImmutableMap.of());
    }

    @Override
    public ImmutableSubstitution<ImmutableTerm> reduceDomainToIntersectionWith(ImmutableSet<Variable> restrictingDomain) {
        return new ImmutableSubstitutionImpl<>(ImmutableMap.of());
    }

    @Override
    public final ImmutableMap<Variable, Term> getMap() {
        return (ImmutableMap<Variable, Term>)(ImmutableMap<Variable, ?>) getImmutableMap();
    }

    @Override
    public ImmutableMap<Variable, ImmutableTerm> getImmutableMap() {
        return ImmutableMap.of();
    }

    @Override
    public boolean isDefining(Variable variable) {
        return false;
    }

    @Override
    public ImmutableSet<Variable> getDomain() {
        return ImmutableSet.of();
    }

    @Override
    public boolean isEmpty() {
        return true;
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
