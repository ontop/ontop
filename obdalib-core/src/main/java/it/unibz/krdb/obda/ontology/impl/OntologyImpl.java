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

import it.unibz.krdb.obda.ontology.BasicClassDescription;
import it.unibz.krdb.obda.ontology.ClassAssertion;
import it.unibz.krdb.obda.ontology.DataPropertyExpression;
import it.unibz.krdb.obda.ontology.Datatype;
import it.unibz.krdb.obda.ontology.DisjointClassesAxiom;
import it.unibz.krdb.obda.ontology.DisjointPropertiesAxiom;
import it.unibz.krdb.obda.ontology.OClass;
import it.unibz.krdb.obda.ontology.ObjectPropertyExpression;
import it.unibz.krdb.obda.ontology.Ontology;
import it.unibz.krdb.obda.ontology.FunctionalPropertyAxiom;
import it.unibz.krdb.obda.ontology.OntologyVocabulary;
import it.unibz.krdb.obda.ontology.PropertyExpression;
import it.unibz.krdb.obda.ontology.PropertyAssertion;
import it.unibz.krdb.obda.ontology.SomeValuesFrom;
import it.unibz.krdb.obda.ontology.SubClassExpression;
import it.unibz.krdb.obda.ontology.SubClassOfAxiom;
import it.unibz.krdb.obda.ontology.SubPropertyOfAxiom;

import java.util.LinkedHashSet;
import java.util.Set;

public class OntologyImpl implements Ontology {

	private static final long serialVersionUID = 758424053258299151L;

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

	
	private void addReferencedEntries(BasicClassDescription desc) {
		if (desc instanceof OClass) 
			vocabulary.declareClass((OClass) desc);
		else if (desc instanceof SomeValuesFrom) 
			addReferencedEntries(((SomeValuesFrom) desc).getProperty());
		else if (desc instanceof Datatype)  {
			// NO-OP
			// datatypes.add((Datatype) desc);
		}
		else 
			throw new UnsupportedOperationException("Cant understand: " + desc.toString());
	}
	
	private void addReferencedEntries(PropertyExpression prop) {
		if (prop instanceof ObjectPropertyExpression) 
			vocabulary.declareObjectProperty((ObjectPropertyExpression)prop);
		else
			vocabulary.declareDataProperty((DataPropertyExpression)prop);
	}
	
	@Override
	public void addAxiom(SubClassOfAxiom assertion) {
		addReferencedEntries(assertion.getSub());
		addReferencedEntries(assertion.getSuper());
		subClassAxioms.add(assertion);
	}

	@Override
	public void addAxiom(SubPropertyOfAxiom assertion) {
		addReferencedEntries(assertion.getSub());
		addReferencedEntries(assertion.getSuper());
		subPropertyAxioms.add(assertion);
	}

	@Override
	public void addAxiom(DisjointClassesAxiom assertion) {
		Set<SubClassExpression> classes = assertion.getClasses();
		for (SubClassExpression c : classes)
			addReferencedEntries(c);
		disjointClassesAxioms.add(assertion);
	}

	@Override
	public void addAxiom(DisjointPropertiesAxiom assertion) {
		Set<PropertyExpression> props = assertion.getProperties();
		for (PropertyExpression p : props)
			addReferencedEntries(p);
		disjointPropertiesAxioms.add(assertion);
	}

	@Override
	public void addAxiom(FunctionalPropertyAxiom assertion) {
		addReferencedEntries(assertion.getProperty());
		functionalityAxioms.add(assertion);
	}

	@Override
	public void addAxiom(ClassAssertion assertion) {
		addReferencedEntries(assertion.getConcept());
		classAssertions.add(assertion);
	}

	@Override
	public void addAxiom(PropertyAssertion assertion) {
		addReferencedEntries(assertion.getProperty());
		propertyAssertions.add(assertion);
	}
	
	
	@Override
	public void addAssertionWithCheck(SubClassOfAxiom assertion) {		
		vocabulary.checkSignature(assertion.getSub());
		vocabulary.checkSignature(assertion.getSuper());
		subClassAxioms.add(assertion);
	}

	@Override
	public void addAssertionWithCheck(SubPropertyOfAxiom assertion) {
		vocabulary.checkSignature(assertion.getSub());
		vocabulary.checkSignature(assertion.getSuper());
		subPropertyAxioms.add(assertion);
	}

	@Override
	public void addAssertionWithCheck(DisjointClassesAxiom assertion) {	
		Set<SubClassExpression> classes = assertion.getClasses();
		for (SubClassExpression c : classes)
			vocabulary.checkSignature(c);
		disjointClassesAxioms.add(assertion);
	}

	@Override
	public void addAssertionWithCheck(DisjointPropertiesAxiom assertion) {
		Set<PropertyExpression> props = assertion.getProperties();
		for (PropertyExpression p : props)
			vocabulary.checkSignature(p);
		disjointPropertiesAxioms.add(assertion);
	}
	
	@Override
	public void addAssertionWithCheck(FunctionalPropertyAxiom assertion) {
		vocabulary.checkSignature(assertion.getProperty());
		functionalityAxioms.add(assertion);
	}
	
	@Override
	public void addAssertionWithCheck(ClassAssertion assertion) {
		vocabulary.checkSignature(assertion.getConcept());
		classAssertions.add(assertion);
	}

	@Override
	public void addAssertionWithCheck(PropertyAssertion assertion) {
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
	public void merge(Ontology onto) {
		vocabulary.merge(onto.getVocabulary());
		
		subClassAxioms.addAll(onto.getSubClassAxioms());
		subPropertyAxioms.addAll(onto.getSubPropertyAxioms());
		disjointPropertiesAxioms.addAll(onto.getDisjointPropertiesAxioms());
		disjointClassesAxioms.addAll(onto.getDisjointClassesAxioms());
		functionalityAxioms.addAll(onto.getFunctionalPropertyAxioms());
		classAssertions.addAll(onto.getClassAssertions());
		propertyAssertions.addAll(onto.getPropertyAssertions());
	}

	@Override
	public OntologyVocabulary getVocabulary() {
		return vocabulary;
	}
}
