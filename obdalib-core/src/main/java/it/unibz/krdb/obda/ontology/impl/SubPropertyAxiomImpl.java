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

import java.util.HashSet;
import java.util.Set;

import it.unibz.krdb.obda.model.Predicate;
import it.unibz.krdb.obda.ontology.Property;
import it.unibz.krdb.obda.ontology.SubPropertyOfAxiom;

public class SubPropertyAxiomImpl implements SubPropertyOfAxiom {

	private static final long serialVersionUID = -3020225654321319941L;

	private final Property including; // righthand side
	private final Property included;
	
	private final String string;
	private final int hash;
	
	SubPropertyAxiomImpl(Property subDesc, Property superDesc) {
		if (subDesc == null || superDesc == null) {
			throw new RuntimeException("Recieved null in property inclusion");
		}
		included = subDesc;
		including = superDesc;
		StringBuilder bf = new StringBuilder();
		bf.append(included.toString());
		bf.append(" ISA ");
		bf.append(including.toString());
		string = bf.toString();
		hash = string.hashCode();
	}

	@Override
	public Property getSub() {
		return included;
	}

	@Override
	public Property getSuper() {
		return including;
	}

	@Override
	public boolean equals(Object obj) {
		if (!(obj instanceof SubPropertyAxiomImpl)) {
			return false;
		}
		SubPropertyAxiomImpl inc2 = (SubPropertyAxiomImpl) obj;
		if (!including.equals(inc2.including)) {
			return false;
		}
		return (included.equals(inc2.included));
	}

	@Override
	public Set<Predicate> getReferencedEntities() {
		Set<Predicate> res = new HashSet<Predicate>();
		for (Predicate p : getPredicates(included)) {
			res.add(p);
		}
		for (Predicate p : getPredicates(including)) {
			res.add(p);
		}
		return res;
	}

	private Set<Predicate> getPredicates(Property desc) {
		Set<Predicate> preds = new HashSet<Predicate>();
		preds.add(desc.getPredicate());
		return preds;
	}

	@Override
	public int hashCode() {
		return hash;
	}

	@Override
	public String toString() {
		return string;
	}
	
}
