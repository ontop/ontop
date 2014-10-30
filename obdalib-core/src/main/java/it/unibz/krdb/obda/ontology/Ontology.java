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
import java.util.Set;

public interface Ontology extends Cloneable, Serializable {

	public void addSubClassOfAxiomWithRefencedEntities(SubClassExpression concept1, BasicClassDescription concept2);

	public void addSubPropertyOfAxiomWithRefencedEntities(ObjectPropertyExpression included, ObjectPropertyExpression including);

	public void addSubPropertyOfAxiomWithRefencedEntities(DataPropertyExpression included, DataPropertyExpression including);

	
	
	
	public void add(SubClassOfAxiom assertion);

	public void add(SubPropertyOfAxiom assertion);

	public void add(DisjointClassesAxiom assertion);

	public void add(DisjointPropertiesAxiom assertion);

	public void add(FunctionalPropertyAxiom assertion);

	public void add(ClassAssertion assertion);

	public void add(PropertyAssertion assertion);


	
	
	public void merge(Ontology onto);
	
	public Ontology clone();

	
	public OntologyVocabulary getVocabulary();
	
	
	public Set<SubClassOfAxiom> getSubClassAxioms();

	public Set<SubPropertyOfAxiom> getSubPropertyAxioms();
	
	public Set<DisjointClassesAxiom> getDisjointClassesAxioms();
	
	public Set<DisjointPropertiesAxiom> getDisjointPropertiesAxioms();
	
	public Set<FunctionalPropertyAxiom> getFunctionalPropertyAxioms();
	
	public Set<ClassAssertion> getClassAssertions();

	public Set<PropertyAssertion> getPropertyAssertions();
}
