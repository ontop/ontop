package org.semanticweb.ontop.ontology;

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
import java.util.List;
import java.util.Set;

import org.semanticweb.ontop.model.Predicate;

public interface Ontology extends Cloneable, Serializable {

	@Deprecated
	public void addSubClassOfAxiomWithReferencedEntities(DataRangeExpression concept1, DataRangeExpression concept2);
	
	@Deprecated
	public void addSubClassOfAxiomWithReferencedEntities(ClassExpression concept1, ClassExpression concept2);

	@Deprecated
	public void addSubPropertyOfAxiomWithReferencedEntities(ObjectPropertyExpression included, ObjectPropertyExpression including);

	@Deprecated
	public void addSubPropertyOfAxiomWithReferencedEntities(DataPropertyExpression included, DataPropertyExpression including);

	
	
	
	public void addSubClassOfAxiom(ClassExpression concept1, ClassExpression concept2);

	public void addSubClassOfAxiom(DataRangeExpression concept1, DataRangeExpression concept2);
	
	public void addSubPropertyOfAxiom(ObjectPropertyExpression included, ObjectPropertyExpression including);

	public void addSubPropertyOfAxiom(DataPropertyExpression included, DataPropertyExpression including);

	public void addDisjointClassesAxiom(Set<ClassExpression> classes);

	public void addDisjointObjectPropertiesAxiom(Set<ObjectPropertyExpression> properties);
	
	public void addDisjointDataPropertiesAxiom(Set<DataPropertyExpression> properties);

	public void addFunctionalObjectPropertyAxiom(ObjectPropertyExpression prop);

	public void addFunctionalDataPropertyAxiom(DataPropertyExpression prop);
	
	public void addClassAssertion(ClassAssertion assertion);

	public void addObjectPropertyAssertion(ObjectPropertyAssertion assertion);

	public void addDataPropertyAssertion(DataPropertyAssertion assertion);

	
	
	public Ontology clone();

	
	public OntologyVocabulary getVocabulary();
	
	
	public List<BinaryAxiom<ClassExpression>> getSubClassAxioms();

	public List<BinaryAxiom<DataRangeExpression>> getSubDataRangeAxioms();
	
	public List<BinaryAxiom<ObjectPropertyExpression>> getSubObjectPropertyAxioms();

	public List<BinaryAxiom<DataPropertyExpression>> getSubDataPropertyAxioms();
	
	public List<NaryAxiom<ClassExpression>> getDisjointClassesAxioms();
	
	public List<NaryAxiom<ObjectPropertyExpression>> getDisjointObjectPropertiesAxioms();

	public List<NaryAxiom<DataPropertyExpression>> getDisjointDataPropertiesAxioms();

	public Set<ObjectPropertyExpression> getFunctionalObjectProperties();

	public Set<DataPropertyExpression> getFunctionalDataProperties();
	
	public List<ClassAssertion> getClassAssertions();

	public List<ObjectPropertyAssertion> getObjectPropertyAssertions();
	
	public List<DataPropertyAssertion> getDataPropertyAssertions();

}
