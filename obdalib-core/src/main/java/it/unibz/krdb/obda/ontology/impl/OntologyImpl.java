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
import it.unibz.krdb.obda.ontology.ClassAssertion;
import it.unibz.krdb.obda.ontology.DataPropertyAssertion;
import it.unibz.krdb.obda.ontology.DataPropertyExpression;
import it.unibz.krdb.obda.ontology.DataPropertyRangeExpression;
import it.unibz.krdb.obda.ontology.DataRangeExpression;
import it.unibz.krdb.obda.ontology.DataSomeValuesFrom;
import it.unibz.krdb.obda.ontology.Datatype;
import it.unibz.krdb.obda.ontology.NaryAxiom;
import it.unibz.krdb.obda.ontology.OClass;
import it.unibz.krdb.obda.ontology.ObjectPropertyAssertion;
import it.unibz.krdb.obda.ontology.ObjectPropertyExpression;
import it.unibz.krdb.obda.ontology.ObjectSomeValuesFrom;
import it.unibz.krdb.obda.ontology.Ontology;
import it.unibz.krdb.obda.ontology.OntologyFactory;
import it.unibz.krdb.obda.ontology.OntologyVocabulary;
import it.unibz.krdb.obda.ontology.ClassExpression;
import it.unibz.krdb.obda.ontology.BinaryAxiom;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.HashSet;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Set;

import com.google.common.collect.ImmutableSet;

public class OntologyImpl implements Ontology {

	private static final long serialVersionUID = 758424053258299151L;
	
	private static final OntologyFactory ofac = OntologyFactoryImpl.getInstance();
	
	private final OntologyVocabularyImpl vocabulary;
	
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
	

	OntologyImpl(OntologyVocabularyImpl voc) {
		this.vocabulary = voc;
	}
	

	
	@Override
	public void addSubClassOfAxiomWithReferencedEntities(ClassExpression concept1, ClassExpression concept2) {	
		addReferencedEntries(concept1);
		addReferencedEntries(concept2);
		if (!concept1.isNothing() && !concept2.isThing()) {
			BinaryAxiom<ClassExpression> assertion = new BinaryAxiomImpl<ClassExpression>(concept1, concept2);
			subClassAxioms.add(assertion);
		}
	}

	@Override
	public void addSubClassOfAxiomWithReferencedEntities(DataRangeExpression concept1, DataRangeExpression concept2) {
		addReferencedEntries(concept1);
		addReferencedEntries(concept2);
		BinaryAxiom<DataRangeExpression> assertion = new BinaryAxiomImpl<DataRangeExpression>(concept1, concept2);
		subDataRangeAxioms.add(assertion);
	}
	
	@Override
	public void addSubPropertyOfAxiomWithReferencedEntities(ObjectPropertyExpression included, ObjectPropertyExpression including) {
		addReferencedEntries(included);
		addReferencedEntries(including);
		if (!included.isBottom() && !including.isTop()) {
			BinaryAxiom<ObjectPropertyExpression> assertion = new BinaryAxiomImpl<ObjectPropertyExpression>(included, including);
			subObjectPropertyAxioms.add(assertion);
		}
	}
	
	@Override
	public void addSubPropertyOfAxiomWithReferencedEntities(DataPropertyExpression included, DataPropertyExpression including) {
		addReferencedEntries(included);
		addReferencedEntries(including);
		if (!included.isBottom() && !including.isTop()) {
			BinaryAxiom<DataPropertyExpression> assertion = new BinaryAxiomImpl<DataPropertyExpression>(included, including);
			subDataPropertyAxioms.add(assertion);
		}
	}

	@Override
	public void addSubClassOfAxiom(ClassExpression concept1, ClassExpression concept2) {
		checkSignature(concept1);
		checkSignature(concept2);
		if (!concept1.isNothing() && !concept2.isThing()) {
			BinaryAxiom<ClassExpression> ax = new BinaryAxiomImpl<ClassExpression>(concept1, concept2);
			subClassAxioms.add(ax);
		}
	}	

	@Override
	public void addSubClassOfAxiom(DataRangeExpression concept1, DataRangeExpression concept2) {
		checkSignature(concept1);
		checkSignature(concept2);
		BinaryAxiom<DataRangeExpression> ax = new BinaryAxiomImpl<DataRangeExpression>(concept1, concept2);
		subDataRangeAxioms.add(ax);
	}

