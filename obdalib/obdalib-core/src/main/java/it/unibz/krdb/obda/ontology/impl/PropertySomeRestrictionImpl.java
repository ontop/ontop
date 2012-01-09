package it.unibz.krdb.obda.ontology.impl;

import it.unibz.krdb.obda.model.Predicate;
import it.unibz.krdb.obda.ontology.PropertySomeRestriction;

public class PropertySomeRestrictionImpl implements PropertySomeRestriction {

	/**
	 * 
	 */
	private static final long serialVersionUID = 593821958539751283L;
	private Predicate	predicate	= null;
	private boolean		isInverse	= false;

	PropertySomeRestrictionImpl(Predicate p, boolean isInverse) {
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
