package it.unibz.inf.ontop.spec.ontology.impl;

import it.unibz.inf.ontop.spec.ontology.BinaryAxiom;

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



public class BinaryAxiomImpl<T> implements BinaryAxiom<T> {

	private final T including; // right-hand side
	private final T included;
	private final String string;
	
	BinaryAxiomImpl(T included, T including) {
		this.included = included;
		this.including = including;
		string = included + " ISA " + including;
	}

	@Override
	public T getSub() {
		return included;
	}

	@Override
	public T getSuper() {
		return including;
	}

	@Override
	public boolean equals(Object obj) {
		if (obj instanceof BinaryAxiomImpl) {
			BinaryAxiomImpl<?> inc2 = (BinaryAxiomImpl<?>) obj;
			return including.equals(inc2.including) && included.equals(inc2.included);
		}
		return false;
	}
	
	@Override
	public int hashCode() {
		return string.hashCode();
	}

	@Override
	public String toString() {
		return string;
	}
}
