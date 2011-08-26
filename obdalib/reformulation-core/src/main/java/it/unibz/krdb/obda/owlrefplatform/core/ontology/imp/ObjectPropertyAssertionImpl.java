package it.unibz.krdb.obda.owlrefplatform.core.ontology.imp;

import it.unibz.krdb.obda.model.Predicate;
import it.unibz.krdb.obda.model.URIConstant;
import it.unibz.krdb.obda.owlrefplatform.core.ontology.RoleABoxAssertion;

public class ObjectPropertyAssertionImpl implements RoleABoxAssertion {

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
}
