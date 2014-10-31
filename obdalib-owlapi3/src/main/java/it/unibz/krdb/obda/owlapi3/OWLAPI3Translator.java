package it.unibz.krdb.obda.owlapi3;

/*
 * #%L
 * ontop-obdalib-owlapi3
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

import it.unibz.krdb.obda.model.OBDADataFactory;
import it.unibz.krdb.obda.model.Predicate;
import it.unibz.krdb.obda.model.Predicate.COL_TYPE;
import it.unibz.krdb.obda.model.URIConstant;
import it.unibz.krdb.obda.model.ValueConstant;
import it.unibz.krdb.obda.model.impl.OBDADataFactoryImpl;
import it.unibz.krdb.obda.ontology.BasicClassDescription;
import it.unibz.krdb.obda.ontology.ClassAssertion;
import it.unibz.krdb.obda.ontology.DataPropertyExpression;
import it.unibz.krdb.obda.ontology.DataPropertyRangeExpression;
import it.unibz.krdb.obda.ontology.Datatype;
import it.unibz.krdb.obda.ontology.DisjointClassesAxiom;
import it.unibz.krdb.obda.ontology.DisjointPropertiesAxiom;
import it.unibz.krdb.obda.ontology.LanguageProfile;
import it.unibz.krdb.obda.ontology.OClass;
import it.unibz.krdb.obda.ontology.ObjectPropertyExpression;
import it.unibz.krdb.obda.ontology.Ontology;
import it.unibz.krdb.obda.ontology.OntologyFactory;
import it.unibz.krdb.obda.ontology.OntologyVocabulary;
import it.unibz.krdb.obda.ontology.PropertyExpression;
import it.unibz.krdb.obda.ontology.FunctionalPropertyAxiom;
import it.unibz.krdb.obda.ontology.PropertyAssertion;
import it.unibz.krdb.obda.ontology.SomeValuesFrom;
import it.unibz.krdb.obda.ontology.ClassExpression;
import it.unibz.krdb.obda.ontology.SubClassOfAxiom;
import it.unibz.krdb.obda.ontology.SubPropertyOfAxiom;
import it.unibz.krdb.obda.ontology.impl.OntologyFactoryImpl;
import it.unibz.krdb.obda.ontology.impl.OntologyVocabularyImpl;
import it.unibz.krdb.obda.ontology.impl.PunningException;

import java.io.BufferedWriter;
import java.io.File;
import java.io.FileWriter;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.semanticweb.owlapi.model.OWLAnnotationAxiom;
import org.semanticweb.owlapi.model.OWLAsymmetricObjectPropertyAxiom;
import org.semanticweb.owlapi.model.OWLAxiom;
import org.semanticweb.owlapi.model.OWLClass;
import org.semanticweb.owlapi.model.OWLClassAssertionAxiom;
import org.semanticweb.owlapi.model.OWLClassExpression;
import org.semanticweb.owlapi.model.OWLDataMinCardinality;
import org.semanticweb.owlapi.model.OWLDataProperty;
import org.semanticweb.owlapi.model.OWLDataPropertyAssertionAxiom;
import org.semanticweb.owlapi.model.OWLDataPropertyDomainAxiom;
import org.semanticweb.owlapi.model.OWLDataPropertyExpression;
import org.semanticweb.owlapi.model.OWLDataPropertyRangeAxiom;
import org.semanticweb.owlapi.model.OWLDataRange;
import org.semanticweb.owlapi.model.OWLDataSomeValuesFrom;
import org.semanticweb.owlapi.model.OWLDatatype;
import org.semanticweb.owlapi.model.OWLDeclarationAxiom;
import org.semanticweb.owlapi.model.OWLDisjointClassesAxiom;
import org.semanticweb.owlapi.model.OWLDisjointDataPropertiesAxiom;
import org.semanticweb.owlapi.model.OWLDisjointObjectPropertiesAxiom;
import org.semanticweb.owlapi.model.OWLEntity;
import org.semanticweb.owlapi.model.OWLEquivalentClassesAxiom;
import org.semanticweb.owlapi.model.OWLEquivalentDataPropertiesAxiom;
import org.semanticweb.owlapi.model.OWLEquivalentObjectPropertiesAxiom;
import org.semanticweb.owlapi.model.OWLFunctionalDataPropertyAxiom;
import org.semanticweb.owlapi.model.OWLFunctionalObjectPropertyAxiom;
import org.semanticweb.owlapi.model.OWLIndividual;
import org.semanticweb.owlapi.model.OWLInverseFunctionalObjectPropertyAxiom;
import org.semanticweb.owlapi.model.OWLInverseObjectPropertiesAxiom;
import org.semanticweb.owlapi.model.OWLLiteral;
import org.semanticweb.owlapi.model.OWLObjectIntersectionOf;
import org.semanticweb.owlapi.model.OWLObjectInverseOf;
import org.semanticweb.owlapi.model.OWLObjectMinCardinality;
import org.semanticweb.owlapi.model.OWLObjectProperty;
import org.semanticweb.owlapi.model.OWLObjectPropertyAssertionAxiom;
import org.semanticweb.owlapi.model.OWLObjectPropertyDomainAxiom;
import org.semanticweb.owlapi.model.OWLObjectPropertyExpression;
import org.semanticweb.owlapi.model.OWLObjectPropertyRangeAxiom;
import org.semanticweb.owlapi.model.OWLObjectSomeValuesFrom;
import org.semanticweb.owlapi.model.OWLOntology;
import org.semanticweb.owlapi.model.OWLSubClassOfAxiom;
import org.semanticweb.owlapi.model.OWLSubDataPropertyOfAxiom;
import org.semanticweb.owlapi.model.OWLSubObjectPropertyOfAxiom;
import org.semanticweb.owlapi.model.OWLSymmetricObjectPropertyAxiom;
import org.semanticweb.owlapi.profiles.OWL2QLProfile;
import org.semanticweb.owlapi.profiles.OWLProfileReport;
import org.semanticweb.owlapi.vocab.OWL2Datatype;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/***
 * Translates an OWLOntology into ontop's internal ontology representation. It
 * does a syntactic approximation of the ontology, dropping anything not supported 
 * by Quest during inference.
 * 
 */
