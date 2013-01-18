package it.unibz.krdb.obda.ontology.impl;

import it.unibz.krdb.obda.model.Constant;
import it.unibz.krdb.obda.model.OBDADataFactory;
import it.unibz.krdb.obda.model.ObjectConstant;
import it.unibz.krdb.obda.model.Predicate;
import it.unibz.krdb.obda.model.ValueConstant;
import it.unibz.krdb.obda.model.impl.OBDADataFactoryImpl;
import it.unibz.krdb.obda.ontology.Assertion;
import it.unibz.krdb.obda.ontology.ClassAssertion;
import it.unibz.krdb.obda.ontology.ClassDescription;
import it.unibz.krdb.obda.ontology.DataPropertyAssertion;
import it.unibz.krdb.obda.ontology.DataType;
import it.unibz.krdb.obda.ontology.OClass;
import it.unibz.krdb.obda.ontology.ObjectPropertyAssertion;
import it.unibz.krdb.obda.ontology.Ontology;
import it.unibz.krdb.obda.ontology.OntologyFactory;
import it.unibz.krdb.obda.ontology.Property;
import it.unibz.krdb.obda.ontology.PropertyFunctionalAxiom;
import it.unibz.krdb.obda.ontology.PropertySomeClassRestriction;
import it.unibz.krdb.obda.ontology.PropertySomeDataTypeRestriction;
import it.unibz.krdb.obda.ontology.PropertySomeRestriction;
import it.unibz.krdb.obda.ontology.SubDescriptionAxiom;

import java.net.URI;

import com.hp.hpl.jena.iri.IRI;
import com.hp.hpl.jena.iri.IRIFactory;

public class OntologyFactoryImpl implements OntologyFactory {

	private static OntologyFactoryImpl instance = new OntologyFactoryImpl();
	private IRIFactory ifac = OBDADataFactoryImpl.getIRIFactory();

	private OBDADataFactory ofac = OBDADataFactoryImpl.getInstance();

	public static OntologyFactory getInstance() {
		return instance;
	}

	@Override
	public ClassAssertion createClassAssertion(Predicate concept,
			ObjectConstant object) {
		return new ClassAssertionImpl(concept, object);
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see it.unibz.krdb.obda.owlrefplatform.core.ontology.imp.BasicFactoryI#
	 * createOntologyImpl(java.net.URI)
	 */
	@Override
	public Ontology createOntology(IRI uri) {
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
	public SubDescriptionAxiom createSubPropertyAxiom(Property included,
			Property including) {
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
	public SubDescriptionAxiom createSubClassAxiom(ClassDescription concept1,
			ClassDescription concept2) {
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
	public PropertySomeRestriction createPropertySomeRestriction(Predicate p,
			boolean isInverse) {
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
	public ObjectPropertyAssertion createObjectPropertyAssertion(
			Predicate role, ObjectConstant o1, ObjectConstant o2) {
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
	public DataPropertyAssertion createDataPropertyAssertion(
			Predicate attribute, ObjectConstant o1, ValueConstant o2) {
		return new DataPropertyAssertionImpl(attribute, o1, o2);
	}

	public PropertySomeRestriction getPropertySomeRestriction(Predicate p,
			boolean inverse) {
		if (p.getArity() != 2) {
			throw new IllegalArgumentException("Roles must have arity = 2");
		}
		return new PropertySomeRestrictionImpl(p, inverse);
	}

	public PropertySomeClassRestriction createPropertySomeClassRestriction(
			Predicate p, boolean isInverse, OClass filler) {
		if (p.getArity() != 2) {
			throw new IllegalArgumentException("Roles must have arity = 2");
		}
		if (filler == null) {
			throw new IllegalArgumentException(
					"Must provide an atomic concept as a filler");
		}
		return new PropertySomeClassRestrictionImpl(p, isInverse, filler);
	}

	@Override
	public PropertySomeDataTypeRestriction createPropertySomeDataTypeRestriction(
			Predicate p, boolean isInverse, DataType filler) {
		if (p.getArity() != 2) {
			throw new IllegalArgumentException("Roles must have arity = 2");
		}
		if (filler == null) {
			throw new IllegalArgumentException(
					"Must provide a data type object as the filler");
		}
		return new PropertySomeDataTypeRestrictionImpl(p, isInverse, filler);
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
	public OClass createClass(IRI c) {
		Predicate classp = ofac.getClassPredicate(c);
		return createClass(classp);
	}

	@Override
	public Property createObjectProperty(IRI uri, boolean inverse) {
		Predicate prop = ofac.getObjectPropertyPredicate(uri);
		return createProperty(prop, inverse);
	}

	@Override
	public Property createObjectProperty(IRI uri) {
		Predicate prop = ofac.getObjectPropertyPredicate(uri);
		return createProperty(prop);
	}

	@Override
	public Property createDataProperty(IRI p) {
		Predicate prop = ofac.getDataPropertyPredicate(p);
		return createProperty(prop);
	}

	@Override
	public OClass createClass(String c) {
		return createClass(ifac.construct(c));
	}

	@Override
	public Property createObjectProperty(String uri, boolean inverse) {
		return createObjectProperty(ifac.construct(uri), inverse);
	}

	@Override
	public Property createObjectProperty(String uri) {
		return createObjectProperty(ifac.construct(uri));
	}

	@Override
	public Property createDataProperty(String p) {
		return createDataProperty(ifac.construct(p));
	}

	@Override
	public DataType createDataType(Predicate p) {
		return new DataTypeImpl(p);
	}

	@Override
	public Assertion createPropertyAssertion(Predicate attribute,
			ObjectConstant o1, Constant o2) {
		if (o2 instanceof ObjectConstant)
			return createObjectPropertyAssertion(attribute, o1,
					(ObjectConstant) o2);

		return createDataPropertyAssertion(attribute, o1, (ValueConstant) o2);

	}
}
