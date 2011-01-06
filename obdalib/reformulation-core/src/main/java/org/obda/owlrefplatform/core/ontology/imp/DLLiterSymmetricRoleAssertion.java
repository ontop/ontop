package org.obda.owlrefplatform.core.ontology.imp;

import org.obda.owlrefplatform.core.ontology.Assertion;
import org.obda.owlrefplatform.core.ontology.RoleDescription;

public class DLLiterSymmetricRoleAssertion implements Assertion{

	private RoleDescription role = null;
	
	public DLLiterSymmetricRoleAssertion(RoleDescription role){
		this.role = role;
	}
}
