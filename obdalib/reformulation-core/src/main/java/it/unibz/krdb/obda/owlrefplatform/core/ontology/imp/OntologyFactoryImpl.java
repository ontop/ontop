package it.unibz.krdb.obda.owlrefplatform.core.ontology.imp;

import it.unibz.krdb.obda.model.OBDADataFactory;
import it.unibz.krdb.obda.model.Predicate;
import it.unibz.krdb.obda.model.URIConstant;
import it.unibz.krdb.obda.model.ValueConstant;
import it.unibz.krdb.obda.model.impl.OBDADataFactoryImpl;
import it.unibz.krdb.obda.owlrefplatform.core.ontology.ClassAssertion;
import it.unibz.krdb.obda.owlrefplatform.core.ontology.ClassDescription;
import it.unibz.krdb.obda.owlrefplatform.core.ontology.DataPropertyAssertion;
import it.unibz.krdb.obda.owlrefplatform.core.ontology.OClass;
import it.unibz.krdb.obda.owlrefplatform.core.ontology.ObjectPropertyAssertion;
import it.unibz.krdb.obda.owlrefplatform.core.ontology.Ontology;
import it.unibz.krdb.obda.owlrefplatform.core.ontology.OntologyFactory;
import it.unibz.krdb.obda.owlrefplatform.core.ontology.Property;
import it.unibz.krdb.obda.owlrefplatform.core.ontology.PropertyFunctionalAxiom;
import it.unibz.krdb.obda.owlrefplatform.core.ontology.PropertySomeClassRestriction;
import it.unibz.krdb.obda.owlrefplatform.core.ontology.PropertySomeRestriction;
import it.unibz.krdb.obda.owlrefplatform.core.ontology.SubDescriptionAxiom;

import java.net.URI;

public class OntologyFactoryImpl implements OntologyFactory {

	private static OntologyFactoryImpl	instance	= new OntologyFactoryImpl();

	private OBDADataFactory				ofac		= OBDADataFactoryImpl.getInstance();

	public static OntologyFactory getInstance() {
		return instance;
	}

	public ClassAssertion createClassAssertion(Predicate concept, URIConstant object) {
		return new ClassAssertionImpl(concept, object);
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see it.unibz.krdb.obda.owlrefplatform.core.ontology.imp.BasicFactoryI#
	 * createOntologyImpl(java.net.URI)
	 */
	@Override
	public Ontology createOntology(URI uri) {
		return new OntologyImpl(uri);
	}

	@Override
	public Ontology createOntology() {
		return new OntologyImpl(null);
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
	public SubDescriptionAxiom createSubPropertyAxiom(Property included, Property including) {
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
	public SubDescriptionAxiom createSubClassAxiom(ClassDescription concept1, ClassDescription concept2) {
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
	public PropertySomeRestriction createPropertySomeRestriction(Predicate p, boolean isInverse) {
		return new PropertySomeRestrictionImpl(p, isInverse);
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see it.unibz.krdb.obda.owlrefplatform.core.ontology.imp.BasicFactoryI#
	 * createPropertyFunctionalAxiom
	 * (it.unibz.krdb.obda.owlrefplatform.core.ontology.Property)
	 */
	@Override
	public PropertyFunctionalAxiom createPropertyFunctionalAxiom(Property role) {
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
	public ObjectPropertyAssertion createObjectPropertyAssertion(Predicate role, URIConstant o1, URIConstant o2) {
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
	public DataPropertyAssertion createDataPropertyAssertion(Predicate attribute, URIConstant o1, ValueConstant o2) {
		return new DataPropertyAssertionImpl(attribute, o1, o2);
	}

	public PropertySomeRestriction getPropertySomeRestriction(Predicate p, boolean inverse) {
		if (p.getArity() != 2) {
			throw new IllegalArgumentException("Roles must have arity = 2");
		}
		return new PropertySomeRestrictionImpl(p, inverse);
	}

	public PropertySomeClassRestriction createPropertySomeClassRestriction(Predicate p, boolean inverse, OClass filler) {
		if (p.getArity() != 2) {
			throw new IllegalArgumentException("Roles must have arity = 2");
		}
		if (filler == null)
			throw new IllegalArgumentException("Must provide an atomic concept as a filler");
		return new PropertySomeClassRestrictionImpl(p, inverse, filler);
	}

	public OClass createClass(Predicate p) {
		if (p.getArity() != 1) {
			throw new IllegalArgumentException("Concepts must have arity = 1");
		}
		return new ClassImpl(p);

	}

	public Property createProperty(Predicate p, boolean inverse) {
		return new PropertyImpl(p, inverse);
	}

	public Property createProperty(Predicate p) {
		return new PropertyImpl(p, false);
	}

	@Override
	public OClass createClass(URI c) {
		Predicate classp = ofac.getClassPredicate(c);
		return createClass(classp);
	}

	@Override
	public Property createObjectProperty(URI uri, boolean inverse) {
		Predicate prop = ofac.getObjectPropertyPredicate(uri);
		return createProperty(prop, inverse);
	}

	@Override
	public Property createObjectProperty(URI uri) {
		Predicate prop = ofac.getObjectPropertyPredicate(uri);
		return createProperty(prop);
	}

	@Override
	public Property createDataProperty(URI p) {
		Predicate prop = ofac.getDataPropertyPredicate(p);
		return createProperty(prop);
	}

	@Override
	public OClass createClass(String c) {
		return createClass(URI.create(c));
	}

	@Override
	public Property createObjectProperty(String uri, boolean inverse) {
		return createObjectProperty(URI.create(uri), inverse);
	}

	@Override
	public Property createObjectProperty(String uri) {
		return createObjectProperty(URI.create(uri));
	}

	@Override
	public Property createDataProperty(String p) {
		return createDataProperty(URI.create(p));
	}

}