public class OWLAPI3Translator {

	private static final LanguageProfile profile = LanguageProfile.DLLITEA;

	private static final OBDADataFactory dfac = OBDADataFactoryImpl.getInstance();
	private static final OntologyFactory ofac = OntologyFactoryImpl.getInstance();

	private static final Logger log = LoggerFactory.getLogger(OWLAPI3Translator.class);

	/*
	 * If we need to construct auxiliary subclass axioms for A ISA exists R.C we
	 * put them in this map to avoid generating too many auxiliary
	 * roles/classes.
	 */
	private final Map<OWLObjectSomeValuesFrom, SomeValuesFrom> auxiliaryClassProperties 
							= new HashMap<OWLObjectSomeValuesFrom, SomeValuesFrom>();
	
	private final Map<OWLDataSomeValuesFrom, SomeValuesFrom> auxiliaryDatatypeProperties 
							= new HashMap<OWLDataSomeValuesFrom, SomeValuesFrom>();

	private int auxRoleCounter = 0;

	public OWLAPI3Translator() {

	}

	/***
	 * Load all the ontologies into a single translated merge.
	 * 
	 * @param ontologies
	 * @return
	 * @throws PunningException 
	 * @throws Exception
	 */
	public Ontology mergeTranslateOntologies(Set<OWLOntology> ontologies) throws PunningException  {
		/*
		 * We will keep track of the loaded ontologies and translate the TBox
		 * part of them into our internal representation
		 */
		log.debug("Load ontologies called. Translating ontologies.");

		Ontology translation = ofac.createOntology();
		for (OWLOntology onto : ontologies) {
			Ontology aux = translate(onto);
			translation.merge(aux);
		}

		log.debug("Ontology loaded: {}", translation);

		return translation;

	}

	// USED ONLY IN OBDAModelManager
	@Deprecated
	public static Predicate getPredicate(OWLEntity entity) {
		Predicate p = null;
		if (entity instanceof OWLClass) {
			/* We ignore TOP and BOTTOM (Thing and Nothing) */
			if (((OWLClass) entity).isOWLThing() || ((OWLClass) entity).isOWLNothing()) {
				return null;
			}
			String uri = entity.getIRI().toString();

			p = dfac.getClassPredicate(uri);
		} else if (entity instanceof OWLObjectProperty) {
			String uri = entity.getIRI().toString();

			p = dfac.getObjectPropertyPredicate(uri);
		} else if (entity instanceof OWLDataProperty) {
			String uri = entity.getIRI().toString();

			p = dfac.getDataPropertyPredicate(uri);
		}
		return p;
	}
	
	
	/**
	 * 
	 * @param owl
	 * @return
	 * @throws PunningException
	 */

	public Ontology translate(OWLOntology owl) throws PunningException {

		OWL2QLProfile owlprofile = new OWL2QLProfile();
		OWLProfileReport report = owlprofile.checkOntology(owl);
		if (!report.isInProfile()) {
			log.warn("WARNING. The current ontology is not in the OWL 2 QL profile.");
			try {
				File profileReport = new File("quest-profile-report.log");
				if (profileReport.canWrite()) {
					BufferedWriter bf = new BufferedWriter(new FileWriter(profileReport));
					bf.write(report.toString());
					bf.flush();
					bf.close();
				}
			} catch (Exception e) {

			}
		}

		Ontology dl_onto = ofac.createOntology();

		processEntities(dl_onto.getVocabulary(), owl);
		
		for (OWLAxiom axiom : owl.getAxioms()) {

			/***
			 * Important to use the negated normal form of the axioms, and not
			 * the simple ones.
			 */
			axiom = axiom.getNNF();

			try {
				if (axiom instanceof OWLEquivalentClassesAxiom) 
					processAxiom(dl_onto, (OWLEquivalentClassesAxiom) axiom);
			
				else if (axiom instanceof OWLSubClassOfAxiom) 
					processAxiom(dl_onto, (OWLSubClassOfAxiom) axiom);
				 
				else if (axiom instanceof OWLDataPropertyDomainAxiom) 
					processAxiom(dl_onto, (OWLDataPropertyDomainAxiom) axiom);
					
				else if (axiom instanceof OWLDataPropertyRangeAxiom) 
					processAxiom(dl_onto, (OWLDataPropertyRangeAxiom) axiom);

				else if (axiom instanceof OWLSubDataPropertyOfAxiom) 
					processAxiom(dl_onto, (OWLSubDataPropertyOfAxiom) axiom);
				
				else if (axiom instanceof OWLEquivalentDataPropertiesAxiom) 
					processAxiom(dl_onto, (OWLEquivalentDataPropertiesAxiom) axiom); 

				else if (axiom instanceof OWLEquivalentObjectPropertiesAxiom) 
					processAxiom(dl_onto, (OWLEquivalentObjectPropertiesAxiom) axiom);

				else if (axiom instanceof OWLFunctionalDataPropertyAxiom) 
					processAxiom(dl_onto, (OWLFunctionalDataPropertyAxiom) axiom);

				else if (axiom instanceof OWLInverseObjectPropertiesAxiom) 
					processAxiom(dl_onto, (OWLInverseObjectPropertiesAxiom) axiom);
				
				else if (axiom instanceof OWLSymmetricObjectPropertyAxiom) 
					processAxiom(dl_onto, (OWLSymmetricObjectPropertyAxiom) axiom);
				
				else if (axiom instanceof OWLObjectPropertyDomainAxiom) 
					processAxiom(dl_onto, (OWLObjectPropertyDomainAxiom) axiom);

				else if (axiom instanceof OWLObjectPropertyRangeAxiom) 
					processAxiom(dl_onto, (OWLObjectPropertyRangeAxiom) axiom);

				else if (axiom instanceof OWLSubObjectPropertyOfAxiom) 
					processAxiom(dl_onto, (OWLSubObjectPropertyOfAxiom) axiom); 
				
				else if (axiom instanceof OWLFunctionalObjectPropertyAxiom) 
					processAxiom(dl_onto, (OWLFunctionalObjectPropertyAxiom) axiom);

				else if (axiom instanceof OWLInverseFunctionalObjectPropertyAxiom) 
					processAxiom(dl_onto,  (OWLInverseFunctionalObjectPropertyAxiom) axiom); 
				
				else if (axiom instanceof OWLDisjointClassesAxiom) 
					processAxiom(dl_onto, (OWLDisjointClassesAxiom) axiom);
							
				else if (axiom instanceof OWLDisjointDataPropertiesAxiom) 
					processAxiom(dl_onto, (OWLDisjointDataPropertiesAxiom) axiom);
					
				else if (axiom instanceof OWLDisjointObjectPropertiesAxiom) 
					processAxiom(dl_onto, (OWLDisjointObjectPropertiesAxiom) axiom);
					
				else if (axiom instanceof OWLAsymmetricObjectPropertyAxiom) 
					processAxiom(dl_onto, (OWLAsymmetricObjectPropertyAxiom) axiom);
				
				else if (axiom instanceof OWLClassAssertionAxiom)
					processAxiom(dl_onto, (OWLClassAssertionAxiom)axiom);
					
				else if (axiom instanceof OWLObjectPropertyAssertionAxiom) 
					processAxiom(dl_onto, (OWLObjectPropertyAssertionAxiom)axiom);
					
				else if (axiom instanceof OWLDataPropertyAssertionAxiom) 
					processAxiom(dl_onto, (OWLDataPropertyAssertionAxiom)axiom);
					
				else if (axiom instanceof OWLAnnotationAxiom) {
					// NO-OP: annotation axioms are intentionally ignored by the translator
				} 
				else if (axiom instanceof OWLDeclarationAxiom) {
					// NO-OP: declaration axioms are ignored at this stage 
					// (all entities have been processed)
				}
				else {
					log.warn("Axiom not yet supported by Quest: {}", axiom.toString());
				}
			} catch (TranslationException e) {
				log.warn("Axiom not yet supported by Quest: {}", axiom.toString());
			}
		}
		return dl_onto;
	}
	
	

