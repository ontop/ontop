package it.unibz.krdb.obda.owlrefplatform.core.ontology;

import it.unibz.krdb.obda.model.Predicate;

public interface PropertySomeDescription extends BasicClassDescription {

	public boolean isInverse();

	public Predicate getPredicate();
}
