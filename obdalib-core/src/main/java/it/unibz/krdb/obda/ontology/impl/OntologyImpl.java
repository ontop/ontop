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

import it.unibz.krdb.obda.model.Predicate;
import it.unibz.krdb.obda.model.impl.OBDAVocabulary;
import it.unibz.krdb.obda.ontology.BasicClassDescription;
import it.unibz.krdb.obda.ontology.ClassAssertion;
import it.unibz.krdb.obda.ontology.Datatype;
import it.unibz.krdb.obda.ontology.DisjointClassesAxiom;
import it.unibz.krdb.obda.ontology.DisjointPropertiesAxiom;
import it.unibz.krdb.obda.ontology.OClass;
import it.unibz.krdb.obda.ontology.Ontology;
import it.unibz.krdb.obda.ontology.FunctionalPropertyAxiom;
import it.unibz.krdb.obda.ontology.PropertyExpression;
import it.unibz.krdb.obda.ontology.PropertyAssertion;
import it.unibz.krdb.obda.ontology.SomeValuesFrom;
import it.unibz.krdb.obda.ontology.SubClassExpression;
import it.unibz.krdb.obda.ontology.SubClassOfAxiom;
import it.unibz.krdb.obda.ontology.SubPropertyOfAxiom;

import java.util.HashSet;
import java.util.LinkedHashSet;
import java.util.Set;

public class OntologyImpl implements Ontology {

	private static final long serialVersionUID = 758424053258299151L;

	// signature
	
	private final Set<OClass> concepts = new HashSet<OClass>();

	private final Set<Datatype> datatypes = new HashSet<Datatype>();
	
	private final Set<PropertyExpression> roles = new HashSet<PropertyExpression>();
	
	// axioms and assertions

	private final Set<SubClassOfAxiom> subClassAxioms = new LinkedHashSet<SubClassOfAxiom>();
	
	private final Set<SubPropertyOfAxiom> subPropertyAxioms = new LinkedHashSet<SubPropertyOfAxiom>();

	private final Set<DisjointClassesAxiom> disjointClassesAxioms = new LinkedHashSet<DisjointClassesAxiom>();

	private final Set<DisjointPropertiesAxiom> disjointPropertiesAxioms = new LinkedHashSet<DisjointPropertiesAxiom>();
	
	private final Set<FunctionalPropertyAxiom> functionalityAxioms = new LinkedHashSet<FunctionalPropertyAxiom>();
	
	private final Set<ClassAssertion> classAssertions = new LinkedHashSet<ClassAssertion>();

	private final Set<PropertyAssertion> propertyAssertions = new LinkedHashSet<PropertyAssertion>();
	
	
	// auxiliary symbols and built-in datatypes 
	
	public static final String AUXROLEURI = "ER.A-AUXROLE";
		
	private final static Set<Predicate> builtinDatatypes = initializeReserved();

	private static Set<Predicate> initializeReserved() { // static block
		Set<Predicate> datatypes = new HashSet<Predicate>();
		datatypes.add(OBDAVocabulary.RDFS_LITERAL);
		datatypes.add(OBDAVocabulary.XSD_STRING);
		datatypes.add(OBDAVocabulary.XSD_INTEGER);
		datatypes.add(OBDAVocabulary.XSD_NEGATIVE_INTEGER);
		datatypes.add(OBDAVocabulary.XSD_NON_NEGATIVE_INTEGER);
		datatypes.add(OBDAVocabulary.XSD_POSITIVE_INTEGER);
		datatypes.add(OBDAVocabulary.XSD_NON_POSITIVE_INTEGER);
		datatypes.add(OBDAVocabulary.XSD_INT);
		datatypes.add(OBDAVocabulary.XSD_UNSIGNED_INT);
		datatypes.add(OBDAVocabulary.XSD_FLOAT);
		datatypes.add(OBDAVocabulary.XSD_LONG);
		datatypes.add(OBDAVocabulary.XSD_DECIMAL);
		datatypes.add(OBDAVocabulary.XSD_DOUBLE);
		datatypes.add(OBDAVocabulary.XSD_DATETIME);
		datatypes.add(OBDAVocabulary.XSD_BOOLEAN);
		return datatypes;
	}

	OntologyImpl() {
	}

