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
import it.unibz.krdb.obda.ontology.Datatype;
import it.unibz.krdb.obda.ontology.DisjointClassesAxiom;
import it.unibz.krdb.obda.ontology.DisjointPropertiesAxiom;
import it.unibz.krdb.obda.ontology.LanguageProfile;
import it.unibz.krdb.obda.ontology.OClass;
import it.unibz.krdb.obda.ontology.Ontology;
import it.unibz.krdb.obda.ontology.OntologyFactory;
import it.unibz.krdb.obda.ontology.PropertyExpression;
import it.unibz.krdb.obda.ontology.FunctionalPropertyAxiom;
import it.unibz.krdb.obda.ontology.PropertyAssertion;
import it.unibz.krdb.obda.ontology.SomeValuesFrom;
import it.unibz.krdb.obda.ontology.SubClassExpression;
import it.unibz.krdb.obda.ontology.SubClassOfAxiom;
import it.unibz.krdb.obda.ontology.SubPropertyOfAxiom;
import it.unibz.krdb.obda.ontology.impl.OntologyFactoryImpl;
import it.unibz.krdb.obda.ontology.impl.OntologyImpl;
import it.unibz.krdb.obda.ontology.impl.PunningException;

import java.io.BufferedWriter;
import java.io.File;
import java.io.FileWriter;
import java.util.Collection;
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

	private final LanguageProfile profile = LanguageProfile.DLLITEA;

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

	// USED IN OBDAModelManager
	
	public Predicate getPredicate(OWLEntity entity) {
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

	public Ontology translate(OWLOntology owl) throws PunningException {

		OWL2QLProfile owlprofile = new OWL2QLProfile();

		OWLProfileReport report = owlprofile.checkOntology(owl);
		Set<OWLAxiom> axiomIgnoresOWL2QL = new HashSet<OWLAxiom>();

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

		HashSet<String> objectproperties = new HashSet<String>();
		HashSet<String> dataproperties = new HashSet<String>();

		/*
		 * First we add all definitions for classes and roles
		 */
		Set<OWLEntity> entities = owl.getSignature();
		Iterator<OWLEntity> eit = entities.iterator();

		HashSet<String> punnedPredicates = new HashSet<String>();

		while (eit.hasNext()) {
			OWLEntity entity = eit.next();
			Predicate p = getPredicate(entity);
			if (p == null)
				continue;

			/*
			 * When we register predicates punning is not allowed between data
			 * and object properties
			 */

			if (p.isClass()) {
				dl_onto.addConcept(p);

			} else {
				if (p.isObjectProperty()) {
					if (dataproperties.contains(p.getName().toString())) {
						punnedPredicates.add(p.getName().toString());
					} else {
						objectproperties.add(p.getName().toString());
						dl_onto.addRole(p);
					}
				} else {
					if (objectproperties.contains(p.getName().toString())) {
						punnedPredicates.add(p.getName().toString());
					} else {
						dataproperties.add(p.getName().toString());
						dl_onto.addRole(p);
					}
				}
			}
		}

		/*
		 * Generating a WARNING about all punned predicates which have been
		 * ignored
		 */
		if (!punnedPredicates.isEmpty()) {
			log.warn("Quest can become unstable with properties declared as both, data and object property. Offending properties: ");
			for (String predicates : punnedPredicates) {
				log.warn("  " + predicates);
			}
		}

		Set<OWLAxiom> axioms = owl.getAxioms();
		Iterator<OWLAxiom> it = axioms.iterator();
		while (it.hasNext()) {

			OWLAxiom axiom = it.next();

			if (axiomIgnoresOWL2QL.contains(axiom)) {
				/*
				 * This axiom is not part of OWL 2 QL according to the OWLAPI,
				 * we need to ignore it
				 */
				continue;
			}

			/***
			 * Important to use the negated normal form of the axioms, and not
			 * the simple ones.
			 */
			axiom = axiom.getNNF();

			try {
				if (axiom instanceof OWLEquivalentClassesAxiom) {
					if (profile.order() < LanguageProfile.OWL2QL.order())
						throw new TranslationException();

					OWLEquivalentClassesAxiom aux = (OWLEquivalentClassesAxiom) axiom;
					Set<OWLClassExpression> equivalents = aux.getClassExpressions();
					List<SubClassExpression> vec = getSubclassExpressions(equivalents);

					addConceptEquivalences(dl_onto, vec);

				} else if (axiom instanceof OWLSubClassOfAxiom) {

					OWLSubClassOfAxiom aux = (OWLSubClassOfAxiom) axiom;
					SubClassExpression subDescription = getSubclassExpression(aux.getSubClass());
					List<BasicClassDescription> superDescriptions = getSuperclassExpressions(aux.getSuperClass(), dl_onto);

					addSubclassAxioms(dl_onto, subDescription, superDescriptions);

				} else if (axiom instanceof OWLDataPropertyDomainAxiom) {

					OWLDataPropertyDomainAxiom aux = (OWLDataPropertyDomainAxiom) axiom;
					PropertyExpression role = getRoleExpression(aux.getProperty());

					SomeValuesFrom subclass = ofac.createPropertySomeRestriction(role);
					List<BasicClassDescription> superDescriptions = getSuperclassExpressions(aux.getDomain(), dl_onto);

					addSubclassAxioms(dl_onto, subclass, superDescriptions);

				} else if (axiom instanceof OWLDataPropertyRangeAxiom) {

					OWLDataPropertyRangeAxiom aux = (OWLDataPropertyRangeAxiom) axiom;
					PropertyExpression role = getRoleExpression(aux.getProperty());

					SomeValuesFrom subclass = ofac.createDataPropertyRange(role);

					if (aux.getRange().isDatatype()) {
						OWLDatatype rangeDatatype = aux.getRange().asOWLDatatype();

						if (rangeDatatype.isBuiltIn()) {

							Predicate.COL_TYPE columnType = getColumnType(rangeDatatype);
							Datatype datatype = ofac.createDataType(dfac.getTypePredicate(columnType));
							addSubclassAxiom(dl_onto, subclass, datatype);
						} else {
							log.warn("Ignoring range axiom since it refers to a non-supported datatype: " + axiom.toString());
						}
					} else {
						log.warn("Ignoring range axiom since it is not a datatype: " + axiom.toString());
					}

				} else if (axiom instanceof OWLSubDataPropertyOfAxiom) {

					OWLSubDataPropertyOfAxiom aux = (OWLSubDataPropertyOfAxiom) axiom;
					PropertyExpression subrole = getRoleExpression(aux.getSubProperty());
					PropertyExpression superrole = getRoleExpression(aux.getSuperProperty());

					SubPropertyOfAxiom roleinc = ofac.createSubPropertyAxiom(subrole, superrole);
					dl_onto.addAssertionWithCheck(roleinc);

				} else if (axiom instanceof OWLEquivalentDataPropertiesAxiom) {

					if (profile.order() < LanguageProfile.OWL2QL.order())
						throw new TranslationException();

					OWLEquivalentDataPropertiesAxiom aux = (OWLEquivalentDataPropertiesAxiom) axiom;
					List<PropertyExpression> vec = getDataRoleExpressions(aux.getProperties());
					addRoleEquivalences(dl_onto, vec);

				} else if (axiom instanceof OWLEquivalentObjectPropertiesAxiom) {

					if (profile.order() < LanguageProfile.OWL2QL.order())
						throw new TranslationException();

					OWLEquivalentObjectPropertiesAxiom aux = (OWLEquivalentObjectPropertiesAxiom) axiom;
					List<PropertyExpression> vec = getObjectRoleExpressions(aux.getProperties());
					addRoleEquivalences(dl_onto, vec);

				} else if (axiom instanceof OWLFunctionalDataPropertyAxiom) {
					if (profile.order() < LanguageProfile.DLLITEA.order())
						throw new TranslationException();
					OWLFunctionalDataPropertyAxiom aux = (OWLFunctionalDataPropertyAxiom) axiom;
					PropertyExpression role = getRoleExpression(aux.getProperty());
					FunctionalPropertyAxiom funct = ofac.createPropertyFunctionalAxiom(role);

					dl_onto.addAssertionWithCheck(funct);

				} else if (axiom instanceof OWLInverseObjectPropertiesAxiom) {
					if (profile.order() < LanguageProfile.OWL2QL.order())
						throw new TranslationException();

					OWLInverseObjectPropertiesAxiom aux = (OWLInverseObjectPropertiesAxiom) axiom;
					OWLObjectPropertyExpression exp1 = aux.getFirstProperty();
					OWLObjectPropertyExpression exp2 = aux.getSecondProperty();
					PropertyExpression role1 = getRoleExpression(exp1);
					PropertyExpression role2 = getRoleExpression(exp2);

					PropertyExpression invrole1 = role1.getInverse();
					PropertyExpression invrole2 = role2.getInverse();

					SubPropertyOfAxiom inc1 = ofac.createSubPropertyAxiom(role1, invrole2);
					SubPropertyOfAxiom inc2 = ofac.createSubPropertyAxiom(role2, invrole1);

					dl_onto.addAssertionWithCheck(inc1);
					dl_onto.addAssertionWithCheck(inc2);

				} else if (axiom instanceof OWLSymmetricObjectPropertyAxiom) {
					if (profile.order() < LanguageProfile.OWL2QL.order())
						throw new TranslationException();
					OWLSymmetricObjectPropertyAxiom aux = (OWLSymmetricObjectPropertyAxiom) axiom;
					OWLObjectPropertyExpression exp1 = aux.getProperty();
					PropertyExpression role = getRoleExpression(exp1);
					PropertyExpression invrole = role.getInverse();

					SubPropertyOfAxiom symm = ofac.createSubPropertyAxiom(invrole, role);

					dl_onto.addAssertionWithCheck(symm);

				} else if (axiom instanceof OWLObjectPropertyDomainAxiom) {

					OWLObjectPropertyDomainAxiom aux = (OWLObjectPropertyDomainAxiom) axiom;
					PropertyExpression role = getRoleExpression(aux.getProperty());

					SomeValuesFrom subclass = ofac.createPropertySomeRestriction(role);
					List<BasicClassDescription> superDescriptions = getSuperclassExpressions(aux.getDomain(), dl_onto);

					addSubclassAxioms(dl_onto, subclass, superDescriptions);

				} else if (axiom instanceof OWLObjectPropertyRangeAxiom) {

					OWLObjectPropertyRangeAxiom aux = (OWLObjectPropertyRangeAxiom) axiom;
					PropertyExpression role = getRoleExpression(aux.getProperty());
					PropertyExpression inv = role.getInverse();
					
					SomeValuesFrom subclass = ofac.createPropertySomeRestriction(inv);
					List<BasicClassDescription> superDescriptions = getSuperclassExpressions(aux.getRange(), dl_onto);

					addSubclassAxioms(dl_onto, subclass, superDescriptions);

				} else if (axiom instanceof OWLSubObjectPropertyOfAxiom) {

					OWLSubObjectPropertyOfAxiom aux = (OWLSubObjectPropertyOfAxiom) axiom;
					PropertyExpression subrole = getRoleExpression(aux.getSubProperty());
					PropertyExpression superrole = getRoleExpression(aux.getSuperProperty());

					SubPropertyOfAxiom roleinc = ofac.createSubPropertyAxiom(subrole, superrole);

					dl_onto.addAssertionWithCheck(roleinc);

				} else if (axiom instanceof OWLFunctionalObjectPropertyAxiom) {
					if (profile.order() < LanguageProfile.OWL2QL.order())
						throw new TranslationException();
					OWLFunctionalObjectPropertyAxiom aux = (OWLFunctionalObjectPropertyAxiom) axiom;
					PropertyExpression role = getRoleExpression(aux.getProperty());
					FunctionalPropertyAxiom funct = ofac.createPropertyFunctionalAxiom(role);

					dl_onto.addAssertionWithCheck(funct);
					
				} else if (axiom instanceof OWLInverseFunctionalObjectPropertyAxiom) {
					if (profile.order() < LanguageProfile.OWL2QL.order())
						throw new TranslationException();
					OWLInverseFunctionalObjectPropertyAxiom aux = (OWLInverseFunctionalObjectPropertyAxiom) axiom;
					PropertyExpression role = getRoleExpression(aux.getProperty());
					PropertyExpression invrole = role.getInverse();
					FunctionalPropertyAxiom funct = ofac.createPropertyFunctionalAxiom(invrole);

					dl_onto.addAssertionWithCheck(funct);

				} else if (axiom instanceof OWLDisjointClassesAxiom) {
					processAxiom(dl_onto, (OWLDisjointClassesAxiom) axiom);
							
				} else if (axiom instanceof OWLDisjointDataPropertiesAxiom) {
					processAxiom(dl_onto, (OWLDisjointDataPropertiesAxiom) axiom);
					
				} else if (axiom instanceof OWLDisjointObjectPropertiesAxiom) {
					processAxiom(dl_onto, (OWLDisjointObjectPropertiesAxiom) axiom);
					
				} else if (axiom instanceof OWLAsymmetricObjectPropertyAxiom) {
					processAxiom(dl_onto, (OWLAsymmetricObjectPropertyAxiom) axiom);
				
				} else if (axiom instanceof OWLClassAssertionAxiom) {
					processAxiom(dl_onto, (OWLClassAssertionAxiom)axiom);
					
				} else if (axiom instanceof OWLObjectPropertyAssertionAxiom) {
					processAxiom(dl_onto, (OWLObjectPropertyAssertionAxiom)axiom);
					
				} else if (axiom instanceof OWLDataPropertyAssertionAxiom) {
					processAxiom(dl_onto, (OWLDataPropertyAssertionAxiom)axiom);
					
				} else if (axiom instanceof OWLAnnotationAxiom) {
					/*
					 * Annotations axioms are intentionally ignored by the
					 * translator
					 */
				} else if (axiom instanceof OWLDeclarationAxiom) {
					OWLDeclarationAxiom owld = (OWLDeclarationAxiom) axiom;
					OWLEntity entity = owld.getEntity();
					if (entity instanceof OWLClass) {
						if (!entity.asOWLClass().isOWLThing()) {

							String uri = entity.asOWLClass().getIRI().toString();
							dl_onto.addConcept(dfac.getClassPredicate(uri));
						}
					} else if (entity instanceof OWLObjectProperty) {
						String uri = entity.asOWLObjectProperty().getIRI().toString();
						dl_onto.addRole(dfac.getObjectPropertyPredicate(uri));
					} else if (entity instanceof OWLDataProperty) {
						String uri = entity.asOWLDataProperty().getIRI().toString();
						dl_onto.addRole(dfac.getDataPropertyPredicate(uri));
					} else if (entity instanceof OWLIndividual) {
						/*
						 * NO OP, individual declarations are ignored silently
						 * during TBox translation
						 */
					} else {
						log.info("Ignoring declaration axiom: {}", axiom);
					}

					/*
					 * Annotations axioms are intentionally ignored by the
					 * translator
					 */
				}
				// else if (axiom instanceof OWLImportsDeclaration) {
				// /*
				// * Imports
				// */
				// }
				else {
					log.warn("Axiom not yet supported by Quest: {}", axiom.toString());
				}
			} catch (TranslationException e) {
				log.warn("Axiom not yet supported by Quest: {}", axiom.toString());
			}
		}
		return dl_onto;
	}
	
	private void processAxiom(Ontology dl_onto, OWLDisjointClassesAxiom aux) throws TranslationException {
		Set<SubClassExpression> disjointClasses = new HashSet<SubClassExpression>();
		for (OWLClassExpression oc : aux.getClassExpressionsAsList()) {
			BasicClassDescription c = getSubclassExpression(oc);
			disjointClasses.add((SubClassExpression) c);
		}
		
		DisjointClassesAxiom disj = ofac.createDisjointClassesAxiom(disjointClasses);				
		dl_onto.addAssertionWithCheck(disj);
	}

	private void processAxiom(Ontology dl_onto, OWLDisjointDataPropertiesAxiom aux) throws TranslationException {
		Set<PropertyExpression> disjointProperties = new HashSet<PropertyExpression>();
		for (OWLDataPropertyExpression prop : aux.getProperties()) {
			PropertyExpression p = getRoleExpression(prop);
			disjointProperties.add(p);
		}
		DisjointPropertiesAxiom disj = ofac.createDisjointPropertiesAxiom(disjointProperties);
		dl_onto.addAssertionWithCheck(disj);		
	}
	
	private void processAxiom(Ontology dl_onto, OWLDisjointObjectPropertiesAxiom aux) throws TranslationException {
		Set<PropertyExpression> disjointProperties = new HashSet<PropertyExpression>();
		for (OWLObjectPropertyExpression prop : aux.getProperties()) {
			PropertyExpression p = getRoleExpression(prop);
			disjointProperties.add(p);
		}
		DisjointPropertiesAxiom disj = ofac.createDisjointPropertiesAxiom(disjointProperties);				
		dl_onto.addAssertionWithCheck(disj);		
	}
	
	private void processAxiom(Ontology dl_onto, OWLAsymmetricObjectPropertyAxiom aux) throws TranslationException {
		Set<PropertyExpression> disjointProperties = new HashSet<PropertyExpression>();
		OWLObjectPropertyExpression prop = aux.getProperty(); 
		PropertyExpression p = getRoleExpression(prop);
		disjointProperties.add(p);
		disjointProperties.add(p.getInverse());
		
		DisjointPropertiesAxiom disj = ofac.createDisjointPropertiesAxiom(disjointProperties);				
		dl_onto.addAssertionWithCheck(disj);		
	}
	
	private void addSubclassAxiom(Ontology dl_onto, SubClassExpression subDescription, BasicClassDescription superDescription) {
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
		dl_onto.addAssertionWithCheck(inc);
	}

	private void addSubclassAxioms(Ontology dl_onto, SubClassExpression subDescription, List<BasicClassDescription> superDescriptions) {
		for (BasicClassDescription superDescription : superDescriptions) {
			addSubclassAxiom(dl_onto, subDescription, superDescription);
		}
	}

	private List<SubClassExpression> getSubclassExpressions(Collection<OWLClassExpression> owlExpressions) throws TranslationException {
		List<SubClassExpression> descriptions = new LinkedList<SubClassExpression>();
		for (OWLClassExpression OWLClassExpression : owlExpressions) {
			descriptions.add(getSubclassExpression(OWLClassExpression));
		}
		return descriptions;
	}

	private PropertyExpression getRoleExpression(OWLObjectPropertyExpression rolExpression) throws TranslationException {
		PropertyExpression role = null;

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

	private void addConceptEquivalences(Ontology ontology, List<SubClassExpression> roles) {
		for (int i = 0; i < roles.size(); i++) {
			for (int j = i + 1; j < roles.size(); j++) {
				SubClassExpression subclass = roles.get(i);
				SubClassExpression superclass = roles.get(j);
				SubClassOfAxiom inclusion1 = ofac.createSubClassAxiom(subclass, superclass);
				ontology.addAssertionWithCheck(inclusion1);
				SubClassOfAxiom inclusion2 = ofac.createSubClassAxiom(superclass, subclass);
				ontology.addAssertionWithCheck(inclusion2);
			}
		}
	}

	private void addRoleEquivalences(Ontology ontology, List<PropertyExpression> roles) {
		for (int i = 0; i < roles.size(); i++) {
			for (int j = i + 1; j < roles.size(); j++) {
				PropertyExpression subrole = roles.get(i);
				PropertyExpression superole = roles.get(j);
				SubPropertyOfAxiom inclusion1 = ofac.createSubPropertyAxiom(subrole, superole);
				ontology.addAssertionWithCheck(inclusion1);
				SubPropertyOfAxiom inclusion2 = ofac.createSubPropertyAxiom(superole, subrole);
				ontology.addAssertionWithCheck(inclusion2);
			}
		}
	}

	private List<PropertyExpression> getObjectRoleExpressions(Collection<OWLObjectPropertyExpression> rolExpressions) throws TranslationException {
		List<PropertyExpression> result = new LinkedList<PropertyExpression>();
		for (OWLObjectPropertyExpression rolExpression : rolExpressions) {
			result.add(getRoleExpression(rolExpression));
		}
		return result;
	}

	private List<PropertyExpression> getDataRoleExpressions(Collection<OWLDataPropertyExpression> rolExpressions) throws TranslationException {
		List<PropertyExpression> result = new LinkedList<PropertyExpression>();
		for (OWLDataPropertyExpression rolExpression : rolExpressions) {
			result.add(getRoleExpression(rolExpression));
		}
		return result;
	}

	private PropertyExpression getRoleExpression(OWLDataPropertyExpression rolExpression) throws TranslationException {
		PropertyExpression role = null;

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
			
			PropertyExpression role = getRoleExpression(owlProperty);
			BasicClassDescription filler = getSubclassExpression(owlFiller);

			PropertyExpression auxRole = ofac.createObjectProperty(OntologyImpl.AUXROLEURI + auxRoleCounter);
			auxRoleCounter += 1;

			PropertyExpression exp = ofac.createObjectProperty(auxRole.getPredicate().getName());
			if (role.isInverse())
				exp = exp.getInverse();
			
			auxclass = ofac.createPropertySomeRestriction(exp);
			auxiliaryClassProperties.put(someexp, auxclass);

			/* Creating the new subrole assertions */
			SubPropertyOfAxiom subrole = ofac.createSubPropertyAxiom(auxRole, ofac.createProperty(role.getPredicate().getName()));
			dl_onto.addAssertionWithCheck(subrole);
			
			/* Creating the range assertion */
			PropertyExpression expInv = exp.getInverse();
			SomeValuesFrom propertySomeRestrictionInv = ofac.createPropertySomeRestriction(expInv);
			SubClassOfAxiom subclass = ofac.createSubClassAxiom(propertySomeRestrictionInv, filler);
			dl_onto.addAssertionWithCheck(subclass);
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
			PropertyExpression role = getRoleExpression(owlProperty);

			OWLDataRange owlFiller = someexp.getFiller();		
			BasicClassDescription filler = getDataTypeExpression(owlFiller);

			// TODO: changed to data properties
			PropertyExpression auxRole = ofac.createObjectProperty(OntologyImpl.AUXROLEURI + auxRoleCounter);
			auxRoleCounter += 1;

			PropertyExpression exp = ofac.createProperty(auxRole.getPredicate().getName()/*, role.isInverse()*/);
			auxclass = ofac.createPropertySomeRestriction(exp);
			auxiliaryDatatypeProperties.put(someexp, auxclass);

			/* Creating the new subrole assertions */
			SubPropertyOfAxiom subrole = ofac.createSubPropertyAxiom(auxRole, ofac.createProperty(role.getPredicate().getName()));
			dl_onto.addAssertionWithCheck(subrole);
			
			/* Creating the range assertion */
			PropertyExpression expInv = exp.getInverse();
			SomeValuesFrom propertySomeRestrictionInv = ofac.createPropertySomeRestriction(expInv);
			SubClassOfAxiom subclass = ofac.createSubClassAxiom(propertySomeRestrictionInv, filler);
			dl_onto.addAssertionWithCheck(subclass);
		}

		return auxclass;
	}
	
	private Datatype getDataTypeExpression(OWLDataRange filler) throws TranslationException {
		OWLDatatype owlDatatype = (OWLDatatype) filler;
		COL_TYPE datatype = getColumnType(owlDatatype);
		return ofac.createDataType(dfac.getTypePredicate(datatype));
	}

	private SubClassExpression getSubclassExpression(OWLClassExpression owlExpression) throws TranslationException {
		SubClassExpression cd = null;
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
			dl_onto.addAssertionWithCheck(a);
	}
	
	public ClassAssertion translate(OWLClassAssertionAxiom aux) {

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

		Predicate classpred = dfac.getClassPredicate(namedclass.getIRI().toString());
		OClass concept = ofac.createClass(classpred.getName());
		
		URIConstant c = dfac.getConstantURI(indv.asOWLNamedIndividual().getIRI().toString());

		ClassAssertion assertion = ofac.createClassAssertion(concept, c);
		return assertion;
	}
	
	private void processAxiom(Ontology dl_onto, OWLObjectPropertyAssertionAxiom aux) {
		PropertyAssertion assertion = translate(aux);
		dl_onto.addAssertionWithCheck(assertion);
	}
	
	public PropertyAssertion translate(OWLObjectPropertyAssertionAxiom aux) {
			
		if (aux.getSubject().isAnonymous() || aux.getObject().isAnonymous()) 
			throw new RuntimeException("Found anonymous individual, this feature is not supported");			

		OWLObjectPropertyExpression propertyExperssion = aux.getProperty();
					
		PropertyExpression prop;
		URIConstant c1, c2;

		if (propertyExperssion instanceof OWLObjectProperty) {
			OWLObjectProperty namedclass = (OWLObjectProperty) propertyExperssion;
			Predicate p = dfac.getObjectPropertyPredicate(namedclass.getIRI().toString());
			prop = ofac.createObjectProperty(p.getName());
			
			c1 = dfac.getConstantURI(aux.getSubject().asOWLNamedIndividual().getIRI().toString());
			c2 = dfac.getConstantURI(aux.getObject().asOWLNamedIndividual().getIRI().toString());
			
			// TODO: check for bottom
			
		} else if (propertyExperssion instanceof OWLObjectInverseOf) {
			OWLObjectProperty namedclass = ((OWLObjectInverseOf) propertyExperssion).getInverse().getNamedProperty();
			Predicate p = dfac.getObjectPropertyPredicate(namedclass.getIRI().toString());
			prop = ofac.createObjectProperty(p.getName()).getInverse();
			
			c1 = dfac.getConstantURI(aux.getSubject().asOWLNamedIndividual().getIRI().toString());
			c2 = dfac.getConstantURI(aux.getObject().asOWLNamedIndividual().getIRI().toString());

			// TODO: check for bottom			
		}
		else
			throw new RuntimeException("Found complex property expression in an asserion, this feature is not supported");

		PropertyAssertion assertion = ofac.createPropertyAssertion(prop, c1, c2);
		return assertion;
	}
	
	private void processAxiom(Ontology dl_onto, OWLDataPropertyAssertionAxiom aux) {
		PropertyAssertion assertion = translate(aux);
		dl_onto.addAssertionWithCheck(assertion);
	}
	
	public PropertyAssertion translate(OWLDataPropertyAssertionAxiom aux) {
		
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

		Predicate p = dfac.getDataPropertyPredicate(property);
		PropertyExpression prop = ofac.createDataProperty(p.getName());
		
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
