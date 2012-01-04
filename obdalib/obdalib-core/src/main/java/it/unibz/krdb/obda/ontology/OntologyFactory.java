package it.unibz.krdb.obda.ontology;

import it.unibz.krdb.obda.model.Predicate;
import it.unibz.krdb.obda.model.URIConstant;
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
	
	public Ontology createOntology(URI uri);
	
	public Ontology createOntology();

	public SubDescriptionAxiom createSubPropertyAxiom(Property included, Property including);

	public SubDescriptionAxiom createSubClassAxiom(ClassDescription concept1, ClassDescription concept2);

	public PropertySomeRestriction createPropertySomeRestriction(Predicate p, boolean isInverse);

	public PropertySomeClassRestriction createPropertySomeClassRestriction(Predicate p, boolean isInverse, OClass filler);

	public PropertyFunctionalAxiom createPropertyFunctionalAxiom(Property role);

	public ObjectPropertyAssertion createObjectPropertyAssertion(Predicate role, URIConstant o1, URIConstant o2);

	public DataPropertyAssertion createDataPropertyAssertion(Predicate attribute, URIConstant o1, ValueConstant o2);
	
	public ClassAssertion createClassAssertion(Predicate concept, URIConstant object);
	
	


}
