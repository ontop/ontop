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
import it.unibz.krdb.obda.ontology.NaryAxiom;
import it.unibz.krdb.obda.ontology.ObjectPropertyAssertion;
import it.unibz.krdb.obda.ontology.ObjectPropertyExpression;
import it.unibz.krdb.obda.ontology.Ontology;
import it.unibz.krdb.obda.ontology.OntologyFactory;
import it.unibz.krdb.obda.ontology.OntologyVocabulary;
import it.unibz.krdb.obda.ontology.ClassExpression;
import it.unibz.krdb.obda.ontology.BinaryAxiom;

import java.util.ArrayList;
import java.util.Collections;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Set;

public class OntologyImpl implements Ontology {

	private static final long serialVersionUID = 758424053258299151L;
	
	private final OntologyVocabularyImpl vocabulary = new OntologyVocabularyImpl();
	
	private static final OntologyFactory ofac = OntologyFactoryImpl.getInstance();
	
	// axioms and assertions

	private final List<BinaryAxiom<ClassExpression>> subClassAxioms = new ArrayList<BinaryAxiom<ClassExpression>>();
	
	private final List<BinaryAxiom<DataRangeExpression>> subDataRangeAxioms = new ArrayList<BinaryAxiom<DataRangeExpression>>();
	
	private final List<BinaryAxiom<ObjectPropertyExpression>> subObjectPropertyAxioms = new ArrayList<BinaryAxiom<ObjectPropertyExpression>>();
	
	private final List<BinaryAxiom<DataPropertyExpression>> subDataPropertyAxioms = new ArrayList<BinaryAxiom<DataPropertyExpression>>();

	private final List<NaryAxiom<ClassExpression>> disjointClassesAxioms = new ArrayList<NaryAxiom<ClassExpression>>();

	private final List<NaryAxiom<ObjectPropertyExpression>> disjointObjectPropertiesAxioms = new ArrayList<NaryAxiom<ObjectPropertyExpression>>();

	private final List<NaryAxiom<DataPropertyExpression>> disjointDataPropertiesAxioms = new ArrayList<NaryAxiom<DataPropertyExpression>>();
	
	private final Set<ObjectPropertyExpression> functionalObjectPropertyAxioms = new LinkedHashSet<ObjectPropertyExpression>();

	private final Set<DataPropertyExpression> functionalDataPropertyAxioms = new LinkedHashSet<DataPropertyExpression>();
	
	private final List<ClassAssertion> classAssertions = new ArrayList<ClassAssertion>();

	private final List<ObjectPropertyAssertion> objectPropertyAssertions = new ArrayList<ObjectPropertyAssertion>();
	
