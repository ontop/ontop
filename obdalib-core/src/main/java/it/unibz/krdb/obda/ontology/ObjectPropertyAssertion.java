package it.unibz.krdb.obda.ontology;

import it.unibz.krdb.obda.model.ObjectConstant;
import it.unibz.krdb.obda.model.Predicate;

public interface ObjectPropertyAssertion extends Assertion {

	public ObjectConstant getFirstObject();

	public ObjectConstant getSecondObject();

	/***
	 * Use get predicate instead
	 * 
	 * @return
	 */
	@Deprecated
	public Predicate getRole();
}
