package it.unibz.krdb.obda.owlrefplatform.core.ontology.imp;

import it.unibz.krdb.obda.owlrefplatform.core.ontology.Assertion;
import it.unibz.krdb.obda.owlrefplatform.core.ontology.RoleDescription;

import java.util.List;


public class DLLiterRoleEquivalenceAssertion implements Assertion {

private List<RoleDescription> equivalentRoles = null;
	
	public DLLiterRoleEquivalenceAssertion(List<RoleDescription> equivalentRoles){
		this.equivalentRoles = equivalentRoles;
	}
}
