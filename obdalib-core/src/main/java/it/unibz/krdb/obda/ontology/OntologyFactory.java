package it.unibz.krdb.obda.ontology;

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
import it.unibz.krdb.obda.model.ObjectConstant;
import it.unibz.krdb.obda.model.Predicate;
import it.unibz.krdb.obda.model.ValueConstant;

public interface OntologyFactory {

	public OClass createClass(String uri);

	public PropertyExpression createProperty(String uri, boolean inverse);

	public PropertyExpression createObjectProperty(String uri, boolean inverse);

	public PropertyExpression createObjectPropertyInverse(PropertyExpression prop);
	
	public PropertyExpression createDataProperty(String uri);
	
	public Datatype createDataType(Predicate p);
	
	public Ontology createOntology();

	public SubPropertyOfAxiom createSubPropertyAxiom(PropertyExpression included, PropertyExpression including);

	public SubClassOfAxiom createSubClassAxiom(SubClassExpression concept1, BasicClassDescription concept2);


	
	public SomeValuesFrom createPropertySomeRestriction(PropertyExpression role);
	
	public SomeValuesFrom createDataPropertyRange(PropertyExpression role);
	
	public FunctionalPropertyAxiom createPropertyFunctionalAxiom(PropertyExpression role);
	
	public DisjointClassesAxiom createDisjointClassesAxiom(Set<SubClassExpression> classes);
	
	public DisjointPropertiesAxiom createDisjointPropertiesAxiom(Set<PropertyExpression> props);
	
	public PropertyAssertion createPropertyAssertion(PropertyExpression attribute, ObjectConstant o1, Constant o2);
	
	public ClassAssertion createClassAssertion(OClass concept, ObjectConstant object);

	PropertyExpression createProperty(Predicate p, boolean inverse);
}
