package it.unibz.krdb.obda.ontology.impl;

/*
 * #%L
 * ontop-obdalib-core
 * %%
 * Copyright (C) 2009 - 2013 Free University of Bozen-Bolzano
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
import it.unibz.krdb.obda.ontology.Assertion;
import it.unibz.krdb.obda.ontology.ClassAssertion;
import it.unibz.krdb.obda.ontology.ClassDescription;
import it.unibz.krdb.obda.ontology.DataPropertyAssertion;
import it.unibz.krdb.obda.ontology.DataType;
import it.unibz.krdb.obda.ontology.DisjointClassAxiom;
import it.unibz.krdb.obda.ontology.DisjointDataPropertyAxiom;
import it.unibz.krdb.obda.ontology.DisjointObjectPropertyAxiom;
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


public class OntologyFactoryImpl implements OntologyFactory {

	private static OntologyFactoryImpl instance = new OntologyFactoryImpl();

	private OBDADataFactory ofac = OBDADataFactoryImpl.getInstance();

	public static OntologyFactory getInstance() {
		return instance;
	}

	@Override
	public ClassAssertion createClassAssertion(Predicate concept, ObjectConstant object) {
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
	public SubDescriptionAxiom createSubPropertyAxiom(Property included, Property including) {
		return new SubPropertyAxiomImpl(included, including);
	}

	@Override
	public SubDescriptionAxiom createSubClassAxiom(ClassDescription concept1, ClassDescription concept2) {
		return new SubClassAxiomImpl(concept1, concept2);
	}

	@Override
	public PropertySomeRestriction createPropertySomeRestriction(Predicate p, boolean isInverse) {
		return new PropertySomeRestrictionImpl(p, isInverse);
	}

	@Override
	public PropertyFunctionalAxiom createPropertyFunctionalAxiom(Property role) {
		return new PropertyFunctionalAxiomImpl(role);
	}

	@Override
	public ObjectPropertyAssertion createObjectPropertyAssertion(Predicate role, ObjectConstant o1, ObjectConstant o2) {
		return new ObjectPropertyAssertionImpl(role, o1, o2);
	}

	@Override
	public DataPropertyAssertion createDataPropertyAssertion(Predicate attribute, ObjectConstant o1, ValueConstant o2) {
		return new DataPropertyAssertionImpl(attribute, o1, o2);
	}

	public PropertySomeRestriction getPropertySomeRestriction(Predicate p, boolean inverse) {
		if (p.getArity() != 2) {
			throw new IllegalArgumentException("Roles must have arity = 2");
		}
		return new PropertySomeRestrictionImpl(p, inverse);
	}

	public PropertySomeClassRestriction createPropertySomeClassRestriction(Predicate p, boolean isInverse, OClass filler) {
		if (p.getArity() != 2) {
			throw new IllegalArgumentException("Roles must have arity = 2");
		}
		if (filler == null) {
			throw new IllegalArgumentException("Must provide an atomic concept as a filler");
		}
		return new PropertySomeClassRestrictionImpl(p, isInverse, filler);
	}

	@Override
	public PropertySomeDataTypeRestriction createPropertySomeDataTypeRestriction(Predicate p, boolean isInverse, DataType filler) {
		if (p.getArity() != 2) {
			throw new IllegalArgumentException("Roles must have arity = 2");
		}
		if (filler == null) {
			throw new IllegalArgumentException("Must provide a data type object as the filler");
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
	public Property createObjectProperty(String uri) {
		Predicate prop = ofac.getObjectPropertyPredicate(uri);
		return createProperty(prop);
	}

	@Override
	public Property createDataProperty(String p) {
		Predicate prop = ofac.getDataPropertyPredicate(p);
		return createProperty(prop);
	}



	@Override
	public DataType createDataType(Predicate p) {
		return new DataTypeImpl(p);
	}

	@Override
	public Assertion createPropertyAssertion(Predicate attribute, ObjectConstant o1, Constant o2) {
		if (o2 instanceof ObjectConstant) {
			return createObjectPropertyAssertion(attribute, o1, (ObjectConstant) o2);
		}
		return createDataPropertyAssertion(attribute, o1, (ValueConstant) o2);
	}

	@Override
	public DisjointClassAxiom createDisjointClassAxiom(OClass c1, OClass c2) {
		return new DisjointClassAxiomImpl(c1, c2);
	}

	@Override
	public DisjointDataPropertyAxiom createDisjointDataPropertyAxiom(
			Predicate p1, Predicate p2) {
			return new DisjointDataPropertyAxiomImpl(p1, p2);
	}
	
	@Override
	public DisjointObjectPropertyAxiom createDisjointObjectPropertyAxiom(
			Predicate p1, Predicate p2) {
			return new DisjointObjectPropertyAxiomImpl(p1, p2);
	}

}
