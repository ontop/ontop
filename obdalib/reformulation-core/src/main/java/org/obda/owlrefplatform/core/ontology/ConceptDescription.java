package org.obda.owlrefplatform.core.ontology;

import inf.unibz.it.obda.model.Predicate;


public interface ConceptDescription {

	public Predicate getPredicate();
	public boolean isInverse();
}
