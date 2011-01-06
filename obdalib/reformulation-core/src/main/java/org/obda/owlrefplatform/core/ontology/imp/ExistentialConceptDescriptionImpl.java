package org.obda.owlrefplatform.core.ontology.imp;

import org.obda.owlrefplatform.core.ontology.BasicConceptDescription;
import org.obda.query.domain.Predicate;

public class ExistentialConceptDescriptionImpl implements BasicConceptDescription{

	private Predicate predicate = null;
	private boolean isInverse = false;

	public ExistentialConceptDescriptionImpl(Predicate p, boolean isInverse ){
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
		if (!(obj instanceof ExistentialConceptDescriptionImpl))
			return false;
		ExistentialConceptDescriptionImpl concept2 = (ExistentialConceptDescriptionImpl)obj;
		if (isInverse != concept2.isInverse)
			return false;
		return (predicate.equals(concept2));
	}

	public String toString() {
		StringBuffer bf = new StringBuffer();
		bf.append("E");
		bf.append(predicate.toString());
		if (isInverse)
			bf.append("^-");
		return bf.toString();
	}

}
