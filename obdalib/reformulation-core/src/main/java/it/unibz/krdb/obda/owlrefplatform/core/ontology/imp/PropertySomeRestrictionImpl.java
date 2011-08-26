package it.unibz.krdb.obda.owlrefplatform.core.ontology.imp;

import it.unibz.krdb.obda.model.Predicate;
import it.unibz.krdb.obda.owlrefplatform.core.ontology.PropertySomeRestriction;

public class PropertySomeRestrictionImpl implements PropertySomeRestriction {

	private Predicate	predicate	= null;
	private boolean		isInverse	= false;

	public PropertySomeRestrictionImpl(Predicate p, boolean isInverse) {
		this.predicate = p;
		this.isInverse = isInverse;
	}

	@Override
	public boolean isInverse() {
		return isInverse;
	}

	@Override
	public Predicate getPredicate() {
		return predicate;
	}

	public int hashCode() {
		return toString().hashCode();
	}

	public boolean equals(Object obj) {
		if (!(obj instanceof PropertySomeRestrictionImpl))
			return false;
		PropertySomeRestrictionImpl concept2 = (PropertySomeRestrictionImpl) obj;
		if (isInverse != concept2.isInverse)
			return false;
		return (predicate.equals(concept2.getPredicate()));
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
