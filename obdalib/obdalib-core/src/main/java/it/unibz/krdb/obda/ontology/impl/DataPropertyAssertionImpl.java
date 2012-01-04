package it.unibz.krdb.obda.ontology.impl;

import java.util.HashSet;
import java.util.Set;

import it.unibz.krdb.obda.model.Predicate;
import it.unibz.krdb.obda.model.URIConstant;
import it.unibz.krdb.obda.model.ValueConstant;
import it.unibz.krdb.obda.ontology.DataPropertyAssertion;

public class DataPropertyAssertionImpl implements DataPropertyAssertion {

	private Predicate		role;
	private ValueConstant	o2;
	private URIConstant		o1;

	DataPropertyAssertionImpl(Predicate attribute, URIConstant o1, ValueConstant o2) {
		this.role = attribute;
		this.o1 = o1;
		this.o2 = o2;
	}

	@Override
	public URIConstant getObject() {
		return o1;
	}

	@Override
	public ValueConstant getValue() {
		return o2;
	}

	@Override
	public Predicate getAttribute() {
		return role;
	}
	
	public String toString() {
		return role.toString() + "(" + o1.toString() + ", " + o2.getValue() + ")";
	}

	@Override
	public Set<Predicate> getReferencedEntities() {
		Set<Predicate> res = new HashSet<Predicate>();
		res.add(role);
		return res;
	}

}
