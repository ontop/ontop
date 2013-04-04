package it.unibz.krdb.obda.ontology;

import it.unibz.krdb.obda.model.Predicate;

/**
 * A non-qualified property some restriction. Corresponds to DL
 * "exists Property"
 */
public interface PropertySomeRestriction extends BasicClassDescription {

	public boolean isInverse();

	public Predicate getPredicate();
}
