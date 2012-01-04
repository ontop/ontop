package it.unibz.krdb.obda.ontology.impl;

import it.unibz.krdb.obda.model.Predicate;
import it.unibz.krdb.obda.ontology.OClass;

public class ClassImpl implements OClass {

	private Predicate	predicate	= null;

	String				str			= null;

	ClassImpl(Predicate p) {
		this.predicate = p;
		str = predicate.toString();
	}

	public Predicate getPredicate() {
		return predicate;
	}

	public int hashCode() {
		return toString().hashCode();
	}

	public boolean equals(Object obj) {
		if (!(obj instanceof ClassImpl))
			return false;
		ClassImpl concept2 = (ClassImpl) obj;
		return (predicate.equals(concept2.getPredicate()));
	}

	public String toString() {
		return str;
	}
}
