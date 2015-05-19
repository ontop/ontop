package it.unibz.krdb.obda.ontology.impl;

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

import java.util.Collections;
import java.util.Set;

import it.unibz.krdb.obda.ontology.NaryAxiom;

public class NaryAxiomImpl<T> implements NaryAxiom<T> {

	private static final long serialVersionUID = 4576840836473365808L;
	
	private final Set<T> components;
	
	NaryAxiomImpl(Set<T> components) {
		if (components.size() < 2)
			throw new IllegalArgumentException("At least 2 componets are expeccted in NaryAxiom");

		this.components = components;
	}
	
	@Override
	public Set<T> getComponents() {
		return Collections.unmodifiableSet(components);
	}


	@Override
	public boolean equals(Object obj) {
		if (obj instanceof NaryAxiomImpl<?>) {
			NaryAxiomImpl<T> other = (NaryAxiomImpl<T>)obj;
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
