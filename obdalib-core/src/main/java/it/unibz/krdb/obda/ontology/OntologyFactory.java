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

import it.unibz.krdb.obda.model.Constant;
import it.unibz.krdb.obda.model.ObjectConstant;
import it.unibz.krdb.obda.model.Predicate;
import it.unibz.krdb.obda.model.ValueConstant;

public interface OntologyFactory {

	public OClass createClass(Predicate p);
	
	public OClass createClass(String uri);

	public Property createProperty(Predicate p, boolean inverse);

	public Property createObjectProperty(String uri, boolean inverse);
	
	public Property createDataProperty(String uri);
	
	public DataType createDataType(Predicate p);
	
	public Ontology createOntology(String uri);
	
	public Ontology createOntology();

	public SubPropertyOfAxiom createSubPropertyAxiom(Property included, Property including);

	public SubClassOfAxiom createSubClassAxiom(BasicClassDescription concept1, BasicClassDescription concept2);

	@Deprecated
	public PropertySomeRestriction createPropertySomeRestriction(Predicate p, boolean isInverse);

	public PropertySomeRestriction createPropertySomeRestriction(Property role);
	
	public FunctionalPropertyAxiom createPropertyFunctionalAxiom(Property role);
	
	public DisjointClassesAxiom createDisjointClassAxiom(BasicClassDescription c1, BasicClassDescription c2);
	
	public DisjointPropertiesAxiom createDisjointPropertiesAxiom(Property p1, Property p2);
	
	public ObjectPropertyAssertion createObjectPropertyAssertion(Predicate role, ObjectConstant o1, ObjectConstant o2);

	public DataPropertyAssertion createDataPropertyAssertion(Predicate attribute, ObjectConstant o1, ValueConstant o2);
	
	public Assertion createPropertyAssertion(Predicate attribute, ObjectConstant o1, Constant o2);
	
	public ClassAssertion createClassAssertion(Predicate concept, ObjectConstant object);
}
