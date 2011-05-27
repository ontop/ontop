package it.unibz.krdb.obda.owlrefplatform.core.ontology.imp;

import it.unibz.krdb.obda.owlrefplatform.core.ontology.Assertion;
import it.unibz.krdb.obda.owlrefplatform.core.ontology.RoleDescription;

import java.util.List;


public class DLLiterRoleDisjointAssertion implements Assertion {

	private List<RoleDescription> disjointRoles = null;
	
	public DLLiterRoleDisjointAssertion(List<RoleDescription> disjointRoles){
		this.disjointRoles = disjointRoles;
	}
}
