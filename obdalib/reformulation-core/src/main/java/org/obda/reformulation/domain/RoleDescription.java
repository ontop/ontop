package org.obda.reformulation.domain;

import org.obda.query.domain.Predicate;

public interface RoleDescription {

	public boolean isInverse();
	public Predicate getPredicate();

}
