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
import it.unibz.krdb.obda.ontology.OClass;
import it.unibz.krdb.obda.ontology.PropertySomeClassRestriction;

public class PropertySomeClassRestrictionImpl implements PropertySomeClassRestriction{

	private static final long serialVersionUID = -8242919752579569694L;
	
	private final Predicate predicate;
	private final boolean isInverse;
	private final OClass filler;

	PropertySomeClassRestrictionImpl(Predicate p, boolean isInverse, OClass filler) {
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

	public int hashCode() {
		return toString().hashCode();
	}

	public boolean equals(Object obj) {
		if (!(obj instanceof PropertySomeClassRestrictionImpl)) {
			return false;
		}
		PropertySomeClassRestrictionImpl concept2 = (PropertySomeClassRestrictionImpl) obj;
		if (isInverse != concept2.isInverse) {
			return false;
		}
		if (!predicate.equals(concept2.getPredicate())) {
			return false;
		}
		return (filler.equals(concept2.filler));
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

	@Override
	public OClass getFiller() {
		return filler;
	}
}
