package it.unibz.krdb.obda.ontology.impl;

/*
 * #%L
 * ontop-obdalib-core
 * %%
 * Copyright (C) 2009 - 2014 Free University of Bozen-Bolzano
 * %%
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 * 
 *      http://www.apache.org/licenses/LICENSE-2.0
 * 
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 * #L%
 */

import it.unibz.krdb.obda.model.Constant;
import it.unibz.krdb.obda.model.OBDADataFactory;
import it.unibz.krdb.obda.model.ObjectConstant;
import it.unibz.krdb.obda.model.Predicate;
import it.unibz.krdb.obda.model.ValueConstant;
import it.unibz.krdb.obda.model.impl.OBDADataFactoryImpl;
import it.unibz.krdb.obda.ontology.BasicClassDescription;
import it.unibz.krdb.obda.ontology.ClassAssertion;
import it.unibz.krdb.obda.ontology.DataType;
import it.unibz.krdb.obda.ontology.DisjointClassesAxiom;
import it.unibz.krdb.obda.ontology.DisjointPropertiesAxiom;
import it.unibz.krdb.obda.ontology.OClass;
import it.unibz.krdb.obda.ontology.Ontology;
import it.unibz.krdb.obda.ontology.OntologyFactory;
import it.unibz.krdb.obda.ontology.Property;
import it.unibz.krdb.obda.ontology.FunctionalPropertyAxiom;
import it.unibz.krdb.obda.ontology.PropertyAssertion;
import it.unibz.krdb.obda.ontology.PropertySomeRestriction;
import it.unibz.krdb.obda.ontology.SubClassOfAxiom;
import it.unibz.krdb.obda.ontology.SubPropertyOfAxiom;


public class OntologyFactoryImpl implements OntologyFactory {

	private static final OntologyFactoryImpl instance = new OntologyFactoryImpl();

	private final OBDADataFactory ofac = OBDADataFactoryImpl.getInstance();

	private OntologyFactoryImpl() {
		// NO-OP to make the default constructor private
	}
	
	public static OntologyFactory getInstance() {
		return instance;
	}

	@Override
	public ClassAssertion createClassAssertion(OClass concept, ObjectConstant object) {
		return new ClassAssertionImpl(concept, object);
	}

	@Override
	public Ontology createOntology(String uri) {
		return new OntologyImpl(uri);
	}

	@Override
	public Ontology createOntology() {
		return new OntologyImpl(null);
	}
	
	@Override
	public SubPropertyOfAxiom createSubPropertyAxiom(Property included, Property including) {
		return new SubPropertyOfAxiomImpl(included, including);
	}

	@Override
	public SubClassOfAxiom createSubClassAxiom(BasicClassDescription concept1, BasicClassDescription concept2) {
		return new SubClassOfAxiomImpl(concept1, concept2);
	}

	@Override
	public PropertySomeRestriction createPropertySomeRestriction(Predicate p, boolean isInverse) {
		Property prop = createProperty(p, isInverse);
		return new PropertySomeRestrictionImpl(prop);
	}

	@Override
	public FunctionalPropertyAxiom createPropertyFunctionalAxiom(Property role) {
		return new FunctionalPropertyAxiomImpl(role);
	}

	
	public PropertyAssertion createObjectPropertyAssertion(Property role, ObjectConstant o1, ObjectConstant o2) {
		return new PropertyAssertionImpl(role, o1, o2);
	}

	@Override
	public OClass createClass(Predicate p) {
		if (p.getArity() != 1) {
			throw new IllegalArgumentException("Concepts must have arity = 1");
		}
		return new ClassImpl(p);
	}

	@Override
	public Property createProperty(Predicate p, boolean inverse) {
		return new PropertyImpl(p, inverse);
	}

	@Override
	public OClass createClass(String c) {
		Predicate classp = ofac.getClassPredicate(c);
		return createClass(classp);
	}

	@Override
	public Property createObjectProperty(String uri, boolean inverse) {
		Predicate prop = ofac.getObjectPropertyPredicate(uri);
		return createProperty(prop, inverse);
	}

	@Override
	public Property createDataProperty(String p) {
		Predicate prop = ofac.getDataPropertyPredicate(p);
		return createProperty(prop, false);
	}



	@Override
	public DataType createDataType(Predicate p) {
		return new DataTypeImpl(p);
	}

	@Override
	public PropertyAssertion createPropertyAssertion(Property attribute, ObjectConstant o1, Constant o2) {
		return new PropertyAssertionImpl(attribute, o1, o2);
	}

	@Override
	public DisjointClassesAxiom createDisjointClassAxiom(BasicClassDescription c1, BasicClassDescription c2) {
		return new DisjointClassesAxiomImpl(c1, c2);
	}

	@Override
	public DisjointPropertiesAxiom createDisjointPropertiesAxiom(Property p1, Property p2) {
			return new DisjointPropertiesAxiomImpl(p1, p2);
	}
	
	@Override
	public PropertySomeRestriction createPropertySomeRestriction(Property role) {
		return new PropertySomeRestrictionImpl(role);
	}

}
