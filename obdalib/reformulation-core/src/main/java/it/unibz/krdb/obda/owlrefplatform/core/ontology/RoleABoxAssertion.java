package it.unibz.krdb.obda.owlrefplatform.core.ontology;

import it.unibz.krdb.obda.model.Predicate;
import it.unibz.krdb.obda.model.URIConstant;

public interface RoleABoxAssertion extends Assertion {

	public URIConstant getFirstObject();

	public URIConstant getSecondObject();

	public Predicate getRole();

}
