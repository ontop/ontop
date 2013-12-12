package it.unibz.krdb.obda.ontology;

import it.unibz.krdb.obda.model.ObjectConstant;
import it.unibz.krdb.obda.model.Predicate;
import it.unibz.krdb.obda.model.ValueConstant;

/***
 * An assertion for data properties, e.g., name(mariano,"Mariano Rodriguez").
 * Corresponds to RDF triple: :mariano :name "Mariano Rodriguez".
 */
public interface DataPropertyAssertion extends Assertion {

	public ObjectConstant getObject();

	public ValueConstant getValue();

	/***
	 * Use get predicate instead
	 * 
	 * @return
	 */
	@Deprecated
	public Predicate getAttribute();
}
