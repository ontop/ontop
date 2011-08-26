package it.unibz.krdb.obda.owlrefplatform.core.ontology;

import it.unibz.krdb.obda.model.Predicate;

public interface OntologyFactory {

	public PropertySomeDescription getExistentialConceptDescription(Predicate p, boolean inverse);
	
	public PropertySomeClassDescription getExistentialConceptDescription(Predicate p, boolean inverse, Class filler);

	public Class getAtomicConceptDescription(Predicate p);

	public Property getRoleDescription(Predicate p, boolean inverse);

	public Property getRoleDescription(Predicate p);

}
