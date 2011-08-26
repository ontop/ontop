package it.unibz.krdb.obda.owlrefplatform.core.ontology;

import it.unibz.krdb.obda.model.Predicate;

public interface PropertySomeClassDescription extends ClassDescription {
	public boolean isInverse();

	public Predicate getPredicate();

	public Class getFiller();
}
