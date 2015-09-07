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
import it.unibz.krdb.obda.ontology.DescriptionBT;
import it.unibz.krdb.obda.ontology.ImmutableOntologyVocabulary;
import it.unibz.krdb.obda.ontology.InconsistentOntologyException;
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

import com.google.common.collect.ImmutableList;
import com.google.common.collect.ImmutableMap;

public class OntologyImpl implements Ontology {

	private static final long serialVersionUID = 758424053258299151L;
	
	private static final OBDADataFactory obdafac = OBDADataFactoryImpl.getInstance();
	
	private final ImmutableOntologyVocabularyImpl vocabulary;
	
	// axioms 

	private final static class Hierarchy<T extends DescriptionBT> {
		private final List<BinaryAxiom<T>> inclusions = new ArrayList<>();
		private final List<NaryAxiom<T>> disjointness = new ArrayList<>();
		
		/**
		 * implements rules [D1], [O1] and [C1]:<br>
		 *    - ignore if e1 is bot or e2 is top<br>
		 *    - replace by emptiness if e2 is bot but e1 is not top<br>
		 *    - inconsistency if e1 is top and e2 is bot
		 *    
		 * @param e1
		 * @param e2
		 * @throws InconsistentOntologyException 
		 */
		
		void addInclusion(T e1, T e2) throws InconsistentOntologyException {
			if (e1.isBottom() || e2.isTop()) 
				return;
			
			if (e2.isBottom()) { // emptiness
				if (e1.isTop())
					throw new InconsistentOntologyException();
				NaryAxiom<T> ax = new NaryAxiomImpl<>(ImmutableList.of(e1, e1));
				disjointness.add(ax);
			}
			else {
				BinaryAxiom<T> ax = new BinaryAxiomImpl<>(e1, e2);
				inclusions.add(ax);
			}	
		}
		
		/**
		 * implements an extension of [D1], [O1] and [C1]:<br>
		 *     - eliminates all occurrences of bot and if the result contains<br>
		 *     - no top and at least two elements then disjointness<br>
		 *     - one top then emptiness of all other elements<br>
		 *     - two tops then inconsistency (this behavior is an extension of OWL 2, where duplicates are removed from the list)
		 *     
		 * @param es
		 * @throws InconsistentOntologyException
		 */
		
		void addDisjointness(T... es) throws InconsistentOntologyException {
			ImmutableList.Builder<T> sb = new ImmutableList.Builder<>();
			int numberOfTop = 0;
			for (T e : es) {
				//checkSignature(e);
				if (e.isBottom())
					continue;
				else if (e.isTop()) 
					numberOfTop++;
				else 
					sb.add(e);
			}
			ImmutableList<T> nonTrivialElements = sb.build();
			if (numberOfTop == 0) {
				if (nonTrivialElements.size() >= 2) {
					NaryAxiomImpl<T> ax = new NaryAxiomImpl<>(nonTrivialElements);
					disjointness.add(ax);
				}
				// if 0 or 1 non-bottom elements then do nothing 
			}
			else if (numberOfTop == 1) {
				for (T dpe : nonTrivialElements) {
					NaryAxiomImpl<T> ax = new NaryAxiomImpl<>(ImmutableList.of(dpe, dpe));
					disjointness.add(ax);
				}
			}
			else // many tops 
				throw new InconsistentOntologyException();
		}
	};

	private final Hierarchy<ClassExpression> classAxioms = new Hierarchy<>();
	private final Hierarchy<ObjectPropertyExpression> objectPropertyAxioms = new Hierarchy<>();
	private final Hierarchy<DataPropertyExpression> dataPropertyAxioms = new Hierarchy<>();
	
	private final List<BinaryAxiom<DataRangeExpression>> subDataRangeAxioms = new ArrayList<>();

	private final Set<ObjectPropertyExpression> functionalObjectPropertyAxioms = new LinkedHashSet<>();
	private final Set<DataPropertyExpression> functionalDataPropertyAxioms = new LinkedHashSet<>();
	
	// assertions
	
	private final List<ClassAssertion> classAssertions = new ArrayList<>();
	private final List<ObjectPropertyAssertion> objectPropertyAssertions = new ArrayList<>();
	private final List<DataPropertyAssertion> dataPropertyAssertions = new ArrayList<>();

