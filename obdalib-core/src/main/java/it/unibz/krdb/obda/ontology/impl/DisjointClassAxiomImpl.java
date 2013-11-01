/*
 * Copyright (C) 2009-2013, Free University of Bozen Bolzano
 * This source code is available under the terms of the Affero General Public
 * License v3.
 * 
 * Please see LICENSE.txt for full license terms, including the availability of
 * proprietary exceptions.
 */
package it.unibz.krdb.obda.ontology.impl;

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