	private final List<DataPropertyAssertion> dataPropertyAssertions = new ArrayList<DataPropertyAssertion>();
	

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
		vocabulary.addReferencedEntries(concept1);
		vocabulary.addReferencedEntries(concept2);
		if (!concept1.equals(ofac.getNothing()) && !concept2.equals(ofac.getThing())) {
			BinaryAxiom<ClassExpression> assertion = new BinaryAxiomImpl<ClassExpression>(concept1, concept2);
			subClassAxioms.add(assertion);
		}
	}

	@Override
	public void addSubClassOfAxiomWithReferencedEntities(DataRangeExpression concept1, DataRangeExpression concept2) {
		vocabulary.addReferencedEntries(concept1);
		vocabulary.addReferencedEntries(concept2);
		BinaryAxiom<DataRangeExpression> assertion = new BinaryAxiomImpl<DataRangeExpression>(concept1, concept2);
		subDataRangeAxioms.add(assertion);
	}
	
	@Override
	public void addSubPropertyOfAxiomWithReferencedEntities(ObjectPropertyExpression included, ObjectPropertyExpression including) {
		vocabulary.addReferencedEntries(included);
		vocabulary.addReferencedEntries(including);
		if (!included.isBottom() && !including.isTop()) {
			BinaryAxiom<ObjectPropertyExpression> assertion = new BinaryAxiomImpl<ObjectPropertyExpression>(included, including);
			subObjectPropertyAxioms.add(assertion);
		}
	}
	
	@Override
	public void addSubPropertyOfAxiomWithReferencedEntities(DataPropertyExpression included, DataPropertyExpression including) {
		vocabulary.addReferencedEntries(included);
		vocabulary.addReferencedEntries(including);
		if (!included.isBottom() && !including.isTop()) {
			BinaryAxiom<DataPropertyExpression> assertion = new BinaryAxiomImpl<DataPropertyExpression>(included, including);
			subDataPropertyAxioms.add(assertion);
		}
	}

	@Override
	public void addSubClassOfAxiom(ClassExpression concept1, ClassExpression concept2) {
		vocabulary.checkSignature(concept1);
		vocabulary.checkSignature(concept2);
		if (!concept1.equals(ofac.getNothing()) && !concept2.equals(ofac.getThing())) {
			BinaryAxiom<ClassExpression> ax = new BinaryAxiomImpl<ClassExpression>(concept1, concept2);
			subClassAxioms.add(ax);
		}
	}	

	@Override
	public void addSubClassOfAxiom(DataRangeExpression concept1, DataRangeExpression concept2) {
		vocabulary.checkSignature(concept1);
		vocabulary.checkSignature(concept2);
		BinaryAxiom<DataRangeExpression> ax = new BinaryAxiomImpl<DataRangeExpression>(concept1, concept2);
		subDataRangeAxioms.add(ax);
	}

	@Override
	public void addSubPropertyOfAxiom(ObjectPropertyExpression included, ObjectPropertyExpression including) {
		vocabulary.checkSignature(included);
		vocabulary.checkSignature(including);
		if (!included.isBottom() && !including.isTop()) {
			BinaryAxiom<ObjectPropertyExpression> ax = new BinaryAxiomImpl<ObjectPropertyExpression>(included, including);
			subObjectPropertyAxioms.add(ax);
		}
	}
	
	@Override
	public void addSubPropertyOfAxiom(DataPropertyExpression included, DataPropertyExpression including) {
		vocabulary.checkSignature(included);
		vocabulary.checkSignature(including);
		if (!included.isBottom() && !including.isTop()) {
			BinaryAxiom<DataPropertyExpression> ax = new BinaryAxiomImpl<DataPropertyExpression>(included, including);
			subDataPropertyAxioms.add(ax);
		}
	}

	@Override
	public void addDisjointClassesAxiom(Set<ClassExpression> classes) {	
		for (ClassExpression c : classes)
			vocabulary.checkSignature(c);
		NaryAxiom<ClassExpression> ax = new NaryAxiomImpl<ClassExpression>(classes);
		disjointClassesAxioms.add(ax);
	}

	@Override
	public void addDisjointObjectPropertiesAxiom(Set<ObjectPropertyExpression> props) {
		for (ObjectPropertyExpression p : props)
			vocabulary.checkSignature(p);
		NaryAxiomImpl<ObjectPropertyExpression> ax = new NaryAxiomImpl<ObjectPropertyExpression>(props);
		disjointObjectPropertiesAxioms.add(ax);
	}

	@Override
	public void addDisjointDataPropertiesAxiom(Set<DataPropertyExpression> props) {
		for (DataPropertyExpression p : props)
			vocabulary.checkSignature(p);
		NaryAxiomImpl<DataPropertyExpression> ax = new NaryAxiomImpl<DataPropertyExpression>(props);
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
	public List<ClassAssertion> getClassAssertions() {
		return Collections.unmodifiableList(classAssertions);
	}
	
	@Override 
	public List<ObjectPropertyAssertion> getObjectPropertyAssertions() {
		return Collections.unmodifiableList(objectPropertyAssertions);
	}

	@Override 
	public List<DataPropertyAssertion> getDataPropertyAssertions() {
		return Collections.unmodifiableList(dataPropertyAssertions);
	}

	@Override
	public List<BinaryAxiom<ClassExpression>> getSubClassAxioms() {
		return Collections.unmodifiableList(subClassAxioms);
	}
	
	@Override
	public List<BinaryAxiom<DataRangeExpression>> getSubDataRangeAxioms() {
		return Collections.unmodifiableList(subDataRangeAxioms);
	}
	
	
	@Override
	public List<BinaryAxiom<ObjectPropertyExpression>> getSubObjectPropertyAxioms() {
		return Collections.unmodifiableList(subObjectPropertyAxioms);
	}
	
	@Override
	public List<BinaryAxiom<DataPropertyExpression>> getSubDataPropertyAxioms() {
		return Collections.unmodifiableList(subDataPropertyAxioms);
	}
	
	@Override 
	public Set<ObjectPropertyExpression> getFunctionalObjectProperties() {
		return Collections.unmodifiableSet(functionalObjectPropertyAxioms);
	}
	
	@Override 
	public Set<DataPropertyExpression> getFunctionalDataProperties() {
		return Collections.unmodifiableSet(functionalDataPropertyAxioms);
	}
	
	@Override 
	public List<NaryAxiom<ClassExpression>> getDisjointClassesAxioms() {
		return Collections.unmodifiableList(disjointClassesAxioms);
	}
	
	@Override 
	public List<NaryAxiom<ObjectPropertyExpression>> getDisjointObjectPropertiesAxioms() {
		return Collections.unmodifiableList(disjointObjectPropertiesAxioms);
	}

	@Override 
	public List<NaryAxiom<DataPropertyExpression>> getDisjointDataPropertiesAxioms() {
		return Collections.unmodifiableList(disjointDataPropertiesAxioms);
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
