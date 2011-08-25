package it.unibz.krdb.obda.owlrefplatform.core.ontology;

import it.unibz.krdb.obda.model.Predicate;
import it.unibz.krdb.obda.model.URIConstant;

public interface RoleABoxAssertion extends ABoxAssertion {

	public URIConstant getFirstObject();

	public URIConstant getSecondObject();

	public Predicate getRole();

}
