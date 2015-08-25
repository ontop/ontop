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

import it.unibz.krdb.obda.model.DatatypeFactory;
import it.unibz.krdb.obda.model.OBDADataFactory;
import it.unibz.krdb.obda.model.Predicate;
import it.unibz.krdb.obda.model.Predicate.COL_TYPE;
import it.unibz.krdb.obda.model.impl.OBDADataFactoryImpl;
import it.unibz.krdb.obda.ontology.ClassAssertion;
import it.unibz.krdb.obda.ontology.DataPropertyAssertion;
import it.unibz.krdb.obda.ontology.DataPropertyExpression;
import it.unibz.krdb.obda.ontology.DataPropertyRangeExpression;
import it.unibz.krdb.obda.ontology.DataRangeExpression;
import it.unibz.krdb.obda.ontology.DataSomeValuesFrom;
import it.unibz.krdb.obda.ontology.Datatype;
import it.unibz.krdb.obda.ontology.ImmutableOntologyVocabulary;
import it.unibz.krdb.obda.ontology.NaryAxiom;
import it.unibz.krdb.obda.ontology.OClass;
import it.unibz.krdb.obda.ontology.ObjectPropertyAssertion;
import it.unibz.krdb.obda.ontology.ObjectPropertyExpression;
import it.unibz.krdb.obda.ontology.ObjectSomeValuesFrom;
import it.unibz.krdb.obda.ontology.Ontology;
import it.unibz.krdb.obda.ontology.ClassExpression;
import it.unibz.krdb.obda.ontology.BinaryAxiom;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.HashSet;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Set;

import com.google.common.collect.ImmutableMap;
import com.google.common.collect.ImmutableSet;

public class OntologyImpl implements Ontology {

	private static final long serialVersionUID = 758424053258299151L;
	
	private static final OBDADataFactory obdafac = OBDADataFactoryImpl.getInstance();
	
	private final ImmutableOntologyVocabularyImpl vocabulary;
	
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

	
	private final class ImmutableOntologyVocabularyImpl implements ImmutableOntologyVocabulary {

		final ImmutableMap<String, OClass> concepts;
		final ImmutableMap<String, ObjectPropertyExpression> objectProperties;
		final ImmutableMap<String, DataPropertyExpression> dataProperties;
		
		ImmutableOntologyVocabularyImpl(OntologyVocabularyImpl voc) {
			concepts = ImmutableMap.<String, OClass>builder()
				.putAll(voc.concepts)
				.put(ClassImpl.owlThingIRI, ClassImpl.owlThing)
				.put(ClassImpl.owlNothingIRI, ClassImpl.owlNothing).build();
			objectProperties = ImmutableMap.<String, ObjectPropertyExpression>builder()
				.putAll(voc.objectProperties)
				.put(ObjectPropertyExpressionImpl.owlTopObjectPropertyIRI, ObjectPropertyExpressionImpl.owlTopObjectProperty)
				.put(ObjectPropertyExpressionImpl.owlBottomObjectPropertyIRI, ObjectPropertyExpressionImpl.owlBottomObjectProperty).build();
			dataProperties  = ImmutableMap.<String, DataPropertyExpression>builder() 
				.putAll(voc.dataProperties)
				.put(DataPropertyExpressionImpl.owlTopDataPropertyIRI, DataPropertyExpressionImpl.owlTopDataProperty)
				.put(DataPropertyExpressionImpl.owlBottomDataPropertyIRI, DataPropertyExpressionImpl.owlBottomDataProperty).build();
		}
		
		@Override
		public OClass getClass(String uri) {
			OClass oc = concepts.get(uri);
			if (oc == null)
				throw new RuntimeException("Class not found: " + uri);
			return oc;
		}

		@Override
		public ObjectPropertyExpression getObjectProperty(String uri) {
			ObjectPropertyExpression ope = objectProperties.get(uri);
			if (ope == null)
				throw new RuntimeException("ObjectProperty not found: " + uri);
			return ope;
		}

		@Override
		public DataPropertyExpression getDataProperty(String uri) {
			DataPropertyExpression dpe = dataProperties.get(uri);
			if (dpe == null)
				throw new RuntimeException("DataProperty not found: " + uri);
			return dpe;
		}

		@Override
		public boolean containsClass(String uri) {
			return concepts.containsKey(uri);
		}

		@Override
		public boolean containsObjectProperty(String uri) {
			return objectProperties.containsKey(uri);
		}

		@Override
		public boolean containsDataProperty(String uri) {
			return dataProperties.containsKey(uri);
		}

		@Override
		public Collection<OClass> getClasses() {
			return concepts.values();
		}

		@Override
		public Collection<ObjectPropertyExpression> getObjectProperties() {
			return objectProperties.values();
		}

		@Override
		public Collection<DataPropertyExpression> getDataProperties() {
			return dataProperties.values();
		}

		@Override
		public boolean isEmpty() {
			// the minimum size is 2 because of \top / \bopttom
			return concepts.size() == 2 && objectProperties.size() == 2 && dataProperties.size() == 2;
		}
	}

	OntologyImpl(OntologyVocabularyImpl voc) {
		this.vocabulary = new ImmutableOntologyVocabularyImpl(voc);
	}
	
