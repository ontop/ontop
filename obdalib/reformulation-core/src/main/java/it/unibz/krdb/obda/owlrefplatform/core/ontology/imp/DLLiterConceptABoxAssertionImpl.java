package it.unibz.krdb.obda.owlrefplatform.core.ontology.imp;

import it.unibz.krdb.obda.model.Predicate;
import it.unibz.krdb.obda.model.URIConstant;
import it.unibz.krdb.obda.owlrefplatform.core.ontology.ConceptABoxAssertion;

public class DLLiterConceptABoxAssertionImpl implements ConceptABoxAssertion {

	URIConstant	object	= null;

	Predicate	concept	= null;

	public DLLiterConceptABoxAssertionImpl(Predicate concept, URIConstant object) {
		this.object = object;
		this.concept = concept;
	}

	@Override
	public URIConstant getObject() {
		return object;

	}

	@Override
	public Predicate getConcept() {
		return concept;
	}

	public String toString() {
		return concept.toString() + "(" + object.toString() + ")";
	}
}
