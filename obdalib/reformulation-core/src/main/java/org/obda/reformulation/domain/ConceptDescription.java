package org.obda.reformulation.domain;

import org.obda.query.domain.Predicate;

public interface ConceptDescription {

	public Predicate getPredicate();
	public boolean isInverse();
}
