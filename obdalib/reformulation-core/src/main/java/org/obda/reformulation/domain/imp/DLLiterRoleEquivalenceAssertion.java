package org.obda.reformulation.domain.imp;

import java.util.List;

import org.obda.reformulation.domain.Assertion;
import org.obda.reformulation.domain.RoleDescription;

public class DLLiterRoleEquivalenceAssertion implements Assertion {

private List<RoleDescription> equivalentRoles = null;
	
	public DLLiterRoleEquivalenceAssertion(List<RoleDescription> equivalentRoles){
		this.equivalentRoles = equivalentRoles;
	}
}