	private void processEntities(OntologyVocabulary vocabulary, OWLOntology owl) {
		
		Set<String> objectproperties = new HashSet<String>();
		Set<String> dataproperties = new HashSet<String>();
		Set<String> punnedPredicates = new HashSet<String>();
		
		/*
		 * First we add all definitions for classes and roles
		 */
					
		for (OWLClass entity : owl.getClassesInSignature()) {
			/* We ignore TOP and BOTTOM (Thing and Nothing) */
			//if (entity.isOWLThing() || entity.isOWLNothing()) 
			//	continue;				
			String uri = entity.getIRI().toString();
			vocabulary.declareClass(uri);
		}

		/*
		 * When we register predicates punning is not allowed between data
		 * and object properties
		 */
		for (OWLObjectProperty prop : owl.getObjectPropertiesInSignature()) {
			//if (prop.isOWLTopObjectProperty() || prop.isOWLBottomObjectProperty()) 
			//	continue;
			String uri = prop.getIRI().toString();
			if (dataproperties.contains(uri)) 
				punnedPredicates.add(uri); 
			else {
				objectproperties.add(uri);
				vocabulary.declareObjectProperty(uri);
			}
		} 
		for (OWLDataProperty prop : owl.getDataPropertiesInSignature()) {
			//if (prop.isOWLTopDataProperty() || prop.isOWLBottomDataProperty()) 
			//	continue;
			String uri = prop.getIRI().toString();
			if (objectproperties.contains(uri)) 
				punnedPredicates.add(uri);
			else {
				dataproperties.add(uri);
				vocabulary.declareDataProperty(uri);
			}
		}
		

		/*
		 * Generating a WARNING about all punned predicates which have been
		 * ignored
		 */
		if (!punnedPredicates.isEmpty()) {
			log.warn("Quest can become unstable with properties declared as both, data and object property. Offending properties: ");
			for (String predicates : punnedPredicates) 
				log.warn("  " + predicates);
		}		
	}
	
	
	private void processAxiom(Ontology dl_onto, OWLSubClassOfAxiom aux) throws TranslationException {
		ClassExpression subDescription = getSubclassExpression(aux.getSubClass());
		List<BasicClassDescription> superDescriptions = getSuperclassExpressions(aux.getSuperClass(), dl_onto);

		for (BasicClassDescription superDescription : superDescriptions) 
			addSubclassAxiom(dl_onto, subDescription, superDescription);		
	}
		
	private void processAxiom(Ontology dl_onto, OWLEquivalentClassesAxiom aux) throws TranslationException {
		if (profile.order() < LanguageProfile.OWL2QL.order())
			throw new TranslationException();

		Set<OWLClassExpression> equivalents = aux.getClassExpressions();
		
		List<ClassExpression> vec = new LinkedList<ClassExpression>();
		for (OWLClassExpression OWLClassExpression : equivalents) 
			vec.add(getSubclassExpression(OWLClassExpression));

		for (int i = 0; i < vec.size(); i++) {
			for (int j = i + 1; j < vec.size(); j++) {
				ClassExpression subclass = vec.get(i);
				ClassExpression superclass = vec.get(j);
				SubClassOfAxiom inclusion1 = ofac.createSubClassAxiom(subclass, superclass);
				dl_onto.add(inclusion1);
				SubClassOfAxiom inclusion2 = ofac.createSubClassAxiom(superclass, subclass);
				dl_onto.add(inclusion2);
			}
		}
	}
	
	private void processAxiom(Ontology dl_onto, OWLDataPropertyDomainAxiom aux) throws TranslationException {
		PropertyExpression role = getRoleExpression(aux.getProperty());

		SomeValuesFrom subclass = ofac.createPropertySomeRestriction(role);
		List<BasicClassDescription> superDescriptions = getSuperclassExpressions(aux.getDomain(), dl_onto);

		for (BasicClassDescription superDescription : superDescriptions) 
			addSubclassAxiom(dl_onto, subclass, superDescription);
	}
	