	@Override
	public void addSubPropertyOfAxiom(ObjectPropertyExpression included, ObjectPropertyExpression including) {
		checkSignature(included);
		checkSignature(including);
		if (!included.isBottom() && !including.isTop()) {
			BinaryAxiom<ObjectPropertyExpression> ax = new BinaryAxiomImpl<ObjectPropertyExpression>(included, including);
			subObjectPropertyAxioms.add(ax);
		}
	}
	
	@Override
	public void addSubPropertyOfAxiom(DataPropertyExpression included, DataPropertyExpression including) {
		checkSignature(included);
		checkSignature(including);
		if (!included.isBottom() && !including.isTop()) {
			BinaryAxiom<DataPropertyExpression> ax = new BinaryAxiomImpl<DataPropertyExpression>(included, including);
			subDataPropertyAxioms.add(ax);
		}
	}

	@Override
	public void addDisjointClassesAxiom(ImmutableSet<ClassExpression> classes) {	
		for (ClassExpression c : classes)
			checkSignature(c);
		NaryAxiom<ClassExpression> ax = new NaryAxiomImpl<ClassExpression>(classes);
		disjointClassesAxioms.add(ax);
	}

	@Override
	public void addDisjointObjectPropertiesAxiom(ImmutableSet<ObjectPropertyExpression> props) {
		for (ObjectPropertyExpression p : props)
			checkSignature(p);
		NaryAxiomImpl<ObjectPropertyExpression> ax = new NaryAxiomImpl<ObjectPropertyExpression>(props);
		disjointObjectPropertiesAxioms.add(ax);
	}

	@Override
	public void addDisjointDataPropertiesAxiom(ImmutableSet<DataPropertyExpression> props) {
		for (DataPropertyExpression p : props)
			checkSignature(p);
		NaryAxiomImpl<DataPropertyExpression> ax = new NaryAxiomImpl<DataPropertyExpression>(props);
		disjointDataPropertiesAxioms.add(ax);
	}
	
	@Override
	public void addFunctionalObjectPropertyAxiom(ObjectPropertyExpression prop) {
		checkSignature(prop);
		functionalObjectPropertyAxioms.add(prop);
	}

	@Override
	public void addFunctionalDataPropertyAxiom(DataPropertyExpression prop) {
		checkSignature(prop);
		functionalDataPropertyAxioms.add(prop);
	}
	
	@Override
	public void addClassAssertion(ClassAssertion assertion) {
		checkSignature(assertion.getConcept());
		classAssertions.add(assertion);
	}

	@Override
	public void addObjectPropertyAssertion(ObjectPropertyAssertion assertion) {
		checkSignature(assertion.getProperty());
		objectPropertyAssertions.add(assertion);
	}
	
