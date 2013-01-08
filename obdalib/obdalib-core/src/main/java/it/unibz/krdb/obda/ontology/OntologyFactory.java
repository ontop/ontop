package it.unibz.krdb.obda.ontology;

import it.unibz.krdb.obda.model.Constant;
import it.unibz.krdb.obda.model.ObjectConstant;
import it.unibz.krdb.obda.model.Predicate;
import it.unibz.krdb.obda.model.ValueConstant;

import java.net.URI;

public interface OntologyFactory {

	public PropertySomeRestriction getPropertySomeRestriction(Predicate p, boolean inverse);
	
	public OClass createClass(Predicate p);
	
	public OClass createClass(URI uri);
	
	public OClass createClass(String uri);

	public Property createProperty(Predicate p, boolean inverse);

	public Property createProperty(Predicate p);
	
	public Property createObjectProperty(URI uri, boolean inverse);
	
	public Property createObjectProperty(String uri, boolean inverse);
	
	public Property createObjectProperty(URI uri);
	
	public Property createObjectProperty(String uri);
	
	public Property createDataProperty(URI uri);
	
	public Property createDataProperty(String uri);
	
	public DataType createDataType(Predicate p);
	
	public Ontology createOntology(URI uri);
	
	public Ontology createOntology();

	public SubDescriptionAxiom createSubPropertyAxiom(Property included, Property including);

	public SubDescriptionAxiom createSubClassAxiom(ClassDescription concept1, ClassDescription concept2);

	public PropertySomeRestriction createPropertySomeRestriction(Predicate p, boolean isInverse);

	public PropertySomeClassRestriction createPropertySomeClassRestriction(Predicate p, boolean isInverse, OClass filler);

	public PropertySomeDataTypeRestriction createPropertySomeDataTypeRestriction(Predicate p, boolean isInverse, DataType filler);
	
	public PropertyFunctionalAxiom createPropertyFunctionalAxiom(Property role);

	public ObjectPropertyAssertion createObjectPropertyAssertion(Predicate role, ObjectConstant o1, ObjectConstant o2);

	public DataPropertyAssertion createDataPropertyAssertion(Predicate attribute, ObjectConstant o1, ValueConstant o2);
	
	public Assertion createPropertyAssertion(Predicate attribute, ObjectConstant o1, Constant o2);
	
	public ClassAssertion createClassAssertion(Predicate concept, ObjectConstant object);
}
