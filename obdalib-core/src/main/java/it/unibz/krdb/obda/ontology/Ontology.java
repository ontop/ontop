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

import java.io.Serializable;
import java.util.Collection;
import java.util.List;
import java.util.Set;

import com.google.common.collect.ImmutableList;

public interface Ontology extends Serializable {

	/**
	 * vocabulary is used to check whether all the symbols referenced in axioms are valid
	 * @return ontology vocabulary 
	 */
	
	public ImmutableOntologyVocabulary getVocabulary();
	
	// SUBCLASS/PROPERTY

	public void addSubClassOfAxiom(ClassExpression concept1, ClassExpression concept2) throws InconsistentOntologyException;

	public void addDataPropertyRangeAxiom(DataPropertyRangeExpression range, Datatype datatype) throws InconsistentOntologyException;
	
	public void addSubPropertyOfAxiom(ObjectPropertyExpression included, ObjectPropertyExpression including) throws InconsistentOntologyException;

	public void addSubPropertyOfAxiom(DataPropertyExpression included, DataPropertyExpression including) throws InconsistentOntologyException;


	public Collection<BinaryAxiom<ClassExpression>> getSubClassAxioms();

	public Collection<BinaryAxiom<DataRangeExpression>> getSubDataRangeAxioms();
	
	public Collection<BinaryAxiom<ObjectPropertyExpression>> getSubObjectPropertyAxioms();

	public Collection<BinaryAxiom<DataPropertyExpression>> getSubDataPropertyAxioms();


	// DISJOINTNESS
	
	public void addDisjointClassesAxiom(ClassExpression... classes) throws InconsistentOntologyException;

	public void addDisjointObjectPropertiesAxiom(ObjectPropertyExpression... properties) throws InconsistentOntologyException;
	
	public void addDisjointDataPropertiesAxiom(DataPropertyExpression... properties) throws InconsistentOntologyException;
	
	
	public Collection<NaryAxiom<ClassExpression>> getDisjointClassesAxioms();
	
	public Collection<NaryAxiom<ObjectPropertyExpression>> getDisjointObjectPropertiesAxioms();

	public Collection<NaryAxiom<DataPropertyExpression>> getDisjointDataPropertiesAxioms();
	
	
	// REFLEXIVITY / IRREFLEXIVITY
	
	public void addReflexiveObjectPropertyAxiom(ObjectPropertyExpression ope);

	public void addIrreflexiveObjectPropertyAxiom(ObjectPropertyExpression ope);
	
	// FUNCTIONALITY 
	
	
	public void addFunctionalObjectPropertyAxiom(ObjectPropertyExpression prop);

	public void addFunctionalDataPropertyAxiom(DataPropertyExpression prop);
	
	public Set<ObjectPropertyExpression> getFunctionalObjectProperties();

	public Set<DataPropertyExpression> getFunctionalDataProperties();
	
	
	// ASSERTIONS
	
	public void addClassAssertion(ClassAssertion assertion);

	public void addObjectPropertyAssertion(ObjectPropertyAssertion assertion);

	public void addDataPropertyAssertion(DataPropertyAssertion assertion);

	
	public List<ClassAssertion> getClassAssertions();

	public List<ObjectPropertyAssertion> getObjectPropertyAssertions();
	
	public List<DataPropertyAssertion> getDataPropertyAssertions();
	
	

	
	
	/**
	 * create an auxiliary object property 
	 * (auxiliary properties result from ontology normalization)
	 * 
	 * @param uri
	 */

	public ObjectPropertyExpression createAuxiliaryObjectProperty();
	
	/**
	 * create an auxiliary data property 
	 * (auxiliary properties result from ontology normalization)
	 * 
	 * @param uri
	 */
	
	public DataPropertyExpression createAuxiliaryDataProperty();
	
	
	/**
	 * return all auxiliary object properties
	 * (auxiliary properties result from ontology normalization)
	 * 
	 * @return
	 */
	
	public Collection<ObjectPropertyExpression> getAuxiliaryObjectProperties();

	/**
	 * return all auxiliary data properties
	 * (auxiliary properties result from ontology normalization)
	 * 
	 * @return
	 */
	
	public Collection<DataPropertyExpression> getAuxiliaryDataProperties();


}
