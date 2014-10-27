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

import it.unibz.krdb.obda.ontology.DisjointPropertiesAxiom;
import it.unibz.krdb.obda.ontology.Property;

public class DisjointPropertiesAxiomImpl implements DisjointPropertiesAxiom {

	private static final long serialVersionUID = 4456694617300452114L;
	
	private final Property prop1;
	private final Property prop2;
	
	DisjointPropertiesAxiomImpl(Property p1, Property p2){
		this.prop1 = p1;
		this.prop2 = p2;
	}
	
	@Override
	public Property getFirst() {
		return prop1;
	}

	@Override
	public Property getSecond() {
		return prop2;
	}
	
	@Override
	public boolean equals(Object obj) {
		if (obj instanceof DisjointPropertiesAxiomImpl) {
			DisjointPropertiesAxiomImpl other = (DisjointPropertiesAxiomImpl)obj;
			return prop1.equals(other.prop1) && prop2.equals(other.prop2);
		}
		return false;
	}
	
	@Override
	public int hashCode() {
		return prop1.hashCode() + prop2.hashCode();
	}
	
	@Override
	public String toString() {
		return "disjoint(" + prop1 + ", " + prop2 + ")";
	}

}
