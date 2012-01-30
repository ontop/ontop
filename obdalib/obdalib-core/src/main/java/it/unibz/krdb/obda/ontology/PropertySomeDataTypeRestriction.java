package it.unibz.krdb.obda.ontology;

import it.unibz.krdb.obda.model.Predicate;

public interface PropertySomeDataTypeRestriction extends ClassDescription {
	
	public boolean isInverse();

	public Predicate getPredicate();

	public DataType getFiller();
}
