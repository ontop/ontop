package org.obda.owlrefplatform.core.ontology;

import org.obda.query.domain.Predicate;

public interface RoleDescription {

	public boolean isInverse();
	public Predicate getPredicate();

}