	private void processAxiom(Ontology dl_onto, OWLObjectPropertyDomainAxiom aux) throws TranslationException {
		PropertyExpression role = getRoleExpression(aux.getProperty());

		SomeValuesFrom subclass = ofac.createPropertySomeRestriction(role);
		List<BasicClassDescription> superDescriptions = getSuperclassExpressions(aux.getDomain(), dl_onto);

		for (BasicClassDescription superDescription : superDescriptions) 
			addSubclassAxiom(dl_onto, subclass, superDescription);
	}
	
	private void processAxiom(Ontology dl_onto, OWLObjectPropertyRangeAxiom aux) throws TranslationException {
		ObjectPropertyExpression role = getRoleExpression(aux.getProperty());
		ObjectPropertyExpression inv = role.getInverse();
		
		SomeValuesFrom subclass = ofac.createPropertySomeRestriction(inv);
		List<BasicClassDescription> superDescriptions = getSuperclassExpressions(aux.getRange(), dl_onto);

		for (BasicClassDescription superDescription : superDescriptions) 
			addSubclassAxiom(dl_onto, subclass, superDescription);
	}
	
	
	
	private void processAxiom(Ontology dl_onto, OWLSubObjectPropertyOfAxiom aux) throws TranslationException {
		
		ObjectPropertyExpression subrole = getRoleExpression(aux.getSubProperty());
		ObjectPropertyExpression superrole = getRoleExpression(aux.getSuperProperty());

		SubPropertyOfAxiom roleinc = ofac.createSubPropertyAxiom(subrole, superrole);
		dl_onto.add(roleinc);	
	}

	private void processAxiom(Ontology dl_onto, OWLSubDataPropertyOfAxiom aux) throws TranslationException {
		
		DataPropertyExpression subrole = getRoleExpression(aux.getSubProperty());
		DataPropertyExpression superrole = getRoleExpression(aux.getSuperProperty());

		SubPropertyOfAxiom roleinc = ofac.createSubPropertyAxiom(subrole, superrole);
		dl_onto.add(roleinc);	
	}
	
	private void processAxiom(Ontology dl_onto, OWLEquivalentObjectPropertiesAxiom aux) throws TranslationException {
		if (profile.order() < LanguageProfile.OWL2QL.order())
			throw new TranslationException();

		List<ObjectPropertyExpression> result = new LinkedList<ObjectPropertyExpression>();
		for (OWLObjectPropertyExpression rolExpression : aux.getProperties()) 
			result.add(getRoleExpression(rolExpression));
		
		for (int i = 0; i < result.size(); i++) {
			for (int j = i + 1; j < result.size(); j++) {
				ObjectPropertyExpression subrole = result.get(i);
				ObjectPropertyExpression superole = result.get(j);
				SubPropertyOfAxiom inclusion1 = ofac.createSubPropertyAxiom(subrole, superole);
				dl_onto.add(inclusion1);
				SubPropertyOfAxiom inclusion2 = ofac.createSubPropertyAxiom(superole, subrole);
				dl_onto.add(inclusion2);
			}
		}
	}
	
	private void processAxiom(Ontology dl_onto, OWLEquivalentDataPropertiesAxiom aux) throws TranslationException {
		if (profile.order() < LanguageProfile.OWL2QL.order())
			throw new TranslationException();

		List<DataPropertyExpression> result = new LinkedList<DataPropertyExpression>();
		for (OWLDataPropertyExpression rolExpression : aux.getProperties()) 
			result.add(getRoleExpression(rolExpression));
		
		for (int i = 0; i < result.size(); i++) {
			for (int j = i + 1; j < result.size(); j++) {
				DataPropertyExpression subrole = result.get(i);
				DataPropertyExpression superole = result.get(j);
				SubPropertyOfAxiom inclusion1 = ofac.createSubPropertyAxiom(subrole, superole);
				dl_onto.add(inclusion1);
				SubPropertyOfAxiom inclusion2 = ofac.createSubPropertyAxiom(superole, subrole);
				dl_onto.add(inclusion2);
			}
		}
	}
	
	private void processAxiom(Ontology dl_onto, OWLInverseObjectPropertiesAxiom aux) throws TranslationException {
		if (profile.order() < LanguageProfile.OWL2QL.order())
			throw new TranslationException();

		ObjectPropertyExpression role1 = getRoleExpression(aux.getFirstProperty());
		ObjectPropertyExpression role2 = getRoleExpression(aux.getSecondProperty());

		SubPropertyOfAxiom inc1 = ofac.createSubPropertyAxiom(role1, role2.getInverse());
		dl_onto.add(inc1);

		SubPropertyOfAxiom inc2 = ofac.createSubPropertyAxiom(role2, role1.getInverse());
		dl_onto.add(inc2);		
	}
	
	private void processAxiom(Ontology dl_onto, OWLDisjointClassesAxiom aux) throws TranslationException {
		Set<ClassExpression> disjointClasses = new HashSet<ClassExpression>();
		for (OWLClassExpression oc : aux.getClassExpressionsAsList()) {
			BasicClassDescription c = getSubclassExpression(oc);
			disjointClasses.add((ClassExpression) c);
		}
		
		DisjointClassesAxiom disj = ofac.createDisjointClassesAxiom(disjointClasses);				
		dl_onto.add(disj);
	}

	private void processAxiom(Ontology dl_onto, OWLDisjointDataPropertiesAxiom aux) throws TranslationException {
		Set<PropertyExpression> disjointProperties = new HashSet<PropertyExpression>();
		for (OWLDataPropertyExpression prop : aux.getProperties()) {
			PropertyExpression p = getRoleExpression(prop);
			disjointProperties.add(p);
		}
		DisjointPropertiesAxiom disj = ofac.createDisjointPropertiesAxiom(disjointProperties);
		dl_onto.add(disj);		
	}
	
	private void processAxiom(Ontology dl_onto, OWLDisjointObjectPropertiesAxiom aux) throws TranslationException {
		Set<PropertyExpression> disjointProperties = new HashSet<PropertyExpression>();
		for (OWLObjectPropertyExpression prop : aux.getProperties()) {
			PropertyExpression p = getRoleExpression(prop);
			disjointProperties.add(p);
		}
		DisjointPropertiesAxiom disj = ofac.createDisjointPropertiesAxiom(disjointProperties);				
		dl_onto.add(disj);		
	}
	
