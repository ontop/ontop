package it.unibz.krdb.obda.owlrefplatform.core.ontology;

import it.unibz.krdb.obda.model.Predicate;

public interface OntologyFactory {

	public PropertySomeRestriction getPropertySomeRestriction(Predicate p, boolean inverse);
	
	public PropertySomeClassRestriction getPropertySomeClassRestriction(Predicate p, boolean inverse, Class filler);

	public Class getClass(Predicate p);

	public Property getProperty(Predicate p, boolean inverse);

	public Property getProperty(Predicate p);

}
