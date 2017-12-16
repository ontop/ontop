package it.unibz.inf.ontop.spec.ontology;

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

import java.util.Collection;
import java.util.List;
import java.util.Set;

public interface Ontology  {

	OntologyVocabularyCategory<OClass> classes();

    OntologyVocabularyCategory<ObjectPropertyExpression> objectProperties();

    OntologyVocabularyCategory<DataPropertyExpression> dataProperties();

    OntologyVocabularyCategory<AnnotationProperty> annotationProperties();

	Datatype getDatatype(String uri);


	// SUBCLASS/PROPERTY

	void addSubClassOfAxiom(ClassExpression concept1, ClassExpression concept2) throws InconsistentOntologyException;

	void addDataPropertyRangeAxiom(DataPropertyRangeExpression range, Datatype datatype) throws InconsistentOntologyException;
	
	void addSubPropertyOfAxiom(ObjectPropertyExpression included, ObjectPropertyExpression including) throws InconsistentOntologyException;

	void addSubPropertyOfAxiom(DataPropertyExpression included, DataPropertyExpression including) throws InconsistentOntologyException;

	void addSubPropertyOfAxiom(AnnotationProperty included, AnnotationProperty including);

	Collection<BinaryAxiom<ClassExpression>> getSubClassAxioms();

	Collection<BinaryAxiom<DataRangeExpression>> getSubDataRangeAxioms();
	
	Collection<BinaryAxiom<ObjectPropertyExpression>> getSubObjectPropertyAxioms();

	Collection<BinaryAxiom<DataPropertyExpression>> getSubDataPropertyAxioms();


	// DISJOINTNESS
	
	void addDisjointClassesAxiom(ClassExpression... classes) throws InconsistentOntologyException;

	void addDisjointObjectPropertiesAxiom(ObjectPropertyExpression... properties) throws InconsistentOntologyException;
	
	void addDisjointDataPropertiesAxiom(DataPropertyExpression... properties) throws InconsistentOntologyException;
	
	
	Collection<NaryAxiom<ClassExpression>> getDisjointClassesAxioms();
	
	Collection<NaryAxiom<ObjectPropertyExpression>> getDisjointObjectPropertiesAxioms();

	Collection<NaryAxiom<DataPropertyExpression>> getDisjointDataPropertiesAxioms();
	
	
	// REFLEXIVITY / IRREFLEXIVITY
	
	void addReflexiveObjectPropertyAxiom(ObjectPropertyExpression ope) throws InconsistentOntologyException;

	void addIrreflexiveObjectPropertyAxiom(ObjectPropertyExpression ope) throws InconsistentOntologyException;
	
	Collection<ObjectPropertyExpression> getReflexiveObjectPropertyAxioms();
	
	Collection<ObjectPropertyExpression> getIrreflexiveObjectPropertyAxioms();
	
	// FUNCTIONALITY 
	
	
	void addFunctionalObjectPropertyAxiom(ObjectPropertyExpression prop);

	void addFunctionalDataPropertyAxiom(DataPropertyExpression prop);
	
	Set<ObjectPropertyExpression> getFunctionalObjectProperties();

	Set<DataPropertyExpression> getFunctionalDataProperties();
	
	
	// ASSERTIONS
	
	void addClassAssertion(ClassAssertion assertion);

	void addObjectPropertyAssertion(ObjectPropertyAssertion assertion);

	void addDataPropertyAssertion(DataPropertyAssertion assertion);

	void addAnnotationAssertion(AnnotationAssertion assertion);

	
	List<ClassAssertion> getClassAssertions();

	List<ObjectPropertyAssertion> getObjectPropertyAssertions();
	
	List<DataPropertyAssertion> getDataPropertyAssertions();

	List<AnnotationAssertion> getAnnotationAssertions();





	/**
	 * create an auxiliary object property 
	 * (auxiliary properties result from ontology normalization)
	 * 
	 *
	 */

	ObjectPropertyExpression createAuxiliaryObjectProperty();
	
	
	/**
	 * return all auxiliary object properties
	 * (auxiliary properties result from ontology normalization)
	 * 
	 * @return
	 */
	
	Collection<ObjectPropertyExpression> getAuxiliaryObjectProperties();

}