	private void processAxiom(Ontology dl_onto, OWLSymmetricObjectPropertyAxiom aux) throws TranslationException {
		if (profile.order() < LanguageProfile.OWL2QL.order())
			throw new TranslationException();
		
		OWLObjectPropertyExpression exp1 = aux.getProperty();
		ObjectPropertyExpression role = getRoleExpression(exp1);
		ObjectPropertyExpression invrole = role.getInverse();

		SubPropertyOfAxiom symm = ofac.createSubPropertyAxiom(invrole, role);

		dl_onto.add(symm);
	}
	
	private void processAxiom(Ontology dl_onto, OWLAsymmetricObjectPropertyAxiom aux) throws TranslationException {
		Set<PropertyExpression> disjointProperties = new HashSet<PropertyExpression>();
		OWLObjectPropertyExpression prop = aux.getProperty(); 
		ObjectPropertyExpression p = getRoleExpression(prop);
		disjointProperties.add(p);
		disjointProperties.add(p.getInverse());
		
		DisjointPropertiesAxiom disj = ofac.createDisjointPropertiesAxiom(disjointProperties);				
		dl_onto.add(disj);		
	}
	
	
	
	private void processAxiom(Ontology dl_onto, OWLDataPropertyRangeAxiom aux) throws TranslationException {

		DataPropertyExpression role = getRoleExpression(aux.getProperty());

		DataPropertyRangeExpression subclass = ofac.createDataPropertyRange(role);

		if (aux.getRange().isDatatype()) {
			OWLDatatype rangeDatatype = aux.getRange().asOWLDatatype();

			if (rangeDatatype.isBuiltIn()) {

				Predicate.COL_TYPE columnType = getColumnType(rangeDatatype);
				Datatype datatype = ofac.createDataType(dfac.getTypePredicate(columnType));
				SubClassOfAxiom inc = ofac.createSubClassAxiom(subclass, datatype);
				dl_onto.add(inc);
			} else {
				log.warn("Ignoring range axiom since it refers to a non-supported datatype: " + aux);
			}
		} else {
			log.warn("Ignoring range axiom since it is not a datatype: " + aux);
		}
		
	}

	
	
	
	
	private void processAxiom(Ontology dl_onto, OWLFunctionalObjectPropertyAxiom aux) throws TranslationException {
		if (profile.order() < LanguageProfile.OWL2QL.order())
			throw new TranslationException();
		PropertyExpression role = getRoleExpression(aux.getProperty());
		
		FunctionalPropertyAxiom funct = ofac.createPropertyFunctionalAxiom(role);
		dl_onto.add(funct);				
	}	
	
	private void processAxiom(Ontology dl_onto, OWLFunctionalDataPropertyAxiom aux) throws TranslationException {
		if (profile.order() < LanguageProfile.DLLITEA.order())
			throw new TranslationException();
		PropertyExpression role = getRoleExpression(aux.getProperty());
		FunctionalPropertyAxiom funct = ofac.createPropertyFunctionalAxiom(role);

		dl_onto.add(funct);		
	}
	
	private void processAxiom(Ontology dl_onto, OWLInverseFunctionalObjectPropertyAxiom aux) throws TranslationException {
		if (profile.order() < LanguageProfile.OWL2QL.order())
			throw new TranslationException();
		ObjectPropertyExpression role = getRoleExpression(aux.getProperty());
		ObjectPropertyExpression invrole = role.getInverse();
		FunctionalPropertyAxiom funct = ofac.createPropertyFunctionalAxiom(invrole);

		dl_onto.add(funct);
	}
	
	
	
	
	
	
	private void addSubclassAxiom(Ontology dl_onto, ClassExpression subDescription, BasicClassDescription superDescription) {
		if (superDescription == null || subDescription == null) {
			log.warn("NULL: {} {}", subDescription, superDescription);
		}

		/* We ignore TOP and BOTTOM (Thing and Nothing) */
		if (superDescription instanceof OClass) {
			OClass classDescription = (OClass) superDescription;
			if (classDescription.toString().equals("http://www.w3.org/2002/07/owl#Thing")) {
				return;
			}
		}
		SubClassOfAxiom inc = ofac.createSubClassAxiom(subDescription, superDescription);
		dl_onto.add(inc);
	}

	
	


	private static ObjectPropertyExpression getRoleExpression(OWLObjectPropertyExpression rolExpression) throws TranslationException {
		ObjectPropertyExpression role = null;

		if (rolExpression instanceof OWLObjectProperty) {
			role = ofac.createObjectProperty(rolExpression.asOWLObjectProperty().getIRI().toString());
		} else if (rolExpression instanceof OWLObjectInverseOf) {
			if (profile.order() < LanguageProfile.OWL2QL.order())
				throw new TranslationException();
			OWLObjectInverseOf aux = (OWLObjectInverseOf) rolExpression;
			role = ofac.createObjectProperty(aux.getInverse().asOWLObjectProperty().getIRI().toString()).getInverse();
		} else {
			throw new TranslationException();
		}
		return role;

	}

	private DataPropertyExpression getRoleExpression(OWLDataPropertyExpression rolExpression) throws TranslationException {
		DataPropertyExpression role = null;

		if (rolExpression instanceof OWLDataProperty) {
			role = ofac.createDataProperty((rolExpression.asOWLDataProperty().getIRI().toString()));
		} else {
			throw new TranslationException();
		}
		return role;

	}
	
	

