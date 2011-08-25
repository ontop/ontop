package it.unibz.krdb.obda.owlrefplatform.core.ontology.imp;

import it.unibz.krdb.obda.model.Predicate;
import it.unibz.krdb.obda.model.URIConstant;
import it.unibz.krdb.obda.model.ValueConstant;
import it.unibz.krdb.obda.owlrefplatform.core.ontology.AttributeABoxAssertion;

public class DLLiterAttributeABoxAssertionImpl implements AttributeABoxAssertion {

	private Predicate		role;
	private ValueConstant	o2;
	private URIConstant		o1;

	public DLLiterAttributeABoxAssertionImpl(Predicate attribute, URIConstant o1, ValueConstant o2) {
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

}
