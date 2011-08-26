package it.unibz.krdb.obda.owlrefplatform.core.ontology.imp;

import it.unibz.krdb.obda.model.Predicate;
import it.unibz.krdb.obda.model.URIConstant;
import it.unibz.krdb.obda.model.ValueConstant;
import it.unibz.krdb.obda.owlrefplatform.core.ontology.Class;
import it.unibz.krdb.obda.owlrefplatform.core.ontology.ClassDescription;
import it.unibz.krdb.obda.owlrefplatform.core.ontology.OntologyFactory;
import it.unibz.krdb.obda.owlrefplatform.core.ontology.Property;
import it.unibz.krdb.obda.owlrefplatform.core.ontology.PropertySomeClassRestriction;
import it.unibz.krdb.obda.owlrefplatform.core.ontology.PropertySomeRestriction;

import java.net.URI;

public class OntologyFactoryImpl implements OntologyFactory {

	private static OntologyFactoryImpl	instance	= new OntologyFactoryImpl();

	public static OntologyFactory getInstance() {
		return instance;
	}

	public ClassAssertionImpl createClassAssertion(Predicate concept, URIConstant object) {
		return new ClassAssertionImpl(concept, object);
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see it.unibz.krdb.obda.owlrefplatform.core.ontology.imp.BasicFactoryI#
	 * createOntologyImpl(java.net.URI)
	 */
	@Override
	public OntologyImpl createOntology(URI uri) {
		return new OntologyImpl(uri);
	}
	
	@Override
	public OntologyImpl createOntology() {
		return new OntologyImpl(null);
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see it.unibz.krdb.obda.owlrefplatform.core.ontology.imp.BasicFactoryI#
	 * createClassImpl(it.unibz.krdb.obda.model.Predicate)
	 */
	@Override
	public ClassImpl createClass(Predicate p) {
		return new ClassImpl(p);
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see it.unibz.krdb.obda.owlrefplatform.core.ontology.imp.BasicFactoryI#
	 * createSubPropertyAxiom
	 * (it.unibz.krdb.obda.owlrefplatform.core.ontology.Property,
	 * it.unibz.krdb.obda.owlrefplatform.core.ontology.Property)
	 */
	@Override
	public SubPropertyAxiomImpl createSubPropertyAxiom(Property included, Property including) {
		return new SubPropertyAxiomImpl(included, including);
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see it.unibz.krdb.obda.owlrefplatform.core.ontology.imp.BasicFactoryI#
	 * createSubClassAxiom
	 * (it.unibz.krdb.obda.owlrefplatform.core.ontology.ClassDescription,
	 * it.unibz.krdb.obda.owlrefplatform.core.ontology.ClassDescription)
	 */
	@Override
	public SubClassAxiomImpl createSubClassAxiom(ClassDescription concept1, ClassDescription concept2) {
		return new SubClassAxiomImpl(concept1, concept2);
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see it.unibz.krdb.obda.owlrefplatform.core.ontology.imp.BasicFactoryI#
	 * createPropertySomeRestriction(it.unibz.krdb.obda.model.Predicate,
	 * boolean)
	 */
	@Override
	public PropertySomeRestrictionImpl createPropertySomeRestriction(Predicate p, boolean isInverse) {
		return new PropertySomeRestrictionImpl(p, isInverse);
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see it.unibz.krdb.obda.owlrefplatform.core.ontology.imp.BasicFactoryI#
	 * createPropertySomeClassRestriction(it.unibz.krdb.obda.model.Predicate,
	 * boolean, it.unibz.krdb.obda.owlrefplatform.core.ontology.Class)
	 */
	@Override
	public PropertySomeClassRestrictionImpl createPropertySomeClassRestriction(Predicate p, boolean isInverse, Class filler) {
		return new PropertySomeClassRestrictionImpl(p, isInverse, filler);
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see it.unibz.krdb.obda.owlrefplatform.core.ontology.imp.BasicFactoryI#
	 * createPropertyFunctionalAxiom
	 * (it.unibz.krdb.obda.owlrefplatform.core.ontology.Property)
	 */
	@Override
	public PropertyFunctionalAxiomImpl createPropertyFunctionalAxiom(Property role) {
		return new PropertyFunctionalAxiomImpl(role);
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see it.unibz.krdb.obda.owlrefplatform.core.ontology.imp.BasicFactoryI#
	 * createObjectPropertyAssertion(it.unibz.krdb.obda.model.Predicate,
	 * it.unibz.krdb.obda.model.URIConstant,
	 * it.unibz.krdb.obda.model.URIConstant)
	 */
	@Override
	public ObjectPropertyAssertionImpl createObjectPropertyAssertion(Predicate role, URIConstant o1, URIConstant o2) {
		return new ObjectPropertyAssertionImpl(role, o1, o2);
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see it.unibz.krdb.obda.owlrefplatform.core.ontology.imp.BasicFactoryI#
	 * createDataPropertyAssertion(it.unibz.krdb.obda.model.Predicate,
	 * it.unibz.krdb.obda.model.URIConstant,
	 * it.unibz.krdb.obda.model.ValueConstant)
	 */
	@Override
	public DataPropertyAssertionImpl createDataPropertyAssertion(Predicate attribute, URIConstant o1, ValueConstant o2) {
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
		return createClass(p);

	}

	public Property getProperty(Predicate p, boolean inverse) {
		return new PropertyImpl(p, inverse);
	}

	public Property getProperty(Predicate p) {
		return getProperty(p, false);
	}

}
