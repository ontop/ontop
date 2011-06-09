package it.unibz.krdb.obda.owlrefplatform.core.ontology.imp;

import it.unibz.krdb.obda.owlrefplatform.core.ontology.FunctionalRoleAssertion;
import it.unibz.krdb.obda.owlrefplatform.core.ontology.RoleDescription;

public class FunctionalRoleAssertionImpl implements FunctionalRoleAssertion{

	private RoleDescription role = null;
	
	public FunctionalRoleAssertionImpl(RoleDescription role) {
		this.role = role;
	}
	
}
