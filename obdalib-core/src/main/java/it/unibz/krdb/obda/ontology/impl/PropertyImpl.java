package it.unibz.krdb.obda.ontology.impl;

import it.unibz.krdb.obda.model.Predicate;
import it.unibz.krdb.obda.ontology.Property;

public class PropertyImpl implements Property {

	private static final long serialVersionUID = -2514037755762973974L;
	
	private boolean inverse = false;
	private Predicate predicate = null;

	protected PropertyImpl(Predicate p, boolean isInverse) {
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
		if (!(obj instanceof PropertyImpl)) {
			return false;
		}
		PropertyImpl concept2 = (PropertyImpl) obj;
		if (inverse != concept2.inverse) {
			return false;
		}
		return (predicate.equals(concept2.predicate));
	}

	public String toString() {
		StringBuilder bf = new StringBuilder();
		bf.append(predicate.toString());
		if (inverse) {
			bf.append("^-");
		}
		return bf.toString();
	}
}
