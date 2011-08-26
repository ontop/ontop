package it.unibz.krdb.obda.owlrefplatform.core.ontology.imp;

import it.unibz.krdb.obda.model.Predicate;
import it.unibz.krdb.obda.owlrefplatform.core.ontology.Class;

public class ClassImpl implements Class {

	private Predicate	predicate	= null;

	public ClassImpl(Predicate p) {
		this.predicate = p;		
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
		return predicate.toString();
	}

}
