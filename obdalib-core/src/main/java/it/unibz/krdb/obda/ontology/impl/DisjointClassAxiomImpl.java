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
import it.unibz.krdb.obda.ontology.DisjointClassAxiom;
import it.unibz.krdb.obda.ontology.OClass;

import java.util.HashSet;
import java.util.Set;

public class DisjointClassAxiomImpl implements DisjointClassAxiom {

	private static final long serialVersionUID = 4576840836473365808L;
	
	private OClass class1;
	private OClass class2;
	
	DisjointClassAxiomImpl(OClass c1, OClass c2) {
		this.class1 = c1;
		this.class2 = c2;
	}
	
	public String toString() {
		return "disjoint(" + class1.getPredicate().getName() + ", " + class2.getPredicate().getName() + ")";
	}

	@Override
	public Set<Predicate> getReferencedEntities() {
		Set<Predicate> res = new HashSet<Predicate>();
		res.add(class1.getPredicate());
		res.add(class2.getPredicate());
		return res;
	}

	@Override
	public OClass getFirst() {
		return this.class1;
	}

	@Override
	public OClass getSecond() {
		return this.class2;
	}
}
