package it.unibz.krdb.obda.ontology;

import it.unibz.krdb.obda.model.Predicate;

/***
 * A named class.
 */
public interface OClass extends BasicClassDescription {
	
	public Predicate getPredicate();
}
