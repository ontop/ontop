package it.unibz.krdb.obda.owlrefplatform.core.ontology.imp;

import it.unibz.krdb.obda.model.Predicate;
import it.unibz.krdb.obda.owlrefplatform.core.ontology.Class;
import it.unibz.krdb.obda.owlrefplatform.core.ontology.OntologyFactory;
import it.unibz.krdb.obda.owlrefplatform.core.ontology.PropertySomeDescription;
import it.unibz.krdb.obda.owlrefplatform.core.ontology.PropertySomeClassDescription;
import it.unibz.krdb.obda.owlrefplatform.core.ontology.Property;

public class BasicDescriptionFactory implements OntologyFactory {

	public PropertySomeDescription getExistentialConceptDescription(Predicate p, boolean inverse) {
		if (p.getArity() != 2) {
			throw new IllegalArgumentException("Roles must have arity = 2");
		}
		return new PropertySomeDescriptionImpl(p, inverse);
	}
	
	public PropertySomeClassDescription getExistentialConceptDescription(Predicate p, boolean inverse, Class filler) {
		if (p.getArity() != 2) {
			throw new IllegalArgumentException("Roles must have arity = 2");
		}
		if (filler == null)
			throw new IllegalArgumentException("Must provide an atomic concept as a filler");
		return new PropertySomeClassDescriptionImpl(p, inverse, filler);
	}


	public Class getAtomicConceptDescription(Predicate p) {
		if (p.getArity() != 1) {
			throw new IllegalArgumentException("Concepts must have arity = 1");
		}
		return new ClassImpl(p);

	}

	public Property getRoleDescription(Predicate p, boolean inverse) {
		return new PropertyImpl(p, inverse);
	}

	public Property getRoleDescription(Predicate p) {
		return getRoleDescription(p, false);
	}

}
