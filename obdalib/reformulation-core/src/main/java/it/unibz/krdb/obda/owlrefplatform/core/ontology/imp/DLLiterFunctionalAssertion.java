package it.unibz.krdb.obda.owlrefplatform.core.ontology.imp;

import it.unibz.krdb.obda.owlrefplatform.core.ontology.Assertion;
import it.unibz.krdb.obda.owlrefplatform.core.ontology.RoleDescription;

public class DLLiterFunctionalAssertion implements Assertion{

	private RoleDescription functionalRole = null;
	
	public DLLiterFunctionalAssertion(RoleDescription role){
		
		this.functionalRole = role;
	}
}
