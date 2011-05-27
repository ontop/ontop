package org.obda.owlrefplatform.core.ontology;

import it.unibz.krdb.obda.model.Predicate;


public interface ConceptDescription {

	public Predicate getPredicate();
	public boolean isInverse();
}
