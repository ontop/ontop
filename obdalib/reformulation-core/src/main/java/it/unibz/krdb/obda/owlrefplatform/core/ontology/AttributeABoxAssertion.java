package it.unibz.krdb.obda.owlrefplatform.core.ontology;

import it.unibz.krdb.obda.model.Predicate;
import it.unibz.krdb.obda.model.URIConstant;
import it.unibz.krdb.obda.model.ValueConstant;

public interface AttributeABoxAssertion extends ABoxAssertion {

	public URIConstant getObject();

	public ValueConstant getValue();

	public Predicate getAttribute();

}
