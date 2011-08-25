package it.unibz.krdb.obda.owlrefplatform.core.ontology;

import it.unibz.krdb.obda.model.Predicate;
import it.unibz.krdb.obda.model.URIConstant;

public interface ConceptABoxAssertion extends ABoxAssertion {
	
	public URIConstant getObject();

	public Predicate getConcept();
}
