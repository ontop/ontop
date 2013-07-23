package it.unibz.krdb.obda.ontology;

import it.unibz.krdb.obda.model.Predicate;

/**
 * An OWL2 property restriction on a named class. Corresponds to owl 2 QL
 * qualified property restrictions or DL existential roles.
 */
public interface PropertySomeClassRestriction extends ClassDescription {
	
	public boolean isInverse();

	public Predicate getPredicate();

	public OClass getFiller();
}