	// auxiliary symbols (for normalization)
	
	private final Set<ObjectPropertyExpression> auxObjectProperties = new HashSet<>();
	private final Set<DataPropertyExpression> auxDataProperties = new HashSet<>();
	
	private static final String AUXROLEURI = "ER.A-AUXROLE"; 
	private int auxCounter = 0; 
	
	// exception messages
	
	private static final String CLASS_NOT_FOUND = "Class not found: ";	
	private static final String OBJECT_PROPERTY_NOT_FOUND = "ObjectProperty not found: ";
	private static final String DATA_PROPERTY_NOT_FOUND = "DataProperty not found: ";
	private static final String DATATYPE_NOT_FOUND = "Datatype not found: ";
	
	public static final ImmutableMap<String, Datatype> OWL2QLDatatypes;
	
	private static final String xml  = "http://www.w3.org/1999/02/22-rdf-syntax-ns#";
	private static final String rdfs = "http://www.w3.org/2000/01/rdf-schema#";	
	private static final String owl = "http://www.w3.org/2002/07/owl#";
	private static final String xsd = "http://www.w3.org/2001/XMLSchema#";
	
	static {
		DatatypeFactory ofac = obdafac.getDatatypeFactory();
		
		OWL2QLDatatypes = ImmutableMap.<String, Datatype>builder()
				.put(xml + "PlainLiteral", new DatatypeImpl(ofac.getTypePredicate(COL_TYPE.LITERAL))) // 	rdf:PlainLiteral
				.put(xml + "XMLLiteral", new DatatypeImpl(ofac.getTypePredicate(COL_TYPE.STRING))) //	rdf:XMLLiteral
				.put(rdfs + "Literal", new DatatypeImpl(ofac.getTypePredicate(COL_TYPE.LITERAL))) //		rdfs:Literal
				.put(owl + "real", new DatatypeImpl(ofac.getTypePredicate(COL_TYPE.DECIMAL))) // 			owl:real
				.put(owl + "rational", new DatatypeImpl(ofac.getTypePredicate(COL_TYPE.DECIMAL))) // 		owl:rational		
				.put(xsd + "decimal", new DatatypeImpl(ofac.getTypePredicate(COL_TYPE.DECIMAL))) // 	xsd:decimal
				.put(xsd + "integer", new DatatypeImpl(ofac.getTypePredicate(COL_TYPE.INTEGER))) // 	xsd:integer
				.put(xsd + "nonNegativeInteger", new DatatypeImpl(ofac.getTypePredicate(COL_TYPE.NON_NEGATIVE_INTEGER))) // 	xsd:nonNegativeInteger
				.put(xsd + "string", new DatatypeImpl(ofac.getTypePredicate(COL_TYPE.STRING))) // 	xsd:string
				.put(xsd + "normalizedString", new DatatypeImpl(ofac.getTypePredicate(COL_TYPE.STRING))) // 	xsd:normalizedString
				.put(xsd + "token", new DatatypeImpl(ofac.getTypePredicate(COL_TYPE.STRING))) // 	xsd:token
				.put(xsd + "Name", new DatatypeImpl(ofac.getTypePredicate(COL_TYPE.STRING))) // 	xsd:Name
				.put(xsd + "NCName", new DatatypeImpl(ofac.getTypePredicate(COL_TYPE.STRING))) //	xsd:NCName
				.put(xsd + "NMTOKEN", new DatatypeImpl(ofac.getTypePredicate(COL_TYPE.STRING))) // 	xsd:NMTOKEN
				.put(xsd + "hexBinary", new DatatypeImpl(ofac.getTypePredicate(COL_TYPE.STRING))) // 	xsd:hexBinary
				.put(xsd + "base64Binary", new DatatypeImpl(ofac.getTypePredicate(COL_TYPE.STRING))) // 	xsd:base64Binary
				.put(xsd + "anyURI", new DatatypeImpl(ofac.getTypePredicate(COL_TYPE.STRING))) // 	xsd:anyURI
				.put(xsd + "dateTime", new DatatypeImpl(ofac.getTypePredicate(COL_TYPE.DATETIME))) // 	xsd:dateTime
				.put(xsd + "dateTimeStamp", new DatatypeImpl(ofac.getTypePredicate(COL_TYPE.DATETIME_STAMP))) // 	xsd:dateTimeStamp
				.put(xsd + "int", new DatatypeImpl(ofac.getTypePredicate(COL_TYPE.INT))) // 	TEMPORARY FOR Q9 / FISHMARK
				.put(xsd + "long", new DatatypeImpl(ofac.getTypePredicate(COL_TYPE.LONG))) // 	TEMPORARY FOR OntologyTypesTest
				.build();
	}
	
/*	
	// PREVIOUSLY SUPPORTED built-in datatypes 
	
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
*/	
	
	
	
	
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
				throw new RuntimeException(CLASS_NOT_FOUND + uri);
			return oc;
		}

		@Override
		public ObjectPropertyExpression getObjectProperty(String uri) {
			ObjectPropertyExpression ope = objectProperties.get(uri);
			if (ope == null)
				throw new RuntimeException(OBJECT_PROPERTY_NOT_FOUND + uri);
			return ope;
		}

		@Override
		public DataPropertyExpression getDataProperty(String uri) {
			DataPropertyExpression dpe = dataProperties.get(uri);
			if (dpe == null)
				throw new RuntimeException(DATA_PROPERTY_NOT_FOUND + uri);
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

		@Override
		public Datatype getDatatype(String uri) {
			Datatype dt = OWL2QLDatatypes.get(uri);
			if (dt == null)
				throw new RuntimeException(DATATYPE_NOT_FOUND + uri);
			return dt;
		}
	}

	OntologyImpl(OntologyVocabularyImpl voc) {
		this.vocabulary = new ImmutableOntologyVocabularyImpl(voc);
	}
	
	@Override
	public ImmutableOntologyVocabulary getVocabulary() {
		return vocabulary;
	}
	

	/**
	 * Normalizes and adds subclass axiom
	 * <p>
	 * SubClassOf := 'SubClassOf' '(' axiomAnnotations subClassExpression superClassExpression ')'
	 * <p>
	 * Implements rule [C1]:<br>
	 *    - ignore the axiom if the first argument is owl:Nothing or the second argument is owl:Thing<br>
	 *    - replace by a disjointness axiom if the second argument is owl:Nothing but the first is not owl:Thing<br>
	 *    - inconsistency if the first argument is owl:Thing but the second one is not owl:Nothing
	 * <p>
	 * Implements rules [D5] and [O5] (in conjunction with DataSomeValuesFromImpl and ObjectSomeValuesFromImpl)<br>
	 *    - if the first argument is syntactically "equivalent" to owl:Thing, then replace it by owl:Thing
	 *    
	 * @throws InconsistentOntologyException 
	 */
	
	@Override
	public void addSubClassOfAxiom(ClassExpression ce1, ClassExpression ce2) throws InconsistentOntologyException {
		checkSignature(ce1);
		checkSignature(ce2);
		if (ce1.isTop())
			ce1 = ClassImpl.owlThing; // rule [D5] and [O5]
		classAxioms.addInclusion(ce1, ce2);
	}	
	
	/**
	 * Normalizes and adds a data property range axiom
	 * <p>
	 * DataPropertyRange := 'DataPropertyRange' '(' axiomAnnotations DataPropertyExpression DataRange ')'
	 * <p>
	 * Implements rule [D3]:
	 *     - ignore if the property is bot or the range is rdfs:Literal (top datatype)
	 *     - inconsistency if the property is top but the range is not rdfs:Literal
	 *     
	 * @throws InconsistentOntologyException 
	 */

	@Override
	public void addDataPropertyRangeAxiom(DataPropertyRangeExpression range, Datatype datatype) throws InconsistentOntologyException {
		checkSignature(range);
		checkSignature(datatype);
		if (datatype.equals(DatatypeImpl.rdfsLiteral))
			return;
		
		// otherwise the datatype is not top
		if (range.getProperty().isBottom())
			return;
		if (range.getProperty().isTop())
			throw new InconsistentOntologyException();
		
		BinaryAxiom<DataRangeExpression> ax = new BinaryAxiomImpl<>(range, datatype);
		subDataRangeAxioms.add(ax);
	}

	
	/**
	 * Normalizes and adds an object subproperty axiom
	 * <p>
	 * SubObjectPropertyOf := 'SubObjectPropertyOf' '(' axiomAnnotations 
	 * 						ObjectPropertyExpression ObjectPropertyExpression ')'
	 * <p>
	 * Implements rule [O1]:<br>
	 *    - ignore the axiom if the first argument is owl:bottomObjectProperty 
	 *    				or the second argument is owl:topObjectProperty<br>
	 *    - replace by a disjointness axiom if the second argument is owl:bottomObjectProperty 
	 *                but the first one is not owl:topObjectProperty<br>
	 *    - inconsistency if the first is  owl:topObjectProperty but the second is owl:bottomObjectProperty 
	 *    
	 * @throws InconsistentOntologyException 
	 * 
	 */
	
	@Override
	public void addSubPropertyOfAxiom(ObjectPropertyExpression ope1, ObjectPropertyExpression ope2) throws InconsistentOntologyException {
		checkSignature(ope1);
		checkSignature(ope2);
		objectPropertyAxioms.addInclusion(ope1, ope2);
	}
	
	/**
	 * Normalizes and adds a data subproperty axiom
	 * <p>
	 * SubDataPropertyOf := 'SubDataPropertyOf' '(' axiomAnnotations 
	 * 					subDataPropertyExpression superDataPropertyExpression ')'<br>
	 * subDataPropertyExpression := DataPropertyExpression<br>
	 * superDataPropertyExpression := DataPropertyExpression
	 * <p>
	 * implements rule [D1]:<br>
	 *    - ignore the axiom if the first argument is owl:bottomDataProperty 
	 *    			  or the second argument is owl:topDataProperty<br>
	 *    - replace by a disjointness axiom if the second argument is owl:bottomDataProperty 
	 *                but the first one is not owl:topDataProperty<br>
	 *    - inconsistency if the first is  owl:topDataProperty but the second is owl:bottomDataProperty 
	 *    
	 * @throws InconsistentOntologyException 
	 */
	
	@Override
	public void addSubPropertyOfAxiom(DataPropertyExpression dpe1, DataPropertyExpression dpe2) throws InconsistentOntologyException {
		checkSignature(dpe1);
		checkSignature(dpe2);
		dataPropertyAxioms.addInclusion(dpe1, dpe2);
	}

	
	@Override
	public void addDisjointClassesAxiom(ClassExpression... ces) throws InconsistentOntologyException {	
		for (ClassExpression c : ces)
			checkSignature(c);
		classAxioms.addDisjointness(ces);
	}

	@Override
	public void addDisjointObjectPropertiesAxiom(ObjectPropertyExpression... opes) throws InconsistentOntologyException {
		for (ObjectPropertyExpression p : opes)
			checkSignature(p);
		objectPropertyAxioms.addDisjointness(opes);
	}

	/**
	 * Normalizes and adds data property disjointness axiom
	 * <p>
	 * DisjointDataProperties := 'DisjointDataProperties' '(' axiomAnnotations 
	 * 				DataPropertyExpression DataPropertyExpression { DataPropertyExpression } ')'<br>
	 * <p>
	 * implements rule [D2]:<br>
	 *     - eliminates all occurrences of bot and if the result contains<br>
	 *     - no top and at least two elements then disjointness<br>
	 *     - one top then emptiness of all other elements<br>
	 *     - two tops then inconsistency (this behavior is an extension of OWL 2, where duplicates are removed from the list) 
	 */
	
	@Override
	public void addDisjointDataPropertiesAxiom(DataPropertyExpression... dpes) throws InconsistentOntologyException {
		for (DataPropertyExpression dpe : dpes)
			checkSignature(dpe);
		dataPropertyAxioms.addDisjointness(dpes);
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
	public Collection<BinaryAxiom<ClassExpression>> getSubClassAxioms() {
		return Collections.unmodifiableList(classAxioms.inclusions);
	}
	
	@Override
	public Collection<BinaryAxiom<DataRangeExpression>> getSubDataRangeAxioms() {
		return Collections.unmodifiableList(subDataRangeAxioms);
	}
	
	
	@Override
	public Collection<BinaryAxiom<ObjectPropertyExpression>> getSubObjectPropertyAxioms() {
		return Collections.unmodifiableList(objectPropertyAxioms.inclusions);
	}
	
	@Override
	public Collection<BinaryAxiom<DataPropertyExpression>> getSubDataPropertyAxioms() {
		return Collections.unmodifiableList(dataPropertyAxioms.inclusions);
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
	public Collection<NaryAxiom<ClassExpression>> getDisjointClassesAxioms() {
		return Collections.unmodifiableList(classAxioms.disjointness);
	}
	
	@Override 
	public Collection<NaryAxiom<ObjectPropertyExpression>> getDisjointObjectPropertiesAxioms() {
		return Collections.unmodifiableList(objectPropertyAxioms.disjointness);
	}

	@Override 
	public Collection<NaryAxiom<DataPropertyExpression>> getDisjointDataPropertiesAxioms() {
		return Collections.unmodifiableList(dataPropertyAxioms.disjointness);
	}

	
	@Override
	public String toString() {
		StringBuilder str = new StringBuilder();
		str.append("[Ontology info.")
		 	.append(String.format(" Axioms: %d", classAxioms.inclusions.size() + 
		 			objectPropertyAxioms.inclusions.size() + dataPropertyAxioms.inclusions.size()))
			.append(String.format(" Classes: %d", vocabulary.getClasses().size()))
			.append(String.format(" Object Properties: %d", vocabulary.getObjectProperties().size()))
			.append(String.format(" Data Properties: %d]", vocabulary.getDataProperties().size()));
		return str.toString();
	}

	
	@Override
	public ObjectPropertyExpression createAuxiliaryObjectProperty() {
		ObjectPropertyExpression ope = new ObjectPropertyExpressionImpl(AUXROLEURI + auxCounter);
		auxCounter++ ;
		auxObjectProperties.add(ope);
		return ope;
	}
	
	@Override
	public DataPropertyExpression createAuxiliaryDataProperty() {
		DataPropertyExpression dpe = new DataPropertyExpressionImpl(AUXROLEURI + auxCounter);
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

	
	
	private void checkSignature(ClassExpression desc) {		
		if (desc instanceof OClass) {
			OClass cl = (OClass) desc;
			if (!vocabulary.concepts.containsKey(cl.getName()))
				throw new IllegalArgumentException(CLASS_NOT_FOUND + desc);
		}	
		else if (desc instanceof ObjectSomeValuesFrom) {
			checkSignature(((ObjectSomeValuesFrom) desc).getProperty());
		}
		else  {
			assert (desc instanceof DataSomeValuesFrom);
			checkSignature(((DataSomeValuesFrom) desc).getProperty());
		}
	}	
	
	private void checkSignature(Datatype desc) {		
		Predicate pred = desc.getPredicate();
		if (!OWL2QLDatatypes.containsKey(pred.getName())) 
			throw new IllegalArgumentException(DATATYPE_NOT_FOUND + pred);
	}
	
	private void checkSignature(DataPropertyRangeExpression desc) {		
		checkSignature(desc.getProperty());
	}

	private void checkSignature(ObjectPropertyExpression prop) {	
		if (prop.isInverse()) 
			prop = prop.getInverse();
		
		if (!vocabulary.containsObjectProperty(prop.getName()) && !auxObjectProperties.contains(prop)) 
				throw new IllegalArgumentException(OBJECT_PROPERTY_NOT_FOUND + prop);
	}

	private void checkSignature(DataPropertyExpression prop) {
		if (!vocabulary.containsDataProperty(prop.getName()) && !auxDataProperties.contains(prop))
			throw new IllegalArgumentException(DATA_PROPERTY_NOT_FOUND + prop);
	}

	@Override
	public void addReflexiveObjectPropertyAxiom(ObjectPropertyExpression ope) {
		// TODO Auto-generated method stub
		
	}

	@Override
	public void addIrreflexiveObjectPropertyAxiom(ObjectPropertyExpression ope) {
		// TODO Auto-generated method stub
		
	}
	
}
