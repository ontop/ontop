package it.unibz.krdb.obda.ontology;

import it.unibz.krdb.obda.model.ObjectConstant;
import it.unibz.krdb.obda.model.Predicate;

/***
 * A data assertion for a class, e.g., Person(mariano), where mariano is an
 * object identifier.
 * 
 * These also correspond to rdf:type assertions, e.g., :mariano rdf:type
 * :Person.
 */
public interface ClassAssertion extends Assertion {

	// TODO: Change the method name to getSubject() because it gets the subject of the class assertion.
	public ObjectConstant getObject();

	/***
	 * Use get predicate instead
	 * 
	 * @return
	 */
	@Deprecated
	public Predicate getConcept();
}
