package org.obda.owlrefplatform.core.ontology.imp;

import org.obda.owlrefplatform.core.ontology.BasicConceptDescription;
import org.obda.query.domain.Predicate;

public class AtomicConceptDescriptionImpl implements BasicConceptDescription{

	private Predicate predicate = null;
	private boolean isInverse = false;

	public AtomicConceptDescriptionImpl(Predicate p, boolean isInverse ){
		this.predicate =p;
		this.isInverse = isInverse;
	}

	public boolean isInverse(){
		return isInverse;
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
		AtomicConceptDescriptionImpl concept2 = (AtomicConceptDescriptionImpl)obj;
		return (predicate.equals(concept2));
	}

	public String toString() {
		return predicate.toString();
	}

}
