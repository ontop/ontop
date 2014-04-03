package it.unibz.krdb.obda.ontology.impl;

import java.util.HashSet;
import java.util.Set;

import it.unibz.krdb.obda.model.Predicate;
import it.unibz.krdb.obda.ontology.BasicClassDescription;
import it.unibz.krdb.obda.ontology.DisjointBasicClassAxiom;
import it.unibz.krdb.obda.ontology.OClass;

public class DisjointBasicClassAxiomImpl implements DisjointBasicClassAxiom {

	private static final long serialVersionUID = -3930101320996594570L;
	
	private BasicClassDescription class1;
	private BasicClassDescription class2;
	
	public DisjointBasicClassAxiomImpl(BasicClassDescription b1, BasicClassDescription b2) {
		this.class1 = b1;
		this.class2 = b2;
	}

	@Override
	public Set<Predicate> getReferencedEntities() {
		Set<Predicate> res = new HashSet<Predicate>();
		res.add(class1.getPredicate());
		res.add(class2.getPredicate());
		return res;
	}

	@Override
	public BasicClassDescription getFirst() {
		
		return class1;
	}

	@Override
	public BasicClassDescription getSecond() {
		
		return class2;
	}

	public String toString() {
		return "disjoint(" + class1 + ", " + class2 + ")";
	}
}
