package it.unibz.krdb.obda.owlrefplatform.core.ontology.imp;

import java.net.URI;

import it.unibz.krdb.obda.model.Predicate;
import it.unibz.krdb.obda.model.URIConstant;
import it.unibz.krdb.obda.model.ValueConstant;
import it.unibz.krdb.obda.owlrefplatform.core.ontology.Class;
import it.unibz.krdb.obda.owlrefplatform.core.ontology.ClassDescription;
import it.unibz.krdb.obda.owlrefplatform.core.ontology.OntologyFactory;
import it.unibz.krdb.obda.owlrefplatform.core.ontology.PropertySomeRestriction;
import it.unibz.krdb.obda.owlrefplatform.core.ontology.PropertySomeClassRestriction;
import it.unibz.krdb.obda.owlrefplatform.core.ontology.Property;

public class BasicDescriptionFactory implements OntologyFactory {

	public static ClassAssertionImpl createClassAssertionImpl(Predicate concept, URIConstant object) {
		return new ClassAssertionImpl(concept, object);
	}

	public static OntologyImpl createOntologyImpl(URI uri) {
		return new OntologyImpl(uri);
	}

	public static ClassImpl createClassImpl(Predicate p) {
		return new ClassImpl(p);
	}

	public static SubPropertyAxiomImpl createSubPropertyAxiom(Property included, Property including) {
		return new SubPropertyAxiomImpl(included, including);
	}

	public static SubClassAxiomImpl createSubClassAxiom(ClassDescription concept1, ClassDescription concept2) {
		return new SubClassAxiomImpl(concept1, concept2);
	}

	public static PropertySomeRestrictionImpl createPropertySomeRestriction(Predicate p, boolean isInverse) {
		return new PropertySomeRestrictionImpl(p, isInverse);
	}

	public static PropertySomeClassRestrictionImpl createPropertySomeClassRestriction(Predicate p, boolean isInverse, Class filler) {
		return new PropertySomeClassRestrictionImpl(p, isInverse, filler);
	}

	public static PropertyFunctionalAxiomImpl createPropertyFunctionalAxiom(Property role) {
		return new PropertyFunctionalAxiomImpl(role);
	}

	public static ObjectPropertyAssertionImpl createObjectPropertyAssertion(Predicate role, URIConstant o1, URIConstant o2) {
		return new ObjectPropertyAssertionImpl(role, o1, o2);
	}

	public static DataPropertyAssertionImpl createDataPropertyAssertion(Predicate attribute, URIConstant o1, ValueConstant o2) {
		return new DataPropertyAssertionImpl(attribute, o1, o2);
	}

	public PropertySomeRestriction getPropertySomeRestriction(Predicate p, boolean inverse) {
		if (p.getArity() != 2) {
			throw new IllegalArgumentException("Roles must have arity = 2");
		}
		return createPropertySomeRestriction(p, inverse);
	}
	
	public PropertySomeClassRestriction getPropertySomeClassRestriction(Predicate p, boolean inverse, Class filler) {
		if (p.getArity() != 2) {
			throw new IllegalArgumentException("Roles must have arity = 2");
		}
		if (filler == null)
			throw new IllegalArgumentException("Must provide an atomic concept as a filler");
		return createPropertySomeClassRestriction(p, inverse, filler);
	}


	public Class getClass(Predicate p) {
		if (p.getArity() != 1) {
			throw new IllegalArgumentException("Concepts must have arity = 1");
		}
		return createClassImpl(p);

	}

	public Property getProperty(Predicate p, boolean inverse) {
		return new PropertyImpl(p, inverse);
	}

	public Property getProperty(Predicate p) {
		return getProperty(p, false);
	}

}