	@Override
	public void addDataPropertyAssertion(DataPropertyAssertion assertion) {
		checkSignature(assertion.getProperty());
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
	
	private final Set<ObjectPropertyExpression> auxObjectProperties = new HashSet<>();

	private final Set<DataPropertyExpression> auxDataProperties = new HashSet<>();
	
	
	private static int auxCounter = 0; // THIS IS SHARED AMONG ALL INSTANCES!
	
	@Override
	public ObjectPropertyExpression createAuxiliaryObjectProperty() {
		ObjectPropertyExpression rd = ofac.createObjectProperty(AUXROLEURI + auxCounter);
		auxCounter++ ;
		auxObjectProperties.add(rd);
		return rd;
	}
	
	@Override
	public DataPropertyExpression createAuxiliaryDataProperty() {
		DataPropertyExpression rd = ofac.createDataProperty(AUXROLEURI + auxCounter);
		auxCounter++ ;
		auxDataProperties.add(rd);
		return rd;
	}
	
	@Override
	public Collection<ObjectPropertyExpression> getAuxiliaryObjectProperties() {
		return Collections.unmodifiableSet(auxObjectProperties);
	}

	@Override
	public Collection<DataPropertyExpression> getAuxiliaryDataProperties() {
		return Collections.unmodifiableSet(auxDataProperties);
	}

	
	public static final String AUXROLEURI = "ER.A-AUXROLE"; 
	
	
	boolean addReferencedEntries(ClassExpression desc) {
		if (desc instanceof OClass) {
			OClass cl = (OClass)desc;
			if (!isBuiltIn(cl)) {
				vocabulary.concepts.put(cl.getPredicate().getName(), cl);
				return true;
			}
		}
		else if (desc instanceof ObjectSomeValuesFrom)  {
			ObjectPropertyExpression prop = ((ObjectSomeValuesFrom) desc).getProperty();
			return addReferencedEntries(prop);
		}
		else  {
			assert (desc instanceof DataSomeValuesFrom);
			DataPropertyExpression prop = ((DataSomeValuesFrom) desc).getProperty();
			return addReferencedEntries(prop);
		}
		return false;
	}
	
	boolean addReferencedEntries(DataRangeExpression desc) {
		if (desc instanceof Datatype)  {
			// NO-OP
			// datatypes.add((Datatype) desc);
			return true;
		}
		else  {
			assert (desc instanceof DataPropertyRangeExpression);
			DataPropertyExpression prop = ((DataPropertyRangeExpression) desc).getProperty();
			return addReferencedEntries(prop);			
		}
	}
	
	boolean addReferencedEntries(ObjectPropertyExpression prop) {
		if (prop.isInverse()) {
			if (!isBuiltIn(prop.getInverse())) {
				ObjectPropertyExpression p = prop.getInverse();
				if (p.getPredicate().getName().toString().startsWith(AUXROLEURI))
					auxObjectProperties.add(p);
				else
					vocabulary.objectProperties.put(p.getPredicate().getName(), p);
				return true;
			}
		}
		else {
			if (!isBuiltIn(prop)) {
				if (prop.getPredicate().getName().toString().startsWith(AUXROLEURI))
					auxObjectProperties.add(prop);
				else
					vocabulary.objectProperties.put(prop.getPredicate().getName(), prop);
				return true;
			}			
		}
		return false;
	}
	
	boolean addReferencedEntries(DataPropertyExpression prop) {
		if (!isBuiltIn(prop)) {
			if (prop.getPredicate().getName().toString().startsWith(AUXROLEURI))
				auxDataProperties.add(prop);
			else
				vocabulary.dataProperties.put(prop.getPredicate().getName(), prop);
			return true;
		}
		return false;
	}
	
	private boolean isBuiltIn(OClass cl) {
		return cl.isNothing() || cl.isThing();
	}
	
	private boolean isBuiltIn(ObjectPropertyExpression prop) {
		return prop.isBottom() || prop.isTop() || auxObjectProperties.contains(prop);
	}

	private boolean isBuiltIn(DataPropertyExpression prop) {
		return prop.isBottom() || prop.isTop() || auxDataProperties.contains(prop);
	}
	
	void checkSignature(ClassExpression desc) {
		
		if (desc instanceof OClass) {
			OClass cl = (OClass) desc;
			if (!vocabulary.concepts.containsKey(cl.getPredicate().getName()) && !isBuiltIn(cl))
				throw new IllegalArgumentException("Class predicate is unknown: " + desc);
		}	
		else if (desc instanceof ObjectSomeValuesFrom) {
			checkSignature(((ObjectSomeValuesFrom) desc).getProperty());
		}
		else  {
			assert (desc instanceof DataSomeValuesFrom);
			checkSignature(((DataSomeValuesFrom) desc).getProperty());
		}
	}	
	
	void checkSignature(DataRangeExpression desc) {
		
		if (desc instanceof Datatype) {
			Predicate pred = ((Datatype) desc).getPredicate();
			if (!vocabulary.builtinDatatypes.contains(pred)) 
				throw new IllegalArgumentException("Datatype predicate is unknown: " + pred);
		}
		else {
			assert (desc instanceof DataPropertyRangeExpression);
			checkSignature(((DataPropertyRangeExpression) desc).getProperty());
		}
	}

	void checkSignature(ObjectPropertyExpression prop) {

		if (prop.isInverse()) {
			if (!vocabulary.objectProperties.containsKey(prop.getInverse().getPredicate().getName()) && !isBuiltIn(prop.getInverse())) 
				throw new IllegalArgumentException("At least one of these predicates is unknown: " + prop.getInverse());
		}
		else {
			if (!vocabulary.objectProperties.containsKey(prop.getPredicate().getName()) && !isBuiltIn(prop)) 
				throw new IllegalArgumentException("At least one of these predicates is unknown: " + prop);
		}
	}

	void checkSignature(DataPropertyExpression prop) {
		if (!vocabulary.dataProperties.containsKey(prop.getPredicate().getName()) && !isBuiltIn(prop))
			throw new IllegalArgumentException("At least one of these predicates is unknown: " + prop);
	}
	
}