	private List<BasicClassDescription> getSuperclassExpressions(OWLClassExpression owlExpression, Ontology dl_onto) throws TranslationException {
		List<BasicClassDescription> result = new LinkedList<BasicClassDescription>();
		if (owlExpression instanceof OWLObjectIntersectionOf) {
			if (profile.order() < LanguageProfile.OWL2QL.order()) {
				throw new TranslationException();
			}
			OWLObjectIntersectionOf intersection = (OWLObjectIntersectionOf) owlExpression;
			Set<OWLClassExpression> operands = intersection.getOperands();
			for (OWLClassExpression operand : operands) {
				result.addAll(getSuperclassExpressions(operand, dl_onto));
			}
		} else if (owlExpression instanceof OWLObjectSomeValuesFrom) {
			if (profile.order() < LanguageProfile.OWL2QL.order()) {
				throw new TranslationException();
			}
			OWLObjectSomeValuesFrom someexp = (OWLObjectSomeValuesFrom) owlExpression;
			OWLClassExpression filler = someexp.getFiller();
			if (!(filler instanceof OWLClass)) {
				throw new TranslationException();
			}
			if (filler.isOWLThing()) {
				BasicClassDescription cd = getSubclassExpression(owlExpression);
				result.add(cd);
			} else {
				BasicClassDescription cd = getPropertySomeClassRestriction(someexp, dl_onto);
				result.add(cd);
			}
		} else if (owlExpression instanceof OWLDataSomeValuesFrom) {
			if (profile.order() < LanguageProfile.OWL2QL.order()) {
				throw new TranslationException();
			}
			OWLDataSomeValuesFrom someexp = (OWLDataSomeValuesFrom) owlExpression;
			OWLDataRange filler = someexp.getFiller();

			if (filler.isTopDatatype()) {
				OWLDataPropertyExpression property = someexp.getProperty();
				PropertyExpression role = getRoleExpression(property);
				BasicClassDescription cd = ofac.createPropertySomeRestriction(role);
				result.add(cd);
			} else if (filler instanceof OWLDatatype) {
				BasicClassDescription cd = this.getPropertySomeDatatypeRestriction(someexp, dl_onto);
				result.add(cd);
			}
		} else {
			result.add(getSubclassExpression(owlExpression));
		}
		return result;
	}

	
	
	
	private BasicClassDescription getPropertySomeClassRestriction(OWLObjectSomeValuesFrom someexp, Ontology dl_onto) throws TranslationException {
		
		SomeValuesFrom auxclass = auxiliaryClassProperties.get(someexp);
		if (auxclass == null) {
			/*
			 * no auxiliary subclass assertions found for this exists R.A,
			 * creating a new one
			 */
			
			OWLObjectPropertyExpression owlProperty = someexp.getProperty();
			OWLClassExpression owlFiller = someexp.getFiller();
			
			ObjectPropertyExpression role = getRoleExpression(owlProperty);
			BasicClassDescription filler = getSubclassExpression(owlFiller);

			PropertyExpression auxRole = ofac.createObjectProperty(OntologyVocabularyImpl.AUXROLEURI + auxRoleCounter);
			auxRoleCounter += 1;

			// if \exists R.C then exp = P, auxclass = \exists P, P <= R, \exists P^- <= C
			// if \exists R^-.C then exp = P^-, auxclass = \exists P^-, P <= R, \exists P <= C
			
			ObjectPropertyExpression exp = ofac.createObjectProperty(auxRole.getPredicate().getName());
			if (role.isInverse())
				exp = exp.getInverse();
			
			auxclass = ofac.createPropertySomeRestriction(exp);
			auxiliaryClassProperties.put(someexp, auxclass);

			/* Creating the new subrole assertions */
			SubPropertyOfAxiom subrole = ofac.createSubPropertyAxiom(exp, role);
			dl_onto.add(subrole);
			
			/* Creating the range assertion */
			PropertyExpression expInv = exp.getInverse();
			SomeValuesFrom propertySomeRestrictionInv = ofac.createPropertySomeRestriction(expInv);
			SubClassOfAxiom subclass = ofac.createSubClassAxiom(propertySomeRestrictionInv, filler);
			dl_onto.add(subclass);
		}

		return auxclass;
	}

	private BasicClassDescription getPropertySomeDatatypeRestriction(OWLDataSomeValuesFrom someexp, Ontology dl_onto) throws TranslationException {
		
		SomeValuesFrom auxclass = auxiliaryDatatypeProperties.get(someexp);
		if (auxclass == null) {
			/*
			 * no auxiliary subclass assertions found for this exists R.A,
			 * creating a new one
			 */
			
			OWLDataPropertyExpression owlProperty = someexp.getProperty();
			DataPropertyExpression role = getRoleExpression(owlProperty);

			// TODO: handle more complex fillers
			OWLDatatype owlDatatype = (OWLDatatype) someexp.getFiller();
			COL_TYPE datatype = getColumnType(owlDatatype);
			Datatype filler = ofac.createDataType(dfac.getTypePredicate(datatype));
			
			DataPropertyExpression auxRole = ofac.createDataProperty(OntologyVocabularyImpl.AUXROLEURI + auxRoleCounter);
			auxRoleCounter += 1;

			DataPropertyExpression exp = ofac.createDataProperty(auxRole.getPredicate().getName());
			auxclass = ofac.createPropertySomeRestriction(exp);
			auxiliaryDatatypeProperties.put(someexp, auxclass);

			/* Creating the new subrole assertions */
			SubPropertyOfAxiom subrole = ofac.createSubPropertyAxiom(auxRole, role);
			dl_onto.add(subrole);
			
			/* Creating the range assertion */
			DataPropertyRangeExpression propertySomeRestrictionInv = ofac.createDataPropertyRange(exp);
			SubClassOfAxiom subclass = ofac.createSubClassAxiom(propertySomeRestrictionInv, filler);
			dl_onto.add(subclass);
		}

		return auxclass;
	}
	
