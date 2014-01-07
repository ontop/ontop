package it.unibz.krdb.obda.ontology.impl;

/*
 * #%L
 * ontop-obdalib-core
 * %%
 * Copyright (C) 2009 - 2013 Free University of Bozen-Bolzano
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

import it.unibz.krdb.obda.model.Predicate;
import it.unibz.krdb.obda.ontology.Property;

public class PropertyImpl implements Property {

	private static final long serialVersionUID = -2514037755762973974L;
	
	private boolean inverse = false;
	private Predicate predicate = null;

	protected PropertyImpl(Predicate p, boolean isInverse) {
		this.predicate = p;
		this.inverse = isInverse;
	}

	public boolean isInverse() {
		return inverse;
	}

	public Predicate getPredicate() {
		return predicate;
	}

	public int hashCode() {
		return toString().hashCode();
	}

	public boolean equals(Object obj) {
		if (!(obj instanceof PropertyImpl)) {
			return false;
		}
		PropertyImpl concept2 = (PropertyImpl) obj;
		if (inverse != concept2.inverse) {
			return false;
		}
		return (predicate.equals(concept2.predicate));
	}

	public String toString() {
		StringBuilder bf = new StringBuilder();
		bf.append(predicate.toString());
		if (inverse) {
			bf.append("^-");
		}
		return bf.toString();
	}
}
