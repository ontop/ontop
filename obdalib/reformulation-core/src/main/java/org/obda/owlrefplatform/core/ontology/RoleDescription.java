package org.obda.owlrefplatform.core.ontology;

import it.unibz.krdb.obda.model.Predicate;


public interface RoleDescription {

	public boolean isInverse();
	public Predicate getPredicate();

}
