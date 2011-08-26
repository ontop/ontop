package it.unibz.krdb.obda.owlrefplatform.core.ontology;

import it.unibz.krdb.obda.model.Predicate;
import it.unibz.krdb.obda.model.URIConstant;
import it.unibz.krdb.obda.model.ValueConstant;

/***
 * An assertion for data properties, e.g., name(mariano,"Mariano Rodriguez").
 * Corresponds to RDF triple: :mariano :name "Mariano Rodriguez".
 * 
 * @author Mariano Rodriguez Muro
 * 
 */
public interface DataPropertyAssertion extends Assertion {

	public URIConstant getObject();

	public ValueConstant getValue();

	public Predicate getAttribute();

}