	@Override
	public ImmutableOntologyVocabulary getVocabulary() {
		return vocabulary;
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

	private final Set<ObjectPropertyExpression> auxObjectProperties = new HashSet<>();
	private final Set<DataPropertyExpression> auxDataProperties = new HashSet<>();
	
	public static final String AUXROLEURI = "ER.A-AUXROLE"; 
	private static int auxCounter = 0; // THIS IS SHARED AMONG ALL INSTANCES!
	
	
	@Override
	public ObjectPropertyExpression createAuxiliaryObjectProperty() {
		Predicate prop = obdafac.getDataPropertyPredicate(AUXROLEURI + auxCounter);
		ObjectPropertyExpression ope = new ObjectPropertyExpressionImpl(prop);
		auxCounter++ ;
		auxObjectProperties.add(ope);
		return ope;
	}
	
	@Override
	public DataPropertyExpression createAuxiliaryDataProperty() {
		Predicate prop = obdafac.getDataPropertyPredicate(AUXROLEURI + auxCounter);
		DataPropertyExpression dpe = new DataPropertyExpressionImpl(prop);
		auxCounter++ ;
		auxDataProperties.add(dpe);
		return dpe;
	}
	
	@Override
	public Collection<ObjectPropertyExpression> getAuxiliaryObjectProperties() {
		return Collections.unmodifiableSet(auxObjectProperties);
	}

	@Override
	public Collection<DataPropertyExpression> getAuxiliaryDataProperties() {
		return Collections.unmodifiableSet(auxDataProperties);
	}

	
	
	// built-in datatypes 
	
	final static Set<Predicate> builtinDatatypes;

	static { // static block
		DatatypeFactory dfac = OBDADataFactoryImpl.getInstance().getDatatypeFactory();
		
		builtinDatatypes = new HashSet<>();
		builtinDatatypes.add(dfac.getTypePredicate(COL_TYPE.LITERAL)); //  .RDFS_LITERAL);
		builtinDatatypes.add(dfac.getTypePredicate(COL_TYPE.STRING)); // .XSD_STRING);
		builtinDatatypes.add(dfac.getTypePredicate(COL_TYPE.INTEGER)); //OBDAVocabulary.XSD_INTEGER);
		builtinDatatypes.add(dfac.getTypePredicate(COL_TYPE.NEGATIVE_INTEGER)); // XSD_NEGATIVE_INTEGER);
		builtinDatatypes.add(dfac.getTypePredicate(COL_TYPE.INT)); // OBDAVocabulary.XSD_INT);
		builtinDatatypes.add(dfac.getTypePredicate(COL_TYPE.NON_NEGATIVE_INTEGER)); //OBDAVocabulary.XSD_NON_NEGATIVE_INTEGER);
		builtinDatatypes.add(dfac.getTypePredicate(COL_TYPE.UNSIGNED_INT)); // OBDAVocabulary.XSD_UNSIGNED_INT);
		builtinDatatypes.add(dfac.getTypePredicate(COL_TYPE.POSITIVE_INTEGER)); //.XSD_POSITIVE_INTEGER);
		builtinDatatypes.add(dfac.getTypePredicate(COL_TYPE.NON_POSITIVE_INTEGER)); // OBDAVocabulary.XSD_NON_POSITIVE_INTEGER);
		builtinDatatypes.add(dfac.getTypePredicate(COL_TYPE.LONG)); // OBDAVocabulary.XSD_LONG);
		builtinDatatypes.add(dfac.getTypePredicate(COL_TYPE.DECIMAL)); // OBDAVocabulary.XSD_DECIMAL);
		builtinDatatypes.add(dfac.getTypePredicate(COL_TYPE.DOUBLE)); // OBDAVocabulary.XSD_DOUBLE);
		builtinDatatypes.add(dfac.getTypePredicate(COL_TYPE.FLOAT)); // OBDAVocabulary.XSD_FLOAT);
		builtinDatatypes.add(dfac.getTypePredicate(COL_TYPE.DATETIME)); // OBDAVocabulary.XSD_DATETIME);
		builtinDatatypes.add(dfac.getTypePredicate(COL_TYPE.BOOLEAN)); // OBDAVocabulary.XSD_BOOLEAN
		builtinDatatypes.add(dfac.getTypePredicate(COL_TYPE.DATETIME_STAMP)); // OBDAVocabulary.XSD_DATETIME_STAMP
	}
	
	
	
	private void checkSignature(ClassExpression desc) {
		
		if (desc instanceof OClass) {
			OClass cl = (OClass) desc;
			if (!vocabulary.concepts.containsKey(cl.getPredicate().getName()))
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
	
	private void checkSignature(DataRangeExpression desc) {
		
		if (desc instanceof Datatype) {
			Predicate pred = ((Datatype) desc).getPredicate();
			if (!builtinDatatypes.contains(pred)) 
				throw new IllegalArgumentException("Datatype predicate is unknown: " + pred);
		}
		else {
			assert (desc instanceof DataPropertyRangeExpression);
			checkSignature(((DataPropertyRangeExpression) desc).getProperty());
		}
	}

	private void checkSignature(ObjectPropertyExpression prop) {	
		if (prop.isInverse()) 
			prop = prop.getInverse();
		
		if (!vocabulary.objectProperties.containsKey(prop.getPredicate().getName()) && !auxObjectProperties.contains(prop)) 
				throw new IllegalArgumentException("At least one of these predicates is unknown: " + prop);
	}

	private void checkSignature(DataPropertyExpression prop) {
		if (!vocabulary.dataProperties.containsKey(prop.getPredicate().getName()) && !auxDataProperties.contains(prop))
			throw new IllegalArgumentException("At least one of these predicates is unknown: " + prop);
	}
	
}
