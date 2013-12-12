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
import it.unibz.krdb.obda.ontology.DataType;
import it.unibz.krdb.obda.ontology.PropertySomeDataTypeRestriction;

public class PropertySomeDataTypeRestrictionImpl implements PropertySomeDataTypeRestriction {

	private static final long serialVersionUID = -3140348825768903966L;

	private final Predicate predicate;
	private final boolean isInverse;
	private final DataType filler;
	
	public PropertySomeDataTypeRestrictionImpl(Predicate p, boolean isInverse, DataType filler) {
		this.predicate = p;
		this.isInverse = isInverse;
		this.filler = filler;
	}

	@Override
	public boolean isInverse() {
		return isInverse;
	}
	
	@Override
	public Predicate getPredicate() {
		return predicate;
	}

	@Override
	public DataType getFiller() {
		return filler;
	}
	
	public int hashCode() {
		return toString().hashCode();
	}
	
	public boolean equals(Object obj) {
		if (!(obj instanceof PropertySomeDataTypeRestrictionImpl)) {
			return false;
		}
		PropertySomeDataTypeRestrictionImpl concept = (PropertySomeDataTypeRestrictionImpl) obj;
		return (isInverse == concept.isInverse) 
				&& (predicate.equals(concept.getPredicate()))
				&& (filler.equals(concept.filler));
	}

	public String toString() {
		StringBuilder bf = new StringBuilder();
		bf.append("E");
		bf.append(predicate.toString());
		if (isInverse) {
			bf.append("^-");
		}
		bf.append(".");
		bf.append(filler.toString());
		return bf.toString();
	}	
}
