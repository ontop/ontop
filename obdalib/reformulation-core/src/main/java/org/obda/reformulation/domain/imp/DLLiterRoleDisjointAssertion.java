package org.obda.reformulation.domain.imp;

import java.util.List;

import org.obda.reformulation.domain.Assertion;
import org.obda.reformulation.domain.RoleDescription;

public class DLLiterRoleDisjointAssertion implements Assertion {

	private List<RoleDescription> disjointRoles = null;
	
	public DLLiterRoleDisjointAssertion(List<RoleDescription> disjointRoles){
		this.disjointRoles = disjointRoles;
	}
}
