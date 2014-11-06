package org.semanticweb.ontop.ontology.impl;

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


import org.semanticweb.ontop.ontology.*;

import java.util.LinkedHashSet;
import java.util.Set;

public class OntologyImpl implements Ontology {

	private static final long serialVersionUID = 758424053258299151L;
	
	private static final OntologyFactory ofac = OntologyFactoryImpl.getInstance();

	private final OntologyVocabularyImpl vocabulary = new OntologyVocabularyImpl();
	
	// axioms and assertions

	private final Set<SubClassOfAxiom> subClassAxioms = new LinkedHashSet<SubClassOfAxiom>();
	
	private final Set<SubPropertyOfAxiom> subPropertyAxioms = new LinkedHashSet<SubPropertyOfAxiom>();

	private final Set<DisjointClassesAxiom> disjointClassesAxioms = new LinkedHashSet<DisjointClassesAxiom>();

	private final Set<DisjointPropertiesAxiom> disjointPropertiesAxioms = new LinkedHashSet<DisjointPropertiesAxiom>();
	
	private final Set<FunctionalPropertyAxiom> functionalityAxioms = new LinkedHashSet<FunctionalPropertyAxiom>();
	
	private final Set<ClassAssertion> classAssertions = new LinkedHashSet<ClassAssertion>();

	private final Set<PropertyAssertion> propertyAssertions = new LinkedHashSet<PropertyAssertion>();
	
	

	OntologyImpl() {
	}

	@Override
	public OntologyImpl clone() {
		OntologyImpl clone = new OntologyImpl();
		clone.subClassAxioms.addAll(subClassAxioms);
		clone.subPropertyAxioms.addAll(subPropertyAxioms);
		clone.vocabulary.merge(vocabulary);
		return clone;
	}

	
	
	@Override
	public void addSubClassOfAxiomWithReferencedEntities(ClassExpression concept1, ClassExpression concept2) {	
		SubClassOfAxiom assertion = ofac.createSubClassAxiom(concept1, concept2);
		vocabulary.addReferencedEntries(assertion.getSub());
		vocabulary.addReferencedEntries(assertion.getSuper());
		subClassAxioms.add(assertion);
	}

	@Override
	public void addSubClassOfAxiomWithReferencedEntities(DataRangeExpression concept1, DataRangeExpression concept2) {
		SubClassOfAxiom assertion = ofac.createSubClassAxiom(concept1, concept2);
		vocabulary.addReferencedEntries(assertion.getSub());
		vocabulary.addReferencedEntries(assertion.getSuper());
		subClassAxioms.add(assertion);
	}
	
	@Override
	public void addSubPropertyOfAxiomWithReferencedEntities(ObjectPropertyExpression included, ObjectPropertyExpression including) {
		SubPropertyOfAxiom assertion = ofac.createSubPropertyAxiom(included, including);
		vocabulary.addReferencedEntries(assertion.getSub());
		vocabulary.addReferencedEntries(assertion.getSuper());
		subPropertyAxioms.add(assertion);
	}
	
	@Override
	public void addSubPropertyOfAxiomWithReferencedEntities(DataPropertyExpression included, DataPropertyExpression including) {
		SubPropertyOfAxiom assertion = ofac.createSubPropertyAxiom(included, including);
		vocabulary.addReferencedEntries(assertion.getSub());
		vocabulary.addReferencedEntries(assertion.getSuper());
		subPropertyAxioms.add(assertion);
	}

	
	@Override
	public void add(SubClassOfAxiom assertion) {		
		vocabulary.checkSignature(assertion.getSub());
		vocabulary.checkSignature(assertion.getSuper());
		subClassAxioms.add(assertion);
	}

	@Override
	public void add(SubPropertyOfAxiom assertion) {
		vocabulary.checkSignature(assertion.getSub());
		vocabulary.checkSignature(assertion.getSuper());
		subPropertyAxioms.add(assertion);
	}

	@Override
	public void add(DisjointClassesAxiom assertion) {	
		Set<ClassExpression> classes = assertion.getClasses();
		for (ClassExpression c : classes)
			vocabulary.checkSignature(c);
		disjointClassesAxioms.add(assertion);
	}

	@Override
	public void add(DisjointPropertiesAxiom assertion) {
		Set<PropertyExpression> props = assertion.getProperties();
		for (PropertyExpression p : props)
			vocabulary.checkSignature(p);
		disjointPropertiesAxioms.add(assertion);
	}
	
	@Override
	public void add(FunctionalPropertyAxiom assertion) {
		vocabulary.checkSignature(assertion.getProperty());
		functionalityAxioms.add(assertion);
	}
	
	@Override
	public void add(ClassAssertion assertion) {
		vocabulary.checkSignature(assertion.getConcept());
		classAssertions.add(assertion);
	}

	@Override
	public void add(PropertyAssertion assertion) {
		vocabulary.checkSignature(assertion.getProperty());
		propertyAssertions.add(assertion);
	}
	
	
	@Override 
	public Set<ClassAssertion> getClassAssertions() {
		return classAssertions;
	}
	
	@Override 
	public Set<PropertyAssertion> getPropertyAssertions() {
		return propertyAssertions;
	}

	@Override
	public Set<SubClassOfAxiom> getSubClassAxioms() {
		return subClassAxioms;
	}
	
	@Override
	public Set<SubPropertyOfAxiom> getSubPropertyAxioms() {
		return subPropertyAxioms;
	}
	
	@Override 
	public Set<FunctionalPropertyAxiom> getFunctionalPropertyAxioms() {
		return functionalityAxioms;
	}
	
	@Override 
	public Set<DisjointClassesAxiom> getDisjointClassesAxioms() {
		return disjointClassesAxioms;
	}
	
	@Override 
	public Set<DisjointPropertiesAxiom> getDisjointPropertiesAxioms() {
		return disjointPropertiesAxioms;
	}

	@Override
	public String toString() {
		StringBuilder str = new StringBuilder();
		str.append("[Ontology info.");
		str.append(String.format(" Axioms: %d", subClassAxioms.size() + subPropertyAxioms.size()));
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
