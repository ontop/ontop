/*
 * Copyright (C) 2009-2013, Free University of Bozen Bolzano
 * This source code is available under the terms of the Affero General Public
 * License v3.
 * 
 * Please see LICENSE.txt for full license terms, including the availability of
 * proprietary exceptions.
 */
package it.unibz.krdb.obda.ontology.impl;

import java.util.HashSet;
import java.util.Set;

import it.unibz.krdb.obda.model.Predicate;
import it.unibz.krdb.obda.ontology.DisjointPropertyAxiom;

public class DisjointPropertyAxiomImpl implements DisjointPropertyAxiom {

	private static final long serialVersionUID = 4456694617300452114L;
	
	private Predicate pred1;
	private Predicate pred2;
	
	DisjointPropertyAxiomImpl(Predicate p1, Predicate p2){
		this.pred1 = p1;
		this.pred2 = p2;
	}
	
	public String toString() {
		return "disjoint(" + pred1.getName() + ", " + pred2.getName() + ")";
	}

	@Override
	public Set<Predicate> getReferencedEntities() {
		Set<Predicate> res = new HashSet<Predicate>();
		res.add(pred1);
		res.add(pred2);
		return res;
	}

	@Override
	public Predicate getFirst() {
		return this.pred1;
	}

	@Override
	public Predicate getSecond() {
		return this.pred2;
	}
	
	
}
