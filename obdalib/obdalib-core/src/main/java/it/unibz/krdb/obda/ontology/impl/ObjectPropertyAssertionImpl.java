package it.unibz.krdb.obda.ontology.impl;

import java.util.HashSet;
import java.util.Set;

import it.unibz.krdb.obda.model.Predicate;
import it.unibz.krdb.obda.model.URIConstant;
import it.unibz.krdb.obda.ontology.ObjectPropertyAssertion;

public class ObjectPropertyAssertionImpl implements ObjectPropertyAssertion {

	/**
	 * 
	 */
	private static final long serialVersionUID = -8834975903851540150L;
	private Predicate	role;
	private URIConstant	o2;
	private URIConstant	o1;

	ObjectPropertyAssertionImpl(Predicate role, URIConstant o1, URIConstant o2) {
		this.role = role;
		this.o1 = o1;
		this.o2 = o2;
	}

	@Override
	public URIConstant getFirstObject() {
		return o1;
	}

	@Override
	public URIConstant getSecondObject() {
		return o2;
	}

	@Override
	public Predicate getRole() {
		return role;
	}

	public String toString() {
		return role.toString() + "(" + o1.toString() + ", " + o2.toString() + ")";
	}
	
	@Override
	public Set<Predicate> getReferencedEntities() {
		Set<Predicate> res = new HashSet<Predicate>();
		res.add(role);
		return res;
	}
}
