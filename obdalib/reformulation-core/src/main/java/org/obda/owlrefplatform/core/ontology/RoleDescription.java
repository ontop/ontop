package org.obda.owlrefplatform.core.ontology;

import inf.unibz.it.obda.model.Predicate;


public interface RoleDescription {

	public boolean isInverse();
	public Predicate getPredicate();

}
