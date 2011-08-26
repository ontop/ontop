package it.unibz.krdb.obda.owlrefplatform.core.ontology.imp;

import it.unibz.krdb.obda.model.Predicate;
import it.unibz.krdb.obda.owlrefplatform.core.ontology.Class;
import it.unibz.krdb.obda.owlrefplatform.core.ontology.PropertySomeClassRestriction;

public class PropertySomeClassRestrictionImpl implements PropertySomeClassRestriction{

	private final  Predicate	predicate;
	private final boolean		isInverse;
	private final Class filler;

	PropertySomeClassRestrictionImpl(Predicate p, boolean isInverse, Class filler) {
		this.predicate = p;
		this.isInverse = isInverse;
		this.filler = filler;
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
		if (!(obj instanceof PropertySomeClassRestrictionImpl))
			return false;
		PropertySomeClassRestrictionImpl concept2 = (PropertySomeClassRestrictionImpl) obj;
		if (isInverse != concept2.isInverse)
			return false;
		if (!predicate.equals(concept2.getPredicate()))
			return false;
		return (filler.equals(concept2.filler));
	}

	public String toString() {
		StringBuffer bf = new StringBuffer();
		bf.append("E");
		bf.append(predicate.toString());
		if (isInverse)
			bf.append("^-");
		bf.append(".");
		bf.append(filler.toString());
		return bf.toString();
	}

	@Override
	public Class getFiller() {
		return filler;
	}
}
