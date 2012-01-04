package it.unibz.krdb.obda.owlrefplatform.core.ontology;

import it.unibz.krdb.obda.model.Predicate;

/***
 * A non-qualified property some restriction. Corresponds to DL
 * "exists Property"
 * 
 * @author Mariano Rodriguez Muro
 * 
 */
public interface PropertySomeRestriction extends BasicClassDescription {

	public boolean isInverse();

	public Predicate getPredicate();
}
