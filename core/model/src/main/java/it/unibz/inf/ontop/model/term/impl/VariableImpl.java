package it.unibz.inf.ontop.model.term.impl;

/*
 * #%L
 * ontop-obdalib-core
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


import com.google.common.collect.ImmutableSet;
import it.unibz.inf.ontop.iq.node.VariableNullability;
import it.unibz.inf.ontop.model.term.*;

import java.util.Objects;
import java.util.stream.Stream;

public class VariableImpl extends AbstractNonFunctionalTerm implements Variable, Comparable<Variable> {

	private final String name;

	protected VariableImpl(String name) {
		Objects.requireNonNull(name, "Variable name cannot be null");
		this.name = name;
	}

	@Override
	public boolean equals(Object other) {
		return (other instanceof VariableImpl
				&& this.name.equals(((VariableImpl) other).name));
	}

	@Override
	public int hashCode() {
		return name.hashCode();
	}

	@Override
	public String getName() {
		return name;
	}

	// TODO this method seems to be tied to some semantics, if we modified it,
	// things become slow and maybe wrong we must make sure that this is not the
	// case
	@Override
	public String toString() {
		return name;
	}

	@Override
	public boolean isGround() {
		return false;
	}

	@Override
	public Stream<Variable> getVariableStream() {
		return Stream.of(this);
	}

	@Override
	public IncrementalEvaluation evaluateStrictEq(ImmutableTerm otherTerm, VariableNullability variableNullability) {
		if (otherTerm instanceof Variable) {
			return equals(otherTerm)
					? IncrementalEvaluation.declareIsTrue()
					: IncrementalEvaluation.declareSameExpression();
		}
		else if (otherTerm instanceof ImmutableFunctionalTerm) {
			// Functional terms are in charge of evaluating other terms
			return otherTerm.evaluateStrictEq(this, variableNullability);
		}
		// Constant
		else  {
			return otherTerm.isNull()
					? IncrementalEvaluation.declareIsNull()
					: IncrementalEvaluation.declareSameExpression();
		}
	}

    @Override
    public IncrementalEvaluation evaluateIsNotNull(VariableNullability variableNullability) {
		return variableNullability.isPossiblyNullable(this)
				? IncrementalEvaluation.declareSameExpression()
				: IncrementalEvaluation.declareIsTrue();
    }

    @Override
	public boolean isNullable(ImmutableSet<Variable> nullableVariables) {
		return nullableVariables.contains(this);
	}

	@Override
	public int compareTo(Variable other) {
		return name.compareTo(other.getName());
	}
}
