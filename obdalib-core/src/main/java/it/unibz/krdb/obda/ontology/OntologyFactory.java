/*
 * Copyright (C) 2009-2013, Free University of Bozen Bolzano
 * This source code is available under the terms of the Affero General Public
 * License v3.
 * 
 * Please see LICENSE.txt for full license terms, including the availability of
 * proprietary exceptions.
 */
package it.unibz.krdb.obda.ontology;

import it.unibz.krdb.obda.model.Constant;
import it.unibz.krdb.obda.model.ObjectConstant;
import it.unibz.krdb.obda.model.Predicate;
import it.unibz.krdb.obda.model.ValueConstant;

public interface OntologyFactory {

	public PropertySomeRestriction getPropertySomeRestriction(Predicate p, boolean inverse);
	
	public OClass createClass(Predicate p);
	
	public OClass createClass(String uri);

	public Property createProperty(Predicate p, boolean inverse);

	public Property createProperty(Predicate p);
	
	public Property createObjectProperty(String uri, boolean inverse);
	
	public Property createObjectProperty(String uri);
	
	public Property createDataProperty(String uri);
	
	public DataType createDataType(Predicate p);
	
	public Ontology createOntology(String uri);
	
	public Ontology createOntology();

	public SubDescriptionAxiom createSubPropertyAxiom(Property included, Property including);

	public SubDescriptionAxiom createSubClassAxiom(ClassDescription concept1, ClassDescription concept2);

	public PropertySomeRestriction createPropertySomeRestriction(Predicate p, boolean isInverse);

	public PropertySomeClassRestriction createPropertySomeClassRestriction(Predicate p, boolean isInverse, OClass filler);

	public PropertySomeDataTypeRestriction createPropertySomeDataTypeRestriction(Predicate p, boolean isInverse, DataType filler);
	
	public PropertyFunctionalAxiom createPropertyFunctionalAxiom(Property role);
	
	public DisjointClassAxiom createDisjointClassAxiom(OClass c1, OClass c2);
	
	public DisjointDataPropertyAxiom createDisjointDataPropertyAxiom(Predicate p1, Predicate p2);
	
	public DisjointObjectPropertyAxiom createDisjointObjectPropertyAxiom(Predicate p1, Predicate p2);

	public ObjectPropertyAssertion createObjectPropertyAssertion(Predicate role, ObjectConstant o1, ObjectConstant o2);

	public DataPropertyAssertion createDataPropertyAssertion(Predicate attribute, ObjectConstant o1, ValueConstant o2);
	
	public Assertion createPropertyAssertion(Predicate attribute, ObjectConstant o1, Constant o2);
	
	public ClassAssertion createClassAssertion(Predicate concept, ObjectConstant object);
}
