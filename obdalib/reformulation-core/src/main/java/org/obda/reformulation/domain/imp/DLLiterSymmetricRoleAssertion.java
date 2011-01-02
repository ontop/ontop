package org.obda.reformulation.domain.imp;

import org.obda.reformulation.domain.Assertion;
import org.obda.reformulation.domain.RoleDescription;

public class DLLiterSymmetricRoleAssertion implements Assertion{

	private RoleDescription role = null;
	
	public DLLiterSymmetricRoleAssertion(RoleDescription role){
		this.role = role;
	}
}
