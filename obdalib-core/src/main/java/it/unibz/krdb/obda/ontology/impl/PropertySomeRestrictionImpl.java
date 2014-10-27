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

import it.unibz.krdb.obda.model.Predicate;
import it.unibz.krdb.obda.ontology.Property;
import it.unibz.krdb.obda.ontology.PropertySomeRestriction;

public class PropertySomeRestrictionImpl implements PropertySomeRestriction {

	private static final long serialVersionUID = 593821958539751283L;
	
	private final Property property;
	private final String string;

	PropertySomeRestrictionImpl(Property property) {
		this.property = property;
		StringBuilder bf = new StringBuilder();
		bf.append("E");
		bf.append(property.toString());
		this.string =  bf.toString();
	}

	@Override
	public Property getProperty() {
		return property;
	}

	@Override
	public boolean isInverse() {
		return property.isInverse();
	}

	@Override
	public Predicate getPredicate() {
		return property.getPredicate();
	}

	@Override
	public int hashCode() {
		return string.hashCode();
	}

	@Override
	public boolean equals(Object obj) {
		if (!(obj instanceof PropertySomeRestrictionImpl)) {
			return false;
		}
		PropertySomeRestrictionImpl other = (PropertySomeRestrictionImpl) obj;
		return property.equals(other.property);
	}

	@Override
	public String toString() {
		return string;
	}
}
