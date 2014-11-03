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

import it.unibz.krdb.obda.ontology.ClassAssertion;
import it.unibz.krdb.obda.ontology.DataPropertyAssertion;
import it.unibz.krdb.obda.ontology.DataPropertyExpression;
import it.unibz.krdb.obda.ontology.DataRangeExpression;
import it.unibz.krdb.obda.ontology.DisjointnessAxiom;
import it.unibz.krdb.obda.ontology.ObjectPropertyAssertion;
import it.unibz.krdb.obda.ontology.ObjectPropertyExpression;
import it.unibz.krdb.obda.ontology.Ontology;
import it.unibz.krdb.obda.ontology.FunctionalPropertyAxiom;
import it.unibz.krdb.obda.ontology.OntologyFactory;
import it.unibz.krdb.obda.ontology.OntologyVocabulary;

import it.unibz.krdb.obda.ontology.ClassExpression;
import it.unibz.krdb.obda.ontology.SubClassOfAxiom;
import it.unibz.krdb.obda.ontology.SubPropertyOfAxiom;

import java.util.LinkedHashSet;
import java.util.Set;

public class OntologyImpl implements Ontology {

	private static final long serialVersionUID = 758424053258299151L;
	
	private final OntologyVocabularyImpl vocabulary = new OntologyVocabularyImpl();
	
	// axioms and assertions

	private final Set<SubClassOfAxiom> subClassAxioms = new LinkedHashSet<SubClassOfAxiom>();
	
	private final Set<SubPropertyOfAxiom<ObjectPropertyExpression>> subObjectPropertyAxioms = new LinkedHashSet<SubPropertyOfAxiom<ObjectPropertyExpression>>();
	
	private final Set<SubPropertyOfAxiom<DataPropertyExpression>> subDataPropertyAxioms = new LinkedHashSet<SubPropertyOfAxiom<DataPropertyExpression>>();

	private final Set<DisjointnessAxiom<ClassExpression>> disjointClassesAxioms = new LinkedHashSet<DisjointnessAxiom<ClassExpression>>();

	private final Set<DisjointnessAxiom<ObjectPropertyExpression>> disjointObjectPropertiesAxioms = new LinkedHashSet<DisjointnessAxiom<ObjectPropertyExpression>>();

	private final Set<DisjointnessAxiom<DataPropertyExpression>> disjointDataPropertiesAxioms = new LinkedHashSet<DisjointnessAxiom<DataPropertyExpression>>();
	
	private final Set<ObjectPropertyExpression> functionalObjectPropertyAxioms = new LinkedHashSet<ObjectPropertyExpression>();

	private final Set<DataPropertyExpression> functionalDataPropertyAxioms = new LinkedHashSet<DataPropertyExpression>();
	
	private final Set<ClassAssertion> classAssertions = new LinkedHashSet<ClassAssertion>();

	private final Set<ObjectPropertyAssertion> objectPropertyAssertions = new LinkedHashSet<ObjectPropertyAssertion>();
	
	private final Set<DataPropertyAssertion> dataPropertyAssertions = new LinkedHashSet<DataPropertyAssertion>();
	

	OntologyImpl() {
	}

	@Override
	public OntologyImpl clone() {
		OntologyImpl clone = new OntologyImpl();
		clone.subClassAxioms.addAll(subClassAxioms);
		clone.subObjectPropertyAxioms.addAll(subObjectPropertyAxioms);
		clone.subDataPropertyAxioms.addAll(subDataPropertyAxioms);
		clone.vocabulary.merge(vocabulary);
		return clone;
	}

	
	
	@Override
	public void addSubClassOfAxiomWithReferencedEntities(ClassExpression concept1, ClassExpression concept2) {	
		SubClassOfAxiom assertion = new SubClassOfAxiomImpl(concept1, concept2);
		vocabulary.addReferencedEntries(assertion.getSub());
		vocabulary.addReferencedEntries(assertion.getSuper());
		subClassAxioms.add(assertion);
	}

	@Override
	public void addSubClassOfAxiomWithReferencedEntities(DataRangeExpression concept1, DataRangeExpression concept2) {
		SubClassOfAxiom assertion = new SubClassOfAxiomImpl(concept1, concept2);
		vocabulary.addReferencedEntries(assertion.getSub());
		vocabulary.addReferencedEntries(assertion.getSuper());
		subClassAxioms.add(assertion);
	}
	
	@Override
	public void addSubPropertyOfAxiomWithReferencedEntities(ObjectPropertyExpression included, ObjectPropertyExpression including) {
		SubPropertyOfAxiom<ObjectPropertyExpression> assertion = new SubPropertyOfAxiomImpl<ObjectPropertyExpression>(included, including);
		vocabulary.addReferencedEntries(assertion.getSub());
		vocabulary.addReferencedEntries(assertion.getSuper());
		subObjectPropertyAxioms.add(assertion);
	}
	
	@Override
	public void addSubPropertyOfAxiomWithReferencedEntities(DataPropertyExpression included, DataPropertyExpression including) {
		SubPropertyOfAxiom<DataPropertyExpression> assertion = new SubPropertyOfAxiomImpl<DataPropertyExpression>(included, including);
		vocabulary.addReferencedEntries(assertion.getSub());
		vocabulary.addReferencedEntries(assertion.getSuper());
		subDataPropertyAxioms.add(assertion);
	}

	@Override
	public void addSubClassOfAxiom(ClassExpression concept1, ClassExpression concept2) {
		vocabulary.checkSignature(concept1);
		vocabulary.checkSignature(concept2);
		SubClassOfAxiom ax = new SubClassOfAxiomImpl(concept1, concept2);
		subClassAxioms.add(ax);
	}	

