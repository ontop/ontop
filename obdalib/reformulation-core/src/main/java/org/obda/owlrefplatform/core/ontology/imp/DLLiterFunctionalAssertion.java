package org.obda.owlrefplatform.core.ontology.imp;

import org.obda.owlrefplatform.core.ontology.Assertion;
import org.obda.owlrefplatform.core.ontology.RoleDescription;

public class DLLiterFunctionalAssertion implements Assertion{

	private RoleDescription functionalRole = null;
	
	public DLLiterFunctionalAssertion(RoleDescription role){
		
		this.functionalRole = role;
	}
}
