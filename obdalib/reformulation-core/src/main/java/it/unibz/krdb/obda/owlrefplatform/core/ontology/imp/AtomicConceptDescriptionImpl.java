package it.unibz.krdb.obda.owlrefplatform.core.ontology.imp;

import it.unibz.krdb.obda.model.Predicate;
import it.unibz.krdb.obda.owlrefplatform.core.ontology.AtomicConceptDescription;

public class AtomicConceptDescriptionImpl implements AtomicConceptDescription {

	private Predicate	predicate	= null;

	public AtomicConceptDescriptionImpl(Predicate p) {
		this.predicate = p;		
	}


	public Predicate getPredicate() {
		return predicate;
	}

	public int hashCode() {
		return toString().hashCode();
	}

	public boolean equals(Object obj) {
		if (!(obj instanceof AtomicConceptDescriptionImpl))
			return false;
		AtomicConceptDescriptionImpl concept2 = (AtomicConceptDescriptionImpl) obj;
		return (predicate.equals(concept2.getPredicate()));
	}

	public String toString() {
		return predicate.toString();
	}

}
