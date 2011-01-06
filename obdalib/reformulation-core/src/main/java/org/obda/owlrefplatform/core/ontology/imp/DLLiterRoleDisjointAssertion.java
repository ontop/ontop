package org.obda.owlrefplatform.core.ontology.imp;

import java.util.List;

import org.obda.owlrefplatform.core.ontology.Assertion;
import org.obda.owlrefplatform.core.ontology.RoleDescription;

public class DLLiterRoleDisjointAssertion implements Assertion {

	private List<RoleDescription> disjointRoles = null;
	
	public DLLiterRoleDisjointAssertion(List<RoleDescription> disjointRoles){
		this.disjointRoles = disjointRoles;
	}
}
