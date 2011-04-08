package org.obda.owlrefplatform.core.ontology.imp;

import org.obda.owlrefplatform.core.ontology.BasicRoleDescription;
import org.obda.query.domain.Predicate;

public class AtomicRoleDescriptionImpl implements BasicRoleDescription {

	private boolean		inverse		= false;
	private Predicate	predicate	= null;

	protected AtomicRoleDescriptionImpl(Predicate p, boolean isInverse) {
		this.predicate = p;
		this.inverse = isInverse;
	}

	public boolean isInverse() {
		return inverse;
	}

	public Predicate getPredicate() {
		return predicate;
	}

	public int hashCode() {
		return toString().hashCode();
	}
	
	public boolean equals(Object obj) {
		if (!(obj instanceof AtomicRoleDescriptionImpl))
			return false;
		AtomicRoleDescriptionImpl concept2 = (AtomicRoleDescriptionImpl)obj;
		if (inverse != concept2.inverse)
			return false;
		return (predicate.equals(concept2.predicate));
	}

	public String toString() {
		StringBuffer bf = new StringBuffer();
		bf.append(predicate.toString());
		if (inverse)
			bf.append("^-");
		return bf.toString();
	}

}
