package it.unibz.krdb.obda.owlrefplatform.core.ontology;

import java.net.URI;

import it.unibz.krdb.obda.model.Predicate;
import it.unibz.krdb.obda.model.URIConstant;
import it.unibz.krdb.obda.model.ValueConstant;
import it.unibz.krdb.obda.owlrefplatform.core.ontology.imp.ClassAssertionImpl;
import it.unibz.krdb.obda.owlrefplatform.core.ontology.imp.ClassImpl;
import it.unibz.krdb.obda.owlrefplatform.core.ontology.imp.DataPropertyAssertionImpl;
import it.unibz.krdb.obda.owlrefplatform.core.ontology.imp.ObjectPropertyAssertionImpl;
import it.unibz.krdb.obda.owlrefplatform.core.ontology.imp.OntologyImpl;
import it.unibz.krdb.obda.owlrefplatform.core.ontology.imp.PropertyFunctionalAxiomImpl;
import it.unibz.krdb.obda.owlrefplatform.core.ontology.imp.PropertySomeClassRestrictionImpl;
import it.unibz.krdb.obda.owlrefplatform.core.ontology.imp.PropertySomeRestrictionImpl;
import it.unibz.krdb.obda.owlrefplatform.core.ontology.imp.SubClassAxiomImpl;
import it.unibz.krdb.obda.owlrefplatform.core.ontology.imp.SubPropertyAxiomImpl;

public interface OntologyFactory {

	public PropertySomeRestriction getPropertySomeRestriction(Predicate p, boolean inverse);
	
	public PropertySomeClassRestriction getPropertySomeClassRestriction(Predicate p, boolean inverse, Class filler);

	public Class getClass(Predicate p);

	public Property getProperty(Predicate p, boolean inverse);

	public Property getProperty(Predicate p);
	
	public OntologyImpl createOntology(URI uri);
	
	public OntologyImpl createOntology();

	public ClassImpl createClass(Predicate p);

	public SubPropertyAxiomImpl createSubPropertyAxiom(Property included, Property including);

	public SubClassAxiomImpl createSubClassAxiom(ClassDescription concept1, ClassDescription concept2);

	public PropertySomeRestrictionImpl createPropertySomeRestriction(Predicate p, boolean isInverse);

	public PropertySomeClassRestrictionImpl createPropertySomeClassRestriction(Predicate p, boolean isInverse, Class filler);

	public PropertyFunctionalAxiomImpl createPropertyFunctionalAxiom(Property role);

	public ObjectPropertyAssertionImpl createObjectPropertyAssertion(Predicate role, URIConstant o1, URIConstant o2);

	public DataPropertyAssertionImpl createDataPropertyAssertion(Predicate attribute, URIConstant o1, ValueConstant o2);
	
	public ClassAssertionImpl createClassAssertion(Predicate concept, URIConstant object);


}
