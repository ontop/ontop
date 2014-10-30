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

import java.util.Set;

import it.unibz.krdb.obda.model.Constant;
import it.unibz.krdb.obda.model.OBDADataFactory;
import it.unibz.krdb.obda.model.ObjectConstant;
import it.unibz.krdb.obda.model.Predicate;
import it.unibz.krdb.obda.model.impl.OBDADataFactoryImpl;
import it.unibz.krdb.obda.ontology.BasicClassDescription;
import it.unibz.krdb.obda.ontology.ClassAssertion;
import it.unibz.krdb.obda.ontology.DataPropertyExpression;
import it.unibz.krdb.obda.ontology.DataPropertyRangeExpression;
import it.unibz.krdb.obda.ontology.DataRangeExpression;
import it.unibz.krdb.obda.ontology.Datatype;
import it.unibz.krdb.obda.ontology.DisjointClassesAxiom;
import it.unibz.krdb.obda.ontology.DisjointPropertiesAxiom;
import it.unibz.krdb.obda.ontology.OClass;
import it.unibz.krdb.obda.ontology.ObjectPropertyExpression;
import it.unibz.krdb.obda.ontology.Ontology;
import it.unibz.krdb.obda.ontology.OntologyFactory;
import it.unibz.krdb.obda.ontology.PropertyExpression;
import it.unibz.krdb.obda.ontology.FunctionalPropertyAxiom;
import it.unibz.krdb.obda.ontology.PropertyAssertion;
import it.unibz.krdb.obda.ontology.SomeValuesFrom;
import it.unibz.krdb.obda.ontology.ClassExpression;
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
	public Ontology createOntology() {
		return new OntologyImpl();
	}
	
	@Override
	public SubPropertyOfAxiom createSubPropertyAxiom(ObjectPropertyExpression included, ObjectPropertyExpression including) {
		return new SubPropertyOfAxiomImpl(included, including);
	}
	
	@Override
	public SubPropertyOfAxiom createSubPropertyAxiom(DataPropertyExpression included, DataPropertyExpression including) {
		return new SubPropertyOfAxiomImpl(included, including);
	}
	
	@Override
	public SubPropertyOfAxiom createSubPropertyAxiom(PropertyExpression included, PropertyExpression including) {
		return new SubPropertyOfAxiomImpl(included, including);
	}

	@Override
	public SubClassOfAxiom createSubClassAxiom(ClassExpression concept1, BasicClassDescription concept2) {
		return new SubClassOfAxiomImpl(concept1, concept2);
	}

	@Override
	public SubClassOfAxiom createSubClassAxiom(DataRangeExpression concept1, DataRangeExpression concept2) {
		return new SubClassOfAxiomImpl(concept1, concept2);
	}
	
	@Override
	public DataPropertyRangeExpression createDataPropertyRange(DataPropertyExpression role) {
		return new DataPropertyRangeExpressionImpl(role.getInverse());
	}
	
	@Override
	public FunctionalPropertyAxiom createPropertyFunctionalAxiom(PropertyExpression role) {
		return new FunctionalPropertyAxiomImpl(role);
	}

	
	public PropertyAssertion createObjectPropertyAssertion(PropertyExpression role, ObjectConstant o1, ObjectConstant o2) {
		return new PropertyAssertionImpl(role, o1, o2);
	}


	@Override
	public OClass createClass(String c) {
		Predicate classp = ofac.getClassPredicate(c);
		return new ClassImpl(classp);
	}

	@Override
	public ObjectPropertyExpression createObjectProperty(String uri) {
		Predicate prop = ofac.getObjectPropertyPredicate(uri);
		return new ObjectPropertyExpressionImpl(prop, false);
	}

	
	@Override
	public DataPropertyExpression createDataProperty(String p) {
		Predicate prop = ofac.getDataPropertyPredicate(p);
		return new DataPropertyExpressionImpl(prop, false);
	}

	@Override
	public PropertyExpression createProperty(String uri) {
		Predicate prop = ofac.getPredicate(uri, 2);
		return new ObjectPropertyExpressionImpl(prop, false);
	}


	@Override
	public Datatype createDataType(Predicate p) {
		return new DatatypeImpl(p);
	}

	@Override
	public PropertyAssertion createPropertyAssertion(PropertyExpression attribute, ObjectConstant o1, Constant o2) {
		return new PropertyAssertionImpl(attribute, o1, o2);
	}

	@Override
	public DisjointClassesAxiom createDisjointClassesAxiom(Set<ClassExpression> classes) {
		return new DisjointClassesAxiomImpl(classes);
	}

	@Override
	public DisjointPropertiesAxiom createDisjointPropertiesAxiom(Set<PropertyExpression> props) {
			return new DisjointPropertiesAxiomImpl(props);
	}
	
	@Override
	public SomeValuesFrom createPropertySomeRestriction(PropertyExpression role) {
		return new PropertySomeRestrictionImpl(role);
	}
}
