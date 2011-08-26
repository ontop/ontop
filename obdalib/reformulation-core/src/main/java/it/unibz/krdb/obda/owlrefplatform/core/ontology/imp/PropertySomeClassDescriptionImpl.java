package it.unibz.krdb.obda.owlrefplatform.core.ontology.imp;

import it.unibz.krdb.obda.model.Predicate;
import it.unibz.krdb.obda.owlrefplatform.core.ontology.Class;
import it.unibz.krdb.obda.owlrefplatform.core.ontology.PropertySomeClassDescription;

public class PropertySomeClassDescriptionImpl implements PropertySomeClassDescription{

	private final  Predicate	predicate;
	private final boolean		isInverse;
	private final Class filler;

	public PropertySomeClassDescriptionImpl(Predicate p, boolean isInverse, Class filler) {
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
		if (!(obj instanceof PropertySomeClassDescriptionImpl))
			return false;
		PropertySomeClassDescriptionImpl concept2 = (PropertySomeClassDescriptionImpl) obj;
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