	@Override
	public void addSubClassOfAxiom(DataRangeExpression concept1, DataRangeExpression concept2) {
		vocabulary.checkSignature(concept1);
		vocabulary.checkSignature(concept2);
		SubClassOfAxiom ax = new SubClassOfAxiomImpl(concept1, concept2);
		subClassAxioms.add(ax);
	}

	@Override
	public void addSubPropertyOfAxiom(ObjectPropertyExpression included, ObjectPropertyExpression including) {
		vocabulary.checkSignature(included);
		vocabulary.checkSignature(including);
		SubPropertyOfAxiom<ObjectPropertyExpression> ax = new SubPropertyOfAxiomImpl<ObjectPropertyExpression>(included, including);
		subObjectPropertyAxioms.add(ax);
	}
	
	@Override
	public void addSubPropertyOfAxiom(DataPropertyExpression included, DataPropertyExpression including) {
		vocabulary.checkSignature(included);
		vocabulary.checkSignature(including);
		SubPropertyOfAxiom<DataPropertyExpression> ax = new SubPropertyOfAxiomImpl<DataPropertyExpression>(included, including);
		subDataPropertyAxioms.add(ax);
	}

	@Override
	public void addDisjointClassesAxiom(Set<ClassExpression> classes) {	
		for (ClassExpression c : classes)
			vocabulary.checkSignature(c);
		DisjointnessAxiom<ClassExpression> ax = new DisjointnessAxiomImpl<ClassExpression>(classes);
		disjointClassesAxioms.add(ax);
	}

	@Override
	public void addDisjointObjectPropertiesAxiom(Set<ObjectPropertyExpression> props) {
		for (ObjectPropertyExpression p : props)
			vocabulary.checkSignature(p);
		DisjointnessAxiomImpl<ObjectPropertyExpression> ax = new DisjointnessAxiomImpl<ObjectPropertyExpression>(props);
		disjointObjectPropertiesAxioms.add(ax);
	}

	@Override
	public void addDisjointDataPropertiesAxiom(Set<DataPropertyExpression> props) {
		for (DataPropertyExpression p : props)
			vocabulary.checkSignature(p);
		DisjointnessAxiomImpl<DataPropertyExpression> ax = new DisjointnessAxiomImpl<DataPropertyExpression>(props);
		disjointDataPropertiesAxioms.add(ax);
	}
	
	@Override
	public void addFunctionalObjectPropertyAxiom(ObjectPropertyExpression prop) {
		vocabulary.checkSignature(prop);
		functionalObjectPropertyAxioms.add(prop);
	}

	@Override
	public void addFunctionalDataPropertyAxiom(DataPropertyExpression prop) {
		vocabulary.checkSignature(prop);
		functionalDataPropertyAxioms.add(prop);
	}
	
	@Override
	public void addClassAssertion(ClassAssertion assertion) {
		vocabulary.checkSignature(assertion.getConcept());
		classAssertions.add(assertion);
	}

	@Override
	public void addObjectPropertyAssertion(ObjectPropertyAssertion assertion) {
		vocabulary.checkSignature(assertion.getProperty());
		objectPropertyAssertions.add(assertion);
	}
	
	@Override
	public void addDataPropertyAssertion(DataPropertyAssertion assertion) {
		vocabulary.checkSignature(assertion.getProperty());
		dataPropertyAssertions.add(assertion);
	}
	
	
	@Override 
	public Set<ClassAssertion> getClassAssertions() {
		return classAssertions;
	}
	
	@Override 
	public Set<ObjectPropertyAssertion> getObjectPropertyAssertions() {
		return objectPropertyAssertions;
	}

	@Override 
	public Set<DataPropertyAssertion> getDataPropertyAssertions() {
		return dataPropertyAssertions;
	}

	@Override
	public Set<SubClassOfAxiom> getSubClassAxioms() {
		return subClassAxioms;
	}
	
	@Override
	public Set<SubPropertyOfAxiom<ObjectPropertyExpression>> getSubObjectPropertyAxioms() {
		return subObjectPropertyAxioms;
	}
	
	@Override
	public Set<SubPropertyOfAxiom<DataPropertyExpression>> getSubDataPropertyAxioms() {
		return subDataPropertyAxioms;
	}
	
	@Override 
	public Set<ObjectPropertyExpression> getFunctionalObjectProperties() {
		return functionalObjectPropertyAxioms;
	}
	
	@Override 
	public Set<DataPropertyExpression> getFunctionalDataProperties() {
		return functionalDataPropertyAxioms;
	}
	
	@Override 
	public Set<DisjointnessAxiom<ClassExpression>> getDisjointClassesAxioms() {
		return disjointClassesAxioms;
	}
	
	@Override 
	public Set<DisjointnessAxiom<ObjectPropertyExpression>> getDisjointObjectPropertiesAxioms() {
		return disjointObjectPropertiesAxioms;
	}

	@Override 
	public Set<DisjointnessAxiom<DataPropertyExpression>> getDisjointDataPropertiesAxioms() {
		return disjointDataPropertiesAxioms;
	}

	
	@Override
	public String toString() {
		StringBuilder str = new StringBuilder();
		str.append("[Ontology info.");
		str.append(String.format(" Axioms: %d", subClassAxioms.size() + subObjectPropertyAxioms.size() + subDataPropertyAxioms.size()));
		str.append(String.format(" Classes: %d", getVocabulary().getClasses().size()));
		str.append(String.format(" Object Properties: %d", getVocabulary().getObjectProperties().size()));
		str.append(String.format(" Data Properties: %d]", getVocabulary().getDataProperties().size()));
		return str.toString();
	}

	@Override
	public OntologyVocabulary getVocabulary() {
		return vocabulary;
	}
}