	@Override
	public OntologyImpl clone() {
		OntologyImpl clone = new OntologyImpl();
		clone.subClassAxioms.addAll(subClassAxioms);
		clone.subPropertyAxioms.addAll(subPropertyAxioms);
		clone.concepts.addAll(concepts);
		clone.roles.addAll(roles);
		return clone;
	}

	
	private void addReferencedEntries(BasicClassDescription desc) {
		if (desc instanceof OClass) 
			addConcept((OClass) desc);
		else if (desc instanceof SomeValuesFrom) 
			addRole(((SomeValuesFrom) desc).getProperty());
		else if (desc instanceof Datatype) 
			datatypes.add((Datatype) desc);
		else 
			throw new UnsupportedOperationException("Cant understand: " + desc.toString());
	}
	
	private void addReferencedEntries(PropertyExpression prop) {
		if (prop.isInverse())
			addRole(prop.getInverse());
		else
			addRole(prop);
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
	
	
	private void checkSignature(BasicClassDescription desc) {
		
		if (desc instanceof OClass) {
			if (!concepts.contains((OClass) desc))
				throw new IllegalArgumentException("Class predicate is unknown: " + desc);
		}	
		else if (desc instanceof Datatype) {
			Predicate pred = ((Datatype) desc).getPredicate();
			if (!builtinDatatypes.contains(pred)) 
				throw new IllegalArgumentException("Datatype predicate is unknown: " + pred);
		}
		else if (desc instanceof SomeValuesFrom) {
			checkSignature(((SomeValuesFrom) desc).getProperty());
		}
		else 
			throw new UnsupportedOperationException("Cant understand: " + desc);
	}

	private void checkSignature(PropertyExpression prop) {
		// Make sure we never validate against auxiliary roles introduced by
		// the translation of the OWL ontology
		if (prop.getPredicate().toString().contains(AUXROLEURI)) 
			return;

		if (prop.isInverse()) {
			if (!roles.contains(prop.getInverse())) 
				throw new IllegalArgumentException("At least one of these predicates is unknown: " + prop.toString());
		}
		else {
			if (!roles.contains(prop)) 
				throw new IllegalArgumentException("At least one of these predicates is unknown: " + prop.toString());			
		}
	}
	
	
	@Override
	public void addAssertionWithCheck(SubClassOfAxiom assertion) {		
		checkSignature(assertion.getSub());
		checkSignature(assertion.getSuper());
		subClassAxioms.add(assertion);
	}

	@Override
	public void addAssertionWithCheck(SubPropertyOfAxiom assertion) {
		checkSignature(assertion.getSub());
		checkSignature(assertion.getSuper());
		subPropertyAxioms.add(assertion);
	}

	@Override
	public void addAssertionWithCheck(DisjointClassesAxiom assertion) {	
		Set<SubClassExpression> classes = assertion.getClasses();
		for (SubClassExpression c : classes)
			checkSignature(c);
		disjointClassesAxioms.add(assertion);
	}

	@Override
	public void addAssertionWithCheck(DisjointPropertiesAxiom assertion) {
		Set<PropertyExpression> props = assertion.getProperties();
		for (PropertyExpression p : props)
			checkSignature(p);
		disjointPropertiesAxioms.add(assertion);
	}
	
	@Override
	public void addAssertionWithCheck(FunctionalPropertyAxiom assertion) {
		checkSignature(assertion.getProperty());
		functionalityAxioms.add(assertion);
	}
	
	@Override
	public void addAssertionWithCheck(ClassAssertion assertion) {
		checkSignature(assertion.getConcept());
		classAssertions.add(assertion);
	}

	@Override
	public void addAssertionWithCheck(PropertyAssertion assertion) {
		checkSignature(assertion.getProperty());
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
		str.append(String.format(" Classes: %d", concepts.size()));
		str.append(String.format(" Properties: %d]", roles.size()));
		return str.toString();
	}

	@Override
	public void addConcept(OClass cd) {
		concepts.add(cd);
	}

	@Override
	public void addRole(PropertyExpression rd) {
		if (rd.isInverse())
			roles.add(rd.getInverse());
		else
			roles.add(rd);
	}

	@Override
	public Set<OClass> getConcepts() {
		return concepts;
	}

	@Override
	public Set<PropertyExpression> getRoles() {
		return roles;
	}

	@Override
	public void merge(Ontology onto) {
		concepts.addAll(onto.getConcepts());
		roles.addAll(onto.getRoles());
		
		subClassAxioms.addAll(onto.getSubClassAxioms());
		subPropertyAxioms.addAll(onto.getSubPropertyAxioms());
		disjointPropertiesAxioms.addAll(onto.getDisjointPropertiesAxioms());
		disjointClassesAxioms.addAll(onto.getDisjointClassesAxioms());
		functionalityAxioms.addAll(onto.getFunctionalPropertyAxioms());
		classAssertions.addAll(onto.getClassAssertions());
		propertyAssertions.addAll(onto.getPropertyAssertions());
	}
}
