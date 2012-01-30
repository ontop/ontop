package it.unibz.krdb.obda.ontology.impl;

import it.unibz.krdb.obda.model.Predicate;
import it.unibz.krdb.obda.ontology.DataType;

public class DataTypeImpl implements DataType {
	
	private Predicate predicate;
	
	public DataTypeImpl(Predicate p) {
		predicate = p;
	}
	
	public Predicate getPredicate() {
		return predicate;
	}
	
	public int hashCode() {
		return toString().hashCode();
	}
	
	@Override
	public String toString() {
		return predicate.getName().toString();
	}
}
