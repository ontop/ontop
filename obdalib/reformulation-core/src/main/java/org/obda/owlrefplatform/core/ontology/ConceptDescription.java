package org.obda.owlrefplatform.core.ontology;

import org.obda.query.domain.Predicate;

public interface ConceptDescription {

	public Predicate getPredicate();
	public boolean isInverse();
}
