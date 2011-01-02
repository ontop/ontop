package org.obda.reformulation.domain.imp;

import org.obda.reformulation.domain.Assertion;
import org.obda.reformulation.domain.RoleDescription;

public class DLLiterFunctionalAssertion implements Assertion{

	private RoleDescription functionalRole = null;
	
	public DLLiterFunctionalAssertion(RoleDescription role){
		
		this.functionalRole = role;
	}
}
