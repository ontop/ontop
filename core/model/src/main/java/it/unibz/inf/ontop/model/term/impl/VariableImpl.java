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


import it.unibz.inf.ontop.model.term.Variable;

import java.util.stream.Stream;

public class VariableImpl implements Variable, Comparable<Variable> {

	private final String name;

	protected VariableImpl(String name) {
		if (name == null) {
			throw new RuntimeException("Variable name cannot be null");
		}
		this.name = name;
	}

	@Override
	public boolean equals(Object obj) {
		if (obj == null || !(obj instanceof Variable)) {
			return false;
		}
		Variable name2 = (Variable) obj;
		return name.equals(name2.getName());
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
	public Variable clone() {
		return this;
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
	public int compareTo(Variable other) {
		return name.compareTo(other.getName());
	}
}
