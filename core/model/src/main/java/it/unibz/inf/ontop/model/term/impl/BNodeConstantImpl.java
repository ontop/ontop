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

import it.unibz.inf.ontop.iq.node.VariableNullability;
import it.unibz.inf.ontop.model.term.*;
import it.unibz.inf.ontop.model.type.ObjectRDFType;
import it.unibz.inf.ontop.model.type.TypeFactory;

import java.util.stream.Stream;

/**
 * Implementation for BNodes.
 */
public class BNodeConstantImpl extends AbstractNonNullConstant implements BNode {

	private final String name;
	private final ObjectRDFType type;

	/**
	 * The default constructor.
	 */
	protected BNodeConstantImpl(String name, TypeFactory typeFactory) {
		this.name = name;
		this.type = typeFactory.getBlankNodeType();
	}

	@Override
	public boolean equals(Object obj) {
		if (obj instanceof BNodeConstantImpl) {
			BNodeConstantImpl other = (BNodeConstantImpl) obj;
			return this.name.equals(other.name);
		}
		return false;
	}

	@Override
	public int hashCode() {
		return name.hashCode();
	}

	@Override
	public String getName() {
		return name;
	}
	@Override
	public String getValue() {
		return name;
	}

	@Override
	public boolean isGround() {
		return true;
	}

	@Override
	public Stream<Variable> getVariableStream() {
		return Stream.of();
	}

	@Override
	public IncrementalEvaluation evaluateStrictEq(ImmutableTerm otherTerm, VariableNullability variableNullability) {
		if (otherTerm instanceof Constant) {
			if (((Constant) otherTerm).isNull())
				return IncrementalEvaluation.declareIsNull();
			return equals(otherTerm)
					? IncrementalEvaluation.declareIsTrue()
					: IncrementalEvaluation.declareIsFalse();
		}
		else
			return otherTerm.evaluateStrictEq(this, variableNullability);
	}

	@Override
	public String toString() {
		return name;
	}

	@Override
	public ObjectRDFType getType() {
		return type;
	}

}