	private static ClassExpression getSubclassExpression(OWLClassExpression owlExpression) throws TranslationException {
		ClassExpression cd = null;
		if (owlExpression instanceof OWLClass) {
			String uri = ((OWLClass) owlExpression).getIRI().toString();
			cd = ofac.createClass(uri);

		} else if (owlExpression instanceof OWLDataMinCardinality) {
			if (profile.order() < LanguageProfile.DLLITEA.order())
				throw new TranslationException();
			OWLDataMinCardinality rest = (OWLDataMinCardinality) owlExpression;
			int cardinatlity = rest.getCardinality();
			OWLDataRange range = rest.getFiller();
			if (cardinatlity != 1 || range != null) {
				throw new TranslationException();
			}
			String uri = (rest.getProperty().asOWLDataProperty().getIRI().toString());
			PropertyExpression prop = ofac.createDataProperty(uri);
			cd = ofac.createPropertySomeRestriction(prop);

		} else if (owlExpression instanceof OWLObjectMinCardinality) {
			if (profile.order() < LanguageProfile.DLLITEA.order())
				throw new TranslationException();
			OWLObjectMinCardinality rest = (OWLObjectMinCardinality) owlExpression;
			int cardinatlity = rest.getCardinality();
			OWLClassExpression filler = rest.getFiller();
			if (cardinatlity != 1) {
				throw new TranslationException();
			}

			if (!filler.isOWLThing()) {
				throw new TranslationException();
			}
			OWLObjectPropertyExpression propExp = rest.getProperty();
			String uri = propExp.getNamedProperty().getIRI().toString();

			PropertyExpression prop;
			
			if (propExp instanceof OWLObjectInverseOf) {
				prop = ofac.createObjectProperty(uri).getInverse();
			} else {
				prop = ofac.createObjectProperty(uri);
			}
			cd = ofac.createPropertySomeRestriction(prop);

		} else if (owlExpression instanceof OWLObjectSomeValuesFrom) {
			if (profile.order() < LanguageProfile.OWL2QL.order())
				throw new TranslationException();
			OWLObjectSomeValuesFrom rest = (OWLObjectSomeValuesFrom) owlExpression;
			OWLClassExpression filler = rest.getFiller();

			if (!filler.isOWLThing()) {
				throw new TranslationException();
			}
			OWLObjectPropertyExpression propExp = rest.getProperty();
			String uri = propExp.getNamedProperty().getIRI().toString();

			PropertyExpression prop;
			
			if (propExp instanceof OWLObjectInverseOf) {
				prop = ofac.createObjectProperty(uri).getInverse();
			} else {
				prop = ofac.createObjectProperty(uri);
			}
			cd = ofac.createPropertySomeRestriction(prop);
			
		} else if (owlExpression instanceof OWLDataSomeValuesFrom) {
			if (profile.order() < LanguageProfile.OWL2QL.order())
				throw new TranslationException();
			OWLDataSomeValuesFrom rest = (OWLDataSomeValuesFrom) owlExpression;
			OWLDataRange filler = rest.getFiller();

			if (!filler.isTopDatatype()) {
				throw new TranslationException();
			}
			OWLDataProperty propExp = (OWLDataProperty) rest.getProperty();
			String uri = propExp.getIRI().toString();
			PropertyExpression prop = ofac.createDataProperty(uri);
			cd = ofac.createPropertySomeRestriction(prop);
		}

		if (cd == null) {
			throw new TranslationException();
		}
		return cd;
	}

	
	
	
	
	private static class TranslationException extends Exception {

		/**
		 * 
		 */
		private static final long serialVersionUID = 7917688953760608030L;

		public TranslationException() {
			super();
		}

		public TranslationException(String msg) {
			super(msg);
		}

	}


	/***
	 * This will translate an OWLABox assertion into our own ABox assertions.
	 * 
	 * @param axiom
	 * @return
	 */
	private void processAxiom(Ontology dl_onto, OWLClassAssertionAxiom aux) {
		ClassAssertion a = translate(aux);
		if (a != null)
			dl_onto.add(a);
	}

	private void processAxiom(Ontology dl_onto, OWLObjectPropertyAssertionAxiom aux) {
		PropertyAssertion assertion = translate(aux);
		dl_onto.add(assertion);
	}

	private void processAxiom(Ontology dl_onto, OWLDataPropertyAssertionAxiom aux) {
		PropertyAssertion assertion = translate(aux);
		dl_onto.add(assertion);
	}
	
	
	
	public static ClassAssertion translate(OWLClassAssertionAxiom aux) {

		OWLClassExpression classExpression = aux.getClassExpression();
		if (!(classExpression instanceof OWLClass))
			throw new RuntimeException("Found complex class in assertion, this feature is not supported");
		
		if (classExpression.isOWLThing())
			return null;
		
		if (classExpression.isOWLNothing())
			throw new RuntimeException("Unsatisfiable class assertion: " + aux);

		OWLClass namedclass = (OWLClass) classExpression;
		OWLIndividual indv = aux.getIndividual();

		if (indv.isAnonymous()) 
			throw new RuntimeException("Found anonymous individual, this feature is not supported:" + aux);

		OClass concept = ofac.createClass(namedclass.getIRI().toString());
		
		URIConstant c = dfac.getConstantURI(indv.asOWLNamedIndividual().getIRI().toString());

		ClassAssertion assertion = ofac.createClassAssertion(concept, c);
		return assertion;
	}
	
	public static PropertyAssertion translate(OWLObjectPropertyAssertionAxiom aux) {
			
		if (aux.getSubject().isAnonymous() || aux.getObject().isAnonymous()) 
			throw new RuntimeException("Found anonymous individual, this feature is not supported");			

		OWLObjectPropertyExpression propertyExperssion = aux.getProperty();
					
		PropertyExpression prop;
		URIConstant c1, c2;

		if (propertyExperssion instanceof OWLObjectProperty) {
			OWLObjectProperty namedclass = (OWLObjectProperty) propertyExperssion;
			prop = ofac.createObjectProperty(namedclass.getIRI().toString());
			
			c1 = dfac.getConstantURI(aux.getSubject().asOWLNamedIndividual().getIRI().toString());
			c2 = dfac.getConstantURI(aux.getObject().asOWLNamedIndividual().getIRI().toString());
			
			// TODO: check for bottom
			
		} 
		else if (propertyExperssion instanceof OWLObjectInverseOf) {
			OWLObjectProperty namedclass = ((OWLObjectInverseOf) propertyExperssion).getInverse().getNamedProperty();
			prop = ofac.createObjectProperty(namedclass.getIRI().toString()).getInverse();
			
			c1 = dfac.getConstantURI(aux.getSubject().asOWLNamedIndividual().getIRI().toString());
			c2 = dfac.getConstantURI(aux.getObject().asOWLNamedIndividual().getIRI().toString());

			// TODO: check for bottom			
		}
		else
			throw new RuntimeException("Found complex property expression in an asserion, this feature is not supported");

		PropertyAssertion assertion = ofac.createPropertyAssertion(prop, c1, c2);
		return assertion;
	}
	
