package org.obda.owlrefplatform.core.ontology.imp;

import java.util.List;

import org.obda.owlrefplatform.core.ontology.Assertion;
import org.obda.owlrefplatform.core.ontology.RoleDescription;

public class DLLiterRoleEquivalenceAssertion implements Assertion {

private List<RoleDescription> equivalentRoles = null;
	
	public DLLiterRoleEquivalenceAssertion(List<RoleDescription> equivalentRoles){
		this.equivalentRoles = equivalentRoles;
	}
}
