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
import it.unibz.krdb.obda.ontology.Assertion;
import it.unibz.krdb.obda.ontology.BasicClassDescription;
import it.unibz.krdb.obda.ontology.ClassDescription;
import it.unibz.krdb.obda.ontology.DataType;
import it.unibz.krdb.obda.ontology.Description;
import it.unibz.krdb.obda.ontology.DisjointClassAxiom;
import it.unibz.krdb.obda.ontology.DisjointDataPropertyAxiom;
import it.unibz.krdb.obda.ontology.DisjointObjectPropertyAxiom;
import it.unibz.krdb.obda.ontology.LanguageProfile;
import it.unibz.krdb.obda.ontology.OClass;
import it.unibz.krdb.obda.ontology.Ontology;
import it.unibz.krdb.obda.ontology.OntologyFactory;
import it.unibz.krdb.obda.ontology.Property;
import it.unibz.krdb.obda.ontology.PropertyFunctionalAxiom;
import it.unibz.krdb.obda.ontology.PropertySomeClassRestriction;
import it.unibz.krdb.obda.ontology.PropertySomeRestriction;
import it.unibz.krdb.obda.ontology.SubDescriptionAxiom;
import it.unibz.krdb.obda.ontology.impl.OntologyFactoryImpl;
import it.unibz.krdb.obda.ontology.impl.OntologyImpl;
import it.unibz.krdb.obda.ontology.impl.PropertySomeDataTypeRestrictionImpl;
import it.unibz.krdb.obda.ontology.impl.PunningException;
import it.unibz.krdb.obda.ontology.impl.SubClassAxiomImpl;
import it.unibz.krdb.obda.ontology.impl.SubPropertyAxiomImpl;

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
import org.semanticweb.owlapi.model.OWLIndividualAxiom;
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
 * Translates an OWLOntology into ontops internal ontlogy representation. It
 * will ignore all ABox assertions and does a syntactic approximation of the
 * ontology, dropping anything not support by Quest during inference.
 * 
 * @author Mariano Rodriguez Muro <mariano.muro@gmail.com>
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
	final Map<ClassDescription, List<SubDescriptionAxiom>> auxiliaryAssertions = new HashMap<ClassDescription, List<SubDescriptionAxiom>>();

	int auxRoleCounter = 0;

	public OWLAPI3Translator() {

	}

	/***
	 * Load all the ontologies into a single translated merge.
	 * 
	 * @param ontologies
	 * @return
	 * @throws Exception
	 */
	public Ontology mergeTranslateOntologies(Set<OWLOntology> ontologies) throws Exception {
		/*
		 * We will keep track of the loaded ontologies and tranlsate the TBox
		 * part of them into our internal represntation
		 */
		String uri = "http://it.unibz.krdb.obda/Quest/auxiliaryontology";

		Ontology translatedOntologyMerge = ofac.createOntology(uri);

		log.debug("Load ontologies called. Translating ontologies.");
		OWLAPI3Translator translator = new OWLAPI3Translator();
		// Set<URI> uris = new HashSet<URI>();

		Ontology translation = ofac.createOntology(uri);
		for (OWLOntology onto : ontologies) {
			// uris.add(onto.getIRI().toURI());
			Ontology aux = translator.translate(onto);
			translation.addConcepts(aux.getConcepts());
			translation.addRoles(aux.getRoles());
			translation.addAssertions(aux.getAssertions());
			translation.getABox().addAll(aux.getABox());
			translation.getDisjointDescriptionAxioms().addAll(aux.getDisjointDescriptionAxioms());
			translation.getFunctionalPropertyAxioms().addAll(aux.getFunctionalPropertyAxioms());
		}
		/* we translated successfully, now we append the new assertions */

		translatedOntologyMerge = translation;

		// translatedOntologyMerge.addAssertions(translation.getAssertions());
		// translatedOntologyMerge.addConcepts(new
		// ArrayList<ClassDescription>(translation.getConcepts()));
		// translatedOntologyMerge.addRoles(new
		// ArrayList<Property>(translation.getRoles()));
		// translatedOntologyMerge.saturate();

		log.debug("Ontology loaded: {}", translatedOntologyMerge);

		return translatedOntologyMerge;

	}

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
		// ManchesterOWLSyntaxOWLObjectRendererImpl rend = new
		// ManchesterOWLSyntaxOWLObjectRendererImpl();

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
			// log.warn(report.toString());
			// for (OWLProfileViolation violation : report.getViolations())
			// axiomIgnoresOWL2QL.add(violation.getAxiom());
		}

		// Ontology dl_onto =
		// ofac.createOntology((owl.getOntologyID().getOntologyIRI().toString()));
		Ontology dl_onto = ofac.createOntology("http://www.unibz.it/ontology");

		HashSet<String> objectproperties = new HashSet<String>();
		HashSet<String> dataproperties = new HashSet<String>();
		HashSet<String> classes = new HashSet<String>();

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
					List<ClassDescription> vec = getSubclassExpressions(equivalents);

					addConceptEquivalences(dl_onto, vec);

				} else if (axiom instanceof OWLSubClassOfAxiom) {

					OWLSubClassOfAxiom aux = (OWLSubClassOfAxiom) axiom;
					ClassDescription subDescription = getSubclassExpression(aux.getSubClass());
					List<ClassDescription> superDescriptions = getSuperclassExpressions(aux.getSuperClass());

					addSubclassAxioms(dl_onto, subDescription, superDescriptions);

				} else if (axiom instanceof OWLDataPropertyDomainAxiom) {

					OWLDataPropertyDomainAxiom aux = (OWLDataPropertyDomainAxiom) axiom;
					Property role = getRoleExpression(aux.getProperty());

					ClassDescription subclass = ofac.getPropertySomeRestriction(role.getPredicate(), role.isInverse());
					List<ClassDescription> superDescriptions = getSuperclassExpressions(((OWLDataPropertyDomainAxiom) axiom).getDomain());

					addSubclassAxioms(dl_onto, subclass, superDescriptions);

				} else if (axiom instanceof OWLDataPropertyRangeAxiom) {

					OWLDataPropertyRangeAxiom aux = (OWLDataPropertyRangeAxiom) axiom;
					Property role = getRoleExpression(aux.getProperty());

					ClassDescription subclass = ofac.getPropertySomeRestriction(role.getPredicate(), true);
					OWLDatatype rangeDatatype = aux.getRange().asOWLDatatype();

					if (rangeDatatype.isBuiltIn()) {

						Predicate.COL_TYPE columnType = getColumnType(rangeDatatype);
						DataType datatype = ofac.createDataType(dfac.getTypePredicate(columnType));
						addSubclassAxiom(dl_onto, subclass, datatype);
					} else {
						log.warn("Ignoring range axiom since it refers to a non-supported datatype: " + axiom.toString());
					}

				} else if (axiom instanceof OWLSubDataPropertyOfAxiom) {

					OWLSubDataPropertyOfAxiom aux = (OWLSubDataPropertyOfAxiom) axiom;
					Property subrole = getRoleExpression(aux.getSubProperty());
					Property superrole = getRoleExpression(aux.getSuperProperty());

					SubPropertyAxiomImpl roleinc = (SubPropertyAxiomImpl) ofac.createSubPropertyAxiom(subrole, superrole);

					dl_onto.addAssertion(roleinc);

				} else if (axiom instanceof OWLEquivalentDataPropertiesAxiom) {

					if (profile.order() < LanguageProfile.OWL2QL.order())
						throw new TranslationException();

					OWLEquivalentDataPropertiesAxiom aux = (OWLEquivalentDataPropertiesAxiom) axiom;
					List<Property> vec = getDataRoleExpressions(aux.getProperties());
					addRoleEquivalences(dl_onto, vec);

				} else if (axiom instanceof OWLEquivalentObjectPropertiesAxiom) {

					if (profile.order() < LanguageProfile.OWL2QL.order())
						throw new TranslationException();

					OWLEquivalentObjectPropertiesAxiom aux = (OWLEquivalentObjectPropertiesAxiom) axiom;
					List<Property> vec = getObjectRoleExpressions(aux.getProperties());
					addRoleEquivalences(dl_onto, vec);

				} else if (axiom instanceof OWLFunctionalDataPropertyAxiom) {
					if (profile.order() < LanguageProfile.DLLITEA.order())
						throw new TranslationException();
					OWLFunctionalDataPropertyAxiom aux = (OWLFunctionalDataPropertyAxiom) axiom;
					Property role = getRoleExpression(aux.getProperty());
					PropertyFunctionalAxiom funct = ofac.createPropertyFunctionalAxiom(role);

					dl_onto.addAssertion(funct);

				} else if (axiom instanceof OWLInverseObjectPropertiesAxiom) {
					if (profile.order() < LanguageProfile.OWL2QL.order())
						throw new TranslationException();

					OWLInverseObjectPropertiesAxiom aux = (OWLInverseObjectPropertiesAxiom) axiom;
					OWLObjectPropertyExpression exp1 = aux.getFirstProperty();
					OWLObjectPropertyExpression exp2 = aux.getSecondProperty();
					Property role1 = getRoleExpression(exp1);
					Property role2 = getRoleExpression(exp2);

					Property invrole1 = ofac.createProperty(role1.getPredicate(), !role1.isInverse());
					Property invrole2 = ofac.createProperty(role2.getPredicate(), !role2.isInverse());

					SubPropertyAxiomImpl inc1 = (SubPropertyAxiomImpl) ofac.createSubPropertyAxiom(role1, invrole2);
					SubPropertyAxiomImpl inc2 = (SubPropertyAxiomImpl) ofac.createSubPropertyAxiom(role2, invrole1);

					dl_onto.addAssertion(inc1);
					dl_onto.addAssertion(inc2);

				} else if (axiom instanceof OWLSymmetricObjectPropertyAxiom) {
					if (profile.order() < LanguageProfile.OWL2QL.order())
						throw new TranslationException();
					OWLSymmetricObjectPropertyAxiom aux = (OWLSymmetricObjectPropertyAxiom) axiom;
					OWLObjectPropertyExpression exp1 = aux.getProperty();
					Property role = getRoleExpression(exp1);
					Property invrole = ofac.createProperty(role.getPredicate(), !role.isInverse());

					SubPropertyAxiomImpl symm = (SubPropertyAxiomImpl) ofac.createSubPropertyAxiom(invrole, role);

					dl_onto.addAssertion(symm);

				} else if (axiom instanceof OWLObjectPropertyDomainAxiom) {

					OWLObjectPropertyDomainAxiom aux = (OWLObjectPropertyDomainAxiom) axiom;
					Property role = getRoleExpression(aux.getProperty());

					ClassDescription subclass = ofac.getPropertySomeRestriction(role.getPredicate(), role.isInverse());
					List<ClassDescription> superDescriptions = getSuperclassExpressions(((OWLObjectPropertyDomainAxiom) axiom).getDomain());

					addSubclassAxioms(dl_onto, subclass, superDescriptions);

				} else if (axiom instanceof OWLObjectPropertyRangeAxiom) {

					OWLObjectPropertyRangeAxiom aux = (OWLObjectPropertyRangeAxiom) axiom;
					Property role = getRoleExpression(aux.getProperty());

					ClassDescription subclass = ofac.getPropertySomeRestriction(role.getPredicate(), !role.isInverse());
					List<ClassDescription> superDescriptions = getSuperclassExpressions(((OWLObjectPropertyRangeAxiom) axiom).getRange());

					addSubclassAxioms(dl_onto, subclass, superDescriptions);

				} else if (axiom instanceof OWLSubObjectPropertyOfAxiom) {

					OWLSubObjectPropertyOfAxiom aux = (OWLSubObjectPropertyOfAxiom) axiom;
					Property subrole = getRoleExpression(aux.getSubProperty());
					Property superrole = getRoleExpression(aux.getSuperProperty());

					SubPropertyAxiomImpl roleinc = (SubPropertyAxiomImpl) ofac.createSubPropertyAxiom(subrole, superrole);

					dl_onto.addAssertion(roleinc);

				} else if (axiom instanceof OWLFunctionalObjectPropertyAxiom) {
					if (profile.order() < LanguageProfile.OWL2QL.order())
						throw new TranslationException();
					OWLFunctionalObjectPropertyAxiom aux = (OWLFunctionalObjectPropertyAxiom) axiom;
					Property role = getRoleExpression(aux.getProperty());
					PropertyFunctionalAxiom funct = ofac.createPropertyFunctionalAxiom(role);

					dl_onto.addAssertion(funct);
					
				} else if (axiom instanceof OWLInverseFunctionalObjectPropertyAxiom) {
					if (profile.order() < LanguageProfile.OWL2QL.order())
						throw new TranslationException();
					OWLInverseFunctionalObjectPropertyAxiom aux = (OWLInverseFunctionalObjectPropertyAxiom) axiom;
					Property role = getRoleExpression(aux.getProperty());
					Property invrole = ofac.createProperty(role.getPredicate(), !role.isInverse());
					PropertyFunctionalAxiom funct = ofac.createPropertyFunctionalAxiom(invrole);

					dl_onto.addAssertion(funct);

				} else if (axiom instanceof OWLDisjointClassesAxiom) {
					OWLDisjointClassesAxiom aux = (OWLDisjointClassesAxiom) axiom;
					for (OWLClassExpression disjClass : aux.getClassExpressionsAsList()) {
						if (!(disjClass instanceof OWLClass))
						throw new TranslationException("Invalid class expression in disjoint class axiom: "+disjClass.toString());
					}
					Set<OWLClass> disjointClasses = aux.getClassesInSignature();
					Iterator<OWLClass> iter = disjointClasses.iterator();
					if (!iter.hasNext())
						throw new TranslationException();
					OClass c1 = ofac.createClass(iter.next().toStringID());
					OClass c2 = ofac.createClass(iter.next().toStringID());
					DisjointClassAxiom disj = ofac.createDisjointClassAxiom(c1, c2);
					
					dl_onto.addAssertion(disj);
							
				} else if (axiom instanceof OWLDisjointDataPropertiesAxiom) {
					OWLDisjointDataPropertiesAxiom aux = (OWLDisjointDataPropertiesAxiom) axiom;
					Set<OWLDataProperty> disjointProps = aux.getDataPropertiesInSignature();
					Iterator<OWLDataProperty> iter = disjointProps.iterator();
					if (!iter.hasNext())
						throw new TranslationException();
					Property p1 = ofac.createDataProperty(iter.next().toStringID());
					Property p2 = ofac.createDataProperty(iter.next().toStringID());
					DisjointDataPropertyAxiom disj = ofac.createDisjointDataPropertyAxiom(p1.getPredicate(), p2.getPredicate());
					
					dl_onto.addAssertion(disj);
					
				} else if (axiom instanceof OWLDisjointObjectPropertiesAxiom) {
					OWLDisjointObjectPropertiesAxiom aux = (OWLDisjointObjectPropertiesAxiom) axiom;
					Set<OWLObjectProperty> disjointProps = aux.getObjectPropertiesInSignature();
					Iterator<OWLObjectProperty> iter = disjointProps.iterator();
					if (!iter.hasNext())
						throw new TranslationException();
					Property p1 = ofac.createObjectProperty(iter.next().toStringID());
					Property p2 = ofac.createObjectProperty(iter.next().toStringID());
					DisjointObjectPropertyAxiom disj = ofac.createDisjointObjectPropertyAxiom(p1.getPredicate(), p2.getPredicate());
					
					dl_onto.addAssertion(disj);
				
				
				} else if (axiom instanceof OWLIndividualAxiom) {
					Assertion translatedAxiom = translate((OWLIndividualAxiom)axiom);
					if (translatedAxiom != null)
						dl_onto.addAssertion(translatedAxiom);
					
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
						log.warn("Ignoring declartion axiom: {}", axiom);
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

	private void addSubclassAxiom(Ontology dl_onto, ClassDescription subDescription, ClassDescription superDescription) {
		if (superDescription == null || subDescription == null) {
			log.warn("NULL: {} {}", subDescription, superDescription);
		}

		if (superDescription instanceof BasicClassDescription) {

			/* We ignore TOP and BOTTOM (Thing and Nothing) */
			if (superDescription instanceof OClass) {
				OClass classDescription = (OClass) superDescription;
				if (classDescription.toString().equals("http://www.w3.org/2002/07/owl#Thing")) {
					return;
				}
			}
			SubClassAxiomImpl inc = (SubClassAxiomImpl) ofac.createSubClassAxiom(subDescription, superDescription);
			dl_onto.addAssertion(inc);
		} else {
			// log.debug("Generating encoding for {} subclassof {}",
			// subDescription, superDescription);

			/*
			 * We found an existential, we need to get an auxiliary set of
			 * subClassAssertion. Remember, each \exists R.A might have an
			 * encoding using existing two axioms. \exists R'^- subClassof A, R'
			 * subRoleOf R.
			 */
			List<SubDescriptionAxiom> aux = auxiliaryAssertions.get(superDescription);
			PropertySomeRestriction auxclass = null;
			if (aux == null) {
				/*
				 * no auxiliary subclass assertions found for this exists R.A,
				 * creating a new one
				 */
				Predicate role = null;
				BasicClassDescription filler = null;
				boolean isInverse = false;

				if (superDescription instanceof PropertySomeClassRestriction) {
					PropertySomeClassRestriction eR = (PropertySomeClassRestriction) superDescription;
					role = eR.getPredicate();
					filler = eR.getFiller();
					isInverse = eR.isInverse();
				} else if (superDescription instanceof PropertySomeDataTypeRestrictionImpl) {
					PropertySomeDataTypeRestrictionImpl eR = (PropertySomeDataTypeRestrictionImpl) superDescription;
					role = eR.getPredicate();
					filler = eR.getFiller();
					isInverse = eR.isInverse();
				}

				Property auxRole = ofac.createProperty(dfac.getObjectPropertyPredicate((OntologyImpl.AUXROLEURI + auxRoleCounter)));
				auxRoleCounter += 1;

				PropertySomeRestriction propertySomeRestriction = ofac.getPropertySomeRestriction(auxRole.getPredicate(), isInverse);
				auxclass = propertySomeRestriction;

				/* Creating the new subrole assertions */
				SubPropertyAxiomImpl subrole = (SubPropertyAxiomImpl) ofac
						.createSubPropertyAxiom(auxRole, ofac.createProperty(role, false)); // Roman:
																							// was
																							// isInverse
																							// in
																							// place
																							// of
																							// false
				/* Creatin the range assertion */
				PropertySomeRestriction propertySomeRestrictionInv = ofac.getPropertySomeRestriction(auxRole.getPredicate(), !isInverse);

				SubClassAxiomImpl subclass = (SubClassAxiomImpl) ofac.createSubClassAxiom(propertySomeRestrictionInv, filler);
				aux = new LinkedList<SubDescriptionAxiom>();
				aux.add(subclass);
				aux.add(subrole);

				auxiliaryAssertions.put(superDescription, aux);

				dl_onto.addAssertion(subclass);
				dl_onto.addAssertion(subrole);
			} else {
				/*
				 * The encoding already exists, so we retrieve it, and get the
				 * existsR that we need for the extra axiom
				 */
				SubClassAxiomImpl axiom1 = (SubClassAxiomImpl) aux.get(0);
				PropertySomeRestriction propertySomeInv = (PropertySomeRestriction) axiom1.getSub();
				PropertySomeRestriction propertySome = ofac.getPropertySomeRestriction(propertySomeInv.getPredicate(),
						!propertySomeInv.isInverse());
				auxclass = propertySome;
			}

			SubClassAxiomImpl inc = (SubClassAxiomImpl) ofac.createSubClassAxiom(subDescription, auxclass);
			dl_onto.addAssertion(inc);
		}
	}

	private void addSubclassAxioms(Ontology dl_onto, ClassDescription subDescription, List<ClassDescription> superDescriptions) {
		for (ClassDescription superDescription : superDescriptions) {
			addSubclassAxiom(dl_onto, subDescription, superDescription);
		}
	}

	private List<ClassDescription> getSubclassExpressions(Collection<OWLClassExpression> owlExpressions) throws TranslationException {
		List<ClassDescription> descriptions = new LinkedList<ClassDescription>();
		for (OWLClassExpression OWLClassExpression : owlExpressions) {
			descriptions.add(getSubclassExpression(OWLClassExpression));
		}
		return descriptions;
	}

	private Property getRoleExpression(OWLObjectPropertyExpression rolExpression) throws TranslationException {
		Property role = null;

		if (rolExpression instanceof OWLObjectProperty) {
			role = ofac.createObjectProperty((rolExpression.asOWLObjectProperty().getIRI().toString()));
		} else if (rolExpression instanceof OWLObjectInverseOf) {
			if (profile.order() < LanguageProfile.OWL2QL.order())
				throw new TranslationException();
			OWLObjectInverseOf aux = (OWLObjectInverseOf) rolExpression;
			role = ofac.createProperty(dfac.getObjectPropertyPredicate((aux.getInverse().asOWLObjectProperty().getIRI().toString())), true);
		} else {
			throw new TranslationException();
		}
		return role;

	}

	private void addConceptEquivalences(Ontology ontology, List<ClassDescription> roles) {
		for (int i = 0; i < roles.size(); i++) {
			for (int j = i + 1; j < roles.size(); j++) {
				ClassDescription subclass = roles.get(i);
				ClassDescription superclass = roles.get(j);
				SubClassAxiomImpl inclusion1 = (SubClassAxiomImpl) ofac.createSubClassAxiom(subclass, superclass);
				SubClassAxiomImpl inclusion2 = (SubClassAxiomImpl) ofac.createSubClassAxiom(superclass, subclass);
				ontology.addAssertion(inclusion1);
				ontology.addAssertion(inclusion2);
			}
		}
	}

	private void addRoleEquivalences(Ontology ontology, List<Property> roles) {
		for (int i = 0; i < roles.size(); i++) {
			for (int j = i + 1; j < roles.size(); j++) {
				Property subrole = roles.get(i);
				Property superole = roles.get(j);
				SubPropertyAxiomImpl inclusion1 = (SubPropertyAxiomImpl) ofac.createSubPropertyAxiom(subrole, superole);
				SubPropertyAxiomImpl inclusion2 = (SubPropertyAxiomImpl) ofac.createSubPropertyAxiom(superole, subrole);
				ontology.addAssertion(inclusion1);
				ontology.addAssertion(inclusion2);
			}
		}
	}

	private List<Property> getObjectRoleExpressions(Collection<OWLObjectPropertyExpression> rolExpressions) throws TranslationException {
		List<Property> result = new LinkedList<Property>();
		for (OWLObjectPropertyExpression rolExpression : rolExpressions) {
			result.add(getRoleExpression(rolExpression));
		}
		return result;
	}

	private List<Property> getDataRoleExpressions(Collection<OWLDataPropertyExpression> rolExpressions) throws TranslationException {
		List<Property> result = new LinkedList<Property>();
		for (OWLDataPropertyExpression rolExpression : rolExpressions) {
			result.add(getRoleExpression(rolExpression));
		}
		return result;
	}

	private Property getRoleExpression(OWLDataPropertyExpression rolExpression) throws TranslationException {
		Property role = null;

		if (rolExpression instanceof OWLDataProperty) {
			role = ofac.createDataProperty((rolExpression.asOWLDataProperty().getIRI().toString()));
		} else {
			throw new TranslationException();
		}
		return role;

	}

	private List<ClassDescription> getSuperclassExpressions(OWLClassExpression owlExpression) throws TranslationException {
		List<ClassDescription> result = new LinkedList<ClassDescription>();
		if (owlExpression instanceof OWLObjectIntersectionOf) {
			if (profile.order() < LanguageProfile.OWL2QL.order()) {
				throw new TranslationException();
			}
			OWLObjectIntersectionOf intersection = (OWLObjectIntersectionOf) owlExpression;
			Set<OWLClassExpression> operands = intersection.getOperands();
			for (OWLClassExpression operand : operands) {
				result.addAll(getSuperclassExpressions(operand));
			}
		} else if (owlExpression instanceof OWLObjectSomeValuesFrom) {
			if (profile.order() < LanguageProfile.OWL2QL.order()) {
				throw new TranslationException();
			}
			OWLObjectSomeValuesFrom someexp = (OWLObjectSomeValuesFrom) owlExpression;
			OWLObjectPropertyExpression property = someexp.getProperty();
			OWLClassExpression filler = someexp.getFiller();
			if (!(filler instanceof OWLClass)) {
				throw new TranslationException();
			}
			if (filler.isOWLThing()) {
				result.add(getSubclassExpression(owlExpression));
			} else {
				Property role = getRoleExpression(property);
				ClassDescription cd = ofac.createPropertySomeClassRestriction(role.getPredicate(), role.isInverse(),
						(OClass) getSubclassExpression(filler));
				result.add(cd);
			}
		} else if (owlExpression instanceof OWLDataSomeValuesFrom) {
			if (profile.order() < LanguageProfile.OWL2QL.order()) {
				throw new TranslationException();
			}
			OWLDataSomeValuesFrom someexp = (OWLDataSomeValuesFrom) owlExpression;
			OWLDataPropertyExpression property = someexp.getProperty();
			OWLDataRange filler = someexp.getFiller();

			if (filler.isTopDatatype()) {
				Property role = getRoleExpression(property);
				ClassDescription cd = ofac.getPropertySomeRestriction(role.getPredicate(), role.isInverse());
				result.add(cd);
			} else if (filler instanceof OWLDatatype) {
				Property role = getRoleExpression(property);
				ClassDescription cd = ofac.createPropertySomeDataTypeRestriction(role.getPredicate(), role.isInverse(),
						getDataTypeExpression(filler));
				result.add(cd);
			}
		} else {
			result.add(getSubclassExpression(owlExpression));
		}
		return result;
	}

	private DataType getDataTypeExpression(OWLDataRange filler) throws TranslationException {
		OWLDatatype owlDatatype = (OWLDatatype) filler;
		COL_TYPE datatype = getColumnType(owlDatatype);
		return ofac.createDataType(dfac.getTypePredicate(datatype));
	}

	private ClassDescription getSubclassExpression(OWLClassExpression owlExpression) throws TranslationException {
		ClassDescription cd = null;
		if (owlExpression instanceof OWLClass) {
			String uri = ((OWLClass) owlExpression).getIRI().toString();
			Predicate p = dfac.getClassPredicate(uri);
			cd = ofac.createClass(p);

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
			Predicate attribute = dfac.getDataPropertyPredicate(uri);
			cd = ofac.getPropertySomeRestriction(attribute, false);

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
			Predicate role = dfac.getObjectPropertyPredicate(uri);

			if (propExp instanceof OWLObjectInverseOf) {
				cd = ofac.getPropertySomeRestriction(role, true);
			} else {
				cd = ofac.getPropertySomeRestriction(role, false);
			}

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
			Predicate role = dfac.getObjectPropertyPredicate(uri);

			if (propExp instanceof OWLObjectInverseOf) {
				cd = ofac.getPropertySomeRestriction(role, true);
			} else {
				cd = ofac.getPropertySomeRestriction(role, false);
			}
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
			Predicate role = dfac.getDataPropertyPredicate(uri);
			cd = ofac.getPropertySomeRestriction(role, false);
		}

		if (cd == null) {
			throw new TranslationException();
		}
		return cd;
	}

	private class TranslationException extends Exception {

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

	public Assertion translate(OWLIndividualAxiom axiom) {
		return translate(axiom, null);
	}

	/***
	 * This will translate an OWLABox assertion into our own abox assertions.
	 * The functioning is straight forward except for the equivalenceMap.
	 * 
	 * The equivalenceMap is used to align the ABox assertions with n
	 * alternative vocabulary. The equivalence map relates a Class or Role with
	 * another Class or Role (or inverse Role) that should be used instead of
	 * the original to create the ABox assertions.
	 * 
	 * For example, if the equivalenceMap has the mapping hasFather ->
	 * inverse(hasChild), then, if the ABox assertions is
	 * "hasFather(mariano,ramon)", the translator will return
	 * "hasChild(ramon,mariano)".
	 * 
	 * If there is no equivalence mapping for a given class or property, the
	 * translation is straight forward. If the map is empty or it is null, the
	 * translation is straight forward.
	 * 
	 * @param axiom
	 * @param equivalenceMap
	 * @return
	 */
	public Assertion translate(OWLIndividualAxiom axiom, Map<Predicate, Description> equivalenceMap) {

		if (axiom instanceof OWLClassAssertionAxiom) {

			/*
			 * Class assertions
			 */

			OWLClassAssertionAxiom assertion = (OWLClassAssertionAxiom) axiom;
			OWLClassExpression classExpression = assertion.getClassExpression();
			if (!(classExpression instanceof OWLClass) || classExpression.isOWLThing() || classExpression.isOWLNothing())
				return null;

			OWLClass namedclass = (OWLClass) classExpression;
			OWLIndividual indv = assertion.getIndividual();

			if (indv.isAnonymous()) {
				throw new RuntimeException("Found anonymous individual, this feature is not supported");
			}

			Predicate classproperty = dfac.getClassPredicate((namedclass.getIRI().toString()));
			URIConstant c = dfac.getConstantURI(indv.asOWLNamedIndividual().getIRI().toString());

			Description equivalent = null;
			if (equivalenceMap != null)
				equivalent = equivalenceMap.get(classproperty);

			if (equivalent == null)
				return ofac.createClassAssertion(classproperty, c);
			else {
				return ofac.createClassAssertion(((OClass) equivalent).getPredicate(), c);
			}

		} else if (axiom instanceof OWLObjectPropertyAssertionAxiom) {

			/*
			 * Role assertions
			 */

			OWLObjectPropertyAssertionAxiom assertion = (OWLObjectPropertyAssertionAxiom) axiom;
			OWLObjectPropertyExpression propertyExperssion = assertion.getProperty();

			String property = null;
			OWLIndividual subject = null;
			OWLIndividual object = null;

			if (propertyExperssion instanceof OWLObjectProperty) {
				OWLObjectProperty namedclass = (OWLObjectProperty) propertyExperssion;
				property = namedclass.getIRI().toString();

				subject = assertion.getSubject();
				object = assertion.getObject();

			} else if (propertyExperssion instanceof OWLObjectInverseOf) {
				OWLObjectProperty namedclass = ((OWLObjectInverseOf) propertyExperssion).getInverse().getNamedProperty();
				property = namedclass.getIRI().toString();
				subject = assertion.getObject();
				object = assertion.getSubject();
			}

			if (subject.isAnonymous()) {
				throw new RuntimeException("Found anonymous individual, this feature is not supported");
			}

			if (object.isAnonymous()) {
				throw new RuntimeException("Found anonymous individual, this feature is not supported");
			}
			Predicate p = dfac.getObjectPropertyPredicate(property);
			URIConstant c1 = dfac.getConstantURI(subject.asOWLNamedIndividual().getIRI().toString());
			URIConstant c2 = dfac.getConstantURI(object.asOWLNamedIndividual().getIRI().toString());

			Description equivalent = null;
			if (equivalenceMap != null)
				equivalent = equivalenceMap.get(p);

			if (equivalent == null)
				return ofac.createObjectPropertyAssertion(p, c1, c2);
			else {
				Property equiProp = (Property) equivalent;
				if (!equiProp.isInverse()) {
					return ofac.createObjectPropertyAssertion(equiProp.getPredicate(), c1, c2);
				} else {
					return ofac.createObjectPropertyAssertion(equiProp.getPredicate(), c2, c1);
				}
			}

		} else if (axiom instanceof OWLDataPropertyAssertionAxiom) {

			/*
			 * Attribute assertions
			 */

			OWLDataPropertyAssertionAxiom assertion = (OWLDataPropertyAssertionAxiom) axiom;
			OWLDataProperty propertyExperssion = (OWLDataProperty) assertion.getProperty();

			String property = propertyExperssion.getIRI().toString();
			OWLIndividual subject = assertion.getSubject();
			OWLLiteral object = assertion.getObject();

			if (subject.isAnonymous()) {
				throw new RuntimeException("Found anonymous individual, this feature is not supported");
			}

			Predicate.COL_TYPE type;
			try {
				type = getColumnType(object.getDatatype());
			} catch (TranslationException e) {
				throw new RuntimeException(e.getMessage());
			}

			Predicate p = dfac.getDataPropertyPredicate(property);
			URIConstant c1 = dfac.getConstantURI(subject.asOWLNamedIndividual().getIRI().toString());
			ValueConstant c2 = dfac.getConstantLiteral(object.getLiteral(), type);

			Description equivalent = null;
			if (equivalenceMap != null) {
				equivalent = equivalenceMap.get(p);
			}

			if (equivalent == null) {
				return ofac.createDataPropertyAssertion(p, c1, c2);
			} else {
				Property equiProp = (Property) equivalent;
				return ofac.createDataPropertyAssertion(equiProp.getPredicate(), c1, c2);
			}

		} else {
			return null;
		}
	}

	private Predicate.COL_TYPE getColumnType(OWLDatatype datatype) throws TranslationException {
		if (datatype == null) {
			return Predicate.COL_TYPE.LITERAL;
		}
		if (datatype.isString() || datatype.getBuiltInDatatype() == OWL2Datatype.XSD_STRING) { // xsd:string
			return Predicate.COL_TYPE.STRING;
		} else if (datatype.isRDFPlainLiteral() || datatype.getBuiltInDatatype() == OWL2Datatype.RDF_PLAIN_LITERAL // rdf:PlainLiteral
				|| datatype.getBuiltInDatatype() == OWL2Datatype.RDFS_LITERAL) { // rdfs:Literal
			return Predicate.COL_TYPE.LITERAL;
		} else if (datatype.isInteger()
				|| datatype.getBuiltInDatatype() == OWL2Datatype.XSD_INTEGER // xsd:integer
				|| datatype.getBuiltInDatatype() == OWL2Datatype.XSD_INT // xsd:int
				|| datatype.getBuiltInDatatype() == OWL2Datatype.XSD_LONG // xsd:long
				|| datatype.getBuiltInDatatype() == OWL2Datatype.XSD_POSITIVE_INTEGER
				|| datatype.getBuiltInDatatype() == OWL2Datatype.XSD_NEGATIVE_INTEGER
				|| datatype.getBuiltInDatatype() == OWL2Datatype.XSD_NON_POSITIVE_INTEGER
				|| datatype.getBuiltInDatatype() == OWL2Datatype.XSD_NON_NEGATIVE_INTEGER
				|| datatype.getBuiltInDatatype() == OWL2Datatype.XSD_UNSIGNED_INT
				|| datatype.getBuiltInDatatype() == OWL2Datatype.XSD_UNSIGNED_LONG) {
			return Predicate.COL_TYPE.INTEGER;
		} else if (datatype.getBuiltInDatatype() == OWL2Datatype.XSD_DECIMAL) { // xsd:decimal
			return Predicate.COL_TYPE.DECIMAL;
		} else if (datatype.isFloat() || datatype.isDouble() || datatype.getBuiltInDatatype() == OWL2Datatype.XSD_DOUBLE // xsd:double
				|| datatype.getBuiltInDatatype() == OWL2Datatype.XSD_FLOAT) { // xsd:float
			return Predicate.COL_TYPE.DOUBLE;
		} else if (datatype.getBuiltInDatatype() == OWL2Datatype.XSD_DATE_TIME) {
			return Predicate.COL_TYPE.DATETIME;
		} else if (datatype.isBoolean() || datatype.getBuiltInDatatype() == OWL2Datatype.XSD_BOOLEAN) { // xsd:boolean
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