	public static PropertyAssertion translate(OWLDataPropertyAssertionAxiom aux) {
		
		OWLDataProperty propertyExperssion = (OWLDataProperty) aux.getProperty();

		String property = propertyExperssion.getIRI().toString();
		OWLIndividual subject = aux.getSubject();
		OWLLiteral object = aux.getObject();

		if (subject.isAnonymous()) 
			throw new RuntimeException("Found anonymous individual, this feature is not supported");

		// TODO: CHECK FOR BOT AND TOP
		
		Predicate.COL_TYPE type;
		try {
			type = getColumnType(object.getDatatype());
		} catch (TranslationException e) {
			throw new RuntimeException(e.getMessage());
		}

		PropertyExpression prop = ofac.createDataProperty(property);
		
		URIConstant c1 = dfac.getConstantURI(subject.asOWLNamedIndividual().getIRI().toString());
		ValueConstant c2 = dfac.getConstantLiteral(object.getLiteral(), type);

		return ofac.createPropertyAssertion(prop, c1, c2);
	}

	private static Predicate.COL_TYPE getColumnType(OWLDatatype datatype) throws TranslationException {
		if (datatype == null) {
			return COL_TYPE.LITERAL;
		}
		if (datatype.isString() || datatype.getBuiltInDatatype() == OWL2Datatype.XSD_STRING) { // xsd:string
			return COL_TYPE.STRING;
		} else if (datatype.isRDFPlainLiteral() || datatype.getBuiltInDatatype() == OWL2Datatype.RDF_PLAIN_LITERAL // rdf:PlainLiteral
				|| datatype.getBuiltInDatatype() == OWL2Datatype.RDFS_LITERAL) { // rdfs:Literal
			return COL_TYPE.LITERAL;
		} else if (datatype.isInteger()
				|| datatype.getBuiltInDatatype() == OWL2Datatype.XSD_INTEGER) {
            return COL_TYPE.INTEGER;
        } else if ( datatype.getBuiltInDatatype() == OWL2Datatype.XSD_NON_NEGATIVE_INTEGER) {
            return COL_TYPE.NON_NEGATIVE_INTEGER;
        } else if (datatype.getBuiltInDatatype() == OWL2Datatype.XSD_INT) { // xsd:int
            System.err.println(datatype.getBuiltInDatatype() + " is not in OWL2QL profile");
            return COL_TYPE.INT;
        } else if  (datatype.getBuiltInDatatype() == OWL2Datatype.XSD_POSITIVE_INTEGER){
            System.err.println(datatype.getBuiltInDatatype() + " is not in OWL2QL profile");
            return COL_TYPE.POSITIVE_INTEGER;
        } else if  (datatype.getBuiltInDatatype() == OWL2Datatype.XSD_NEGATIVE_INTEGER) {
            System.err.println(datatype.getBuiltInDatatype() + " is not in OWL2QL profile");
            return COL_TYPE.NEGATIVE_INTEGER;
        } else if  (datatype.getBuiltInDatatype() == OWL2Datatype.XSD_NON_POSITIVE_INTEGER){
            System.err.println(datatype.getBuiltInDatatype() + " is not in OWL2QL profile");
            return COL_TYPE.NON_POSITIVE_INTEGER;
        } else if  (datatype.getBuiltInDatatype() == OWL2Datatype.XSD_UNSIGNED_INT) {
            System.err.println(datatype.getBuiltInDatatype() + " is not in OWL2QL profile");
            return COL_TYPE.UNSIGNED_INT;
		} else if (datatype.getBuiltInDatatype() == OWL2Datatype.XSD_DECIMAL) { // xsd:decimal
			return Predicate.COL_TYPE.DECIMAL;
        } else if (datatype.isFloat() || datatype.isDouble() || datatype.getBuiltInDatatype() == OWL2Datatype.XSD_DOUBLE) { // xsd:double
            System.err.println(datatype.getBuiltInDatatype() + " is not in OWL2QL profile");
			return Predicate.COL_TYPE.DOUBLE;
        } else if (datatype.isFloat() || datatype.getBuiltInDatatype() == OWL2Datatype.XSD_FLOAT) { // xsd:float
            System.err.println(datatype.getBuiltInDatatype() + " is not in OWL2QL profile");
            return Predicate.COL_TYPE.FLOAT;
		} else if (datatype.getBuiltInDatatype() == OWL2Datatype.XSD_DATE_TIME || datatype.getBuiltInDatatype() == OWL2Datatype.XSD_DATE_TIME_STAMP ) {
			return Predicate.COL_TYPE.DATETIME;
        } else if (datatype.getBuiltInDatatype() == OWL2Datatype.XSD_LONG) {
            System.err.println(datatype.getBuiltInDatatype() + " is not in OWL2QL profile");
            return Predicate.COL_TYPE.LONG;
		} else if (datatype.isBoolean() || datatype.getBuiltInDatatype() == OWL2Datatype.XSD_BOOLEAN) { // xsd:boolean
            System.err.println(datatype.getBuiltInDatatype() + " is not in OWL2QL profile");
			return Predicate.COL_TYPE.BOOLEAN;
		} else {
			throw new TranslationException("Unsupported data range: " + datatype.toString());
		}
	}

	// public Predicate getDataTypePredicate(Predicate.COL_TYPE type) {
	// switch (type) {
	// case LITERAL:
	// return dfac.getDataTypePredicateLiteral();
	// case STRING:
	// return dfac.getDataTypePredicateString();
	// case INTEGER:
	// return dfac.getDataTypePredicateInteger();
	// case DECIMAL:
	// return dfac.getDataTypePredicateDecimal();
	// case DOUBLE:
	// return dfac.getDataTypePredicateDouble();
	// case DATETIME:
	// return dfac.getDataTypePredicateDateTime();
	// case BOOLEAN:
	// return dfac.getDataTypePredicateBoolean();
	// default:
	// return dfac.getDataTypePredicateLiteral();
	// }
	// }
}
