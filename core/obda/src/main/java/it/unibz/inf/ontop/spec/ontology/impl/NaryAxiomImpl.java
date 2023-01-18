package it.unibz.inf.ontop.spec.ontology.impl;

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

import com.google.common.collect.ImmutableList;

import it.unibz.inf.ontop.spec.ontology.NaryAxiom;

public class NaryAxiomImpl<T> implements NaryAxiom<T> {

	private final ImmutableList<T> components;
	
	NaryAxiomImpl(ImmutableList<T> components) {
		if (components.size() < 2)
			throw new IllegalArgumentException("At least two components are expected in NaryAxiom");

		this.components = components;
	}
	
	@Override
	public ImmutableList<T> getComponents() {
		return components;
	}


	@Override
	public boolean equals(Object obj) {
		if (obj instanceof NaryAxiomImpl) {
			NaryAxiomImpl<?> other = (NaryAxiomImpl<?>)obj;
			return components.equals(other.components);
		}
		return false;
	}
	
	@Override
	public int hashCode() {
		return components.hashCode();
	}
	
	@Override
	public String toString() {
		return "disjoint(" + components + ")";
	}
}
