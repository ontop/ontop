package it.unibz.krdb.obda.owlapi3;

import it.unibz.krdb.obda.model.OBDADataFactory;
import it.unibz.krdb.obda.model.Predicate;
import it.unibz.krdb.obda.model.URIConstant;
import it.unibz.krdb.obda.model.ValueConstant;
import it.unibz.krdb.obda.model.impl.OBDADataFactoryImpl;
import it.unibz.krdb.obda.model.impl.OBDAVocabulary;
import it.unibz.krdb.obda.ontology.Assertion;
import it.unibz.krdb.obda.ontology.BasicClassDescription;
import it.unibz.krdb.obda.ontology.ClassDescription;
import it.unibz.krdb.obda.ontology.DataType;
import it.unibz.krdb.obda.ontology.Description;
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
import it.unibz.krdb.obda.ontology.impl.PunningException;
import it.unibz.krdb.obda.ontology.impl.SubClassAxiomImpl;
import it.unibz.krdb.obda.ontology.impl.SubPropertyAxiomImpl;

import java.net.URI;
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
import org.semanticweb.owlapi.vocab.OWL2Datatype;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class OWLAPI3Translator {

	private static final OBDADataFactory predicateFactory = OBDADataFactoryImpl.getInstance();
	private static final OntologyFactory descFactory = OntologyFactoryImpl.getInstance();;
	private HashSet<String> objectproperties = null;
	private HashSet<String> dataproperties = null;

	private final LanguageProfile profile = LanguageProfile.DLLITEA;

	private static final Logger log = LoggerFactory.getLogger(OWLAPI3Translator.class);

	private static final OntologyFactory ofac = OntologyFactoryImpl.getInstance();

	/*
	 * If we need to construct auxiliary subclass axioms for A ISA exists R.C we
	 * put them in this map to avoid generating too many auxiliary
	 * roles/classes.
	 */
	final Map<ClassDescription, List<SubDescriptionAxiom>> auxiliaryAssertions = new HashMap<ClassDescription, List<SubDescriptionAxiom>>();

	int auxRoleCounter = 0;

	public OWLAPI3Translator() {

		objectproperties = new HashSet<String>();
		dataproperties = new HashSet<String>();
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
		URI uri = URI.create("http://it.unibz.krdb.obda/Quest/auxiliaryontology");

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

	public Ontology translate(OWLOntology owl) throws PunningException {
		// ManchesterOWLSyntaxOWLObjectRendererImpl rend = new
		// ManchesterOWLSyntaxOWLObjectRendererImpl();

		Ontology dl_onto = descFactory.createOntology(owl.getOntologyID().getOntologyIRI().toURI());

		/*
		 * First we add all definitions for classes and roles
		 */
		Set<OWLEntity> entities = owl.getSignature();
		Iterator<OWLEntity> eit = entities.iterator();
		while (eit.hasNext()) {
			OWLEntity entity = eit.next();
			if (entity instanceof OWLClass) {

				/* We ignore TOP and BOTTOM (Thing and Nothing) */
				if (((OWLClass) entity).isOWLThing() || ((OWLClass) entity).isOWLNothing()) {
					continue;
				}
				URI uri = entity.getIRI().toURI();
				Predicate p = predicateFactory.getClassPredicate(uri);
				ClassDescription cd = descFactory.createClass(p);
				dl_onto.addConcept(p);

			} else if (entity instanceof OWLObjectProperty) {

				URI uri = entity.getIRI().toURI();
				objectproperties.add(uri.toString());
				Predicate p = predicateFactory.getObjectPropertyPredicate(uri);
				Property rd = descFactory.createProperty(p);
				if (dataproperties.contains(uri.toString())) {
					throw new PunningException("Please avoid using the same name for object and data properties.");
				} else {
					dl_onto.addRole(p);
				}

			} else if (entity instanceof OWLDataProperty) {
				URI uri = entity.getIRI().toURI();
				dataproperties.add(uri.toString());
				Predicate p = predicateFactory.getDataPropertyPredicate(uri);
				Property rd = descFactory.createProperty(p);
				if (objectproperties.contains(uri.toString())) {
					throw new PunningException("Please avoid using the same name for object and data properties.");
				} else {
					dl_onto.addRole(p);
				}
			}
		}

		Set<OWLAxiom> axioms = owl.getAxioms();
		Iterator<OWLAxiom> it = axioms.iterator();
		while (it.hasNext()) {
			OWLAxiom axiom = it.next();
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

					ClassDescription subclass = descFactory.getPropertySomeRestriction(role.getPredicate(), role.isInverse());
					List<ClassDescription> superDescriptions = getSuperclassExpressions(((OWLDataPropertyDomainAxiom) axiom).getDomain());

					addSubclassAxioms(dl_onto, subclass, superDescriptions);

				} else if (axiom instanceof OWLDataPropertyRangeAxiom) {

					OWLDataPropertyRangeAxiom aux = (OWLDataPropertyRangeAxiom) axiom;
					Property role = getRoleExpression(aux.getProperty());

					ClassDescription subclass = descFactory.getPropertySomeRestriction(role.getPredicate(), true);
					OWLDatatype rangeDatatype = aux.getRange().asOWLDatatype();

					Predicate.COL_TYPE columnType = getColumnType(rangeDatatype);
					DataType datatype = ofac.createDataType(getDataTypePredicate(columnType));
					addSubclassAxiom(dl_onto, subclass, datatype);

				} else if (axiom instanceof OWLSubDataPropertyOfAxiom) {

					OWLSubDataPropertyOfAxiom aux = (OWLSubDataPropertyOfAxiom) axiom;
					Property subrole = getRoleExpression(aux.getSubProperty());
					Property superrole = getRoleExpression(aux.getSuperProperty());

					SubPropertyAxiomImpl roleinc = (SubPropertyAxiomImpl) descFactory.createSubPropertyAxiom(subrole, superrole);

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
					PropertyFunctionalAxiom funct = descFactory.createPropertyFunctionalAxiom(role);

					dl_onto.addAssertion(funct);

				} else if (axiom instanceof OWLInverseObjectPropertiesAxiom) {
					if (profile.order() < LanguageProfile.OWL2QL.order())
						throw new TranslationException();

					OWLInverseObjectPropertiesAxiom aux = (OWLInverseObjectPropertiesAxiom) axiom;
					OWLObjectPropertyExpression exp1 = aux.getFirstProperty();
					OWLObjectPropertyExpression exp2 = aux.getSecondProperty();
					Property role1 = getRoleExpression(exp1);
					Property role2 = getRoleExpression(exp2);

					Property invrole1 = descFactory.createProperty(role1.getPredicate(), !role1.isInverse());
					Property invrole2 = descFactory.createProperty(role2.getPredicate(), !role2.isInverse());

					SubPropertyAxiomImpl inc1 = (SubPropertyAxiomImpl) descFactory.createSubPropertyAxiom(role1, invrole2);
					SubPropertyAxiomImpl inc2 = (SubPropertyAxiomImpl) descFactory.createSubPropertyAxiom(role2, invrole1);

					dl_onto.addAssertion(inc1);
					dl_onto.addAssertion(inc2);

				} else if (axiom instanceof OWLSymmetricObjectPropertyAxiom) {
					if (profile.order() < LanguageProfile.OWL2QL.order())
						throw new TranslationException();
					OWLSymmetricObjectPropertyAxiom aux = (OWLSymmetricObjectPropertyAxiom) axiom;
					OWLObjectPropertyExpression exp1 = aux.getProperty();
					Property role = getRoleExpression(exp1);
					Property invrole = descFactory.createProperty(role.getPredicate(), !role.isInverse());

					SubPropertyAxiomImpl symm = (SubPropertyAxiomImpl) descFactory.createSubPropertyAxiom(invrole, role);

					dl_onto.addAssertion(symm);

				} else if (axiom instanceof OWLObjectPropertyDomainAxiom) {

					OWLObjectPropertyDomainAxiom aux = (OWLObjectPropertyDomainAxiom) axiom;
					Property role = getRoleExpression(aux.getProperty());

					ClassDescription subclass = descFactory.getPropertySomeRestriction(role.getPredicate(), role.isInverse());
					List<ClassDescription> superDescriptions = getSuperclassExpressions(((OWLObjectPropertyDomainAxiom) axiom).getDomain());

					addSubclassAxioms(dl_onto, subclass, superDescriptions);

				} else if (axiom instanceof OWLObjectPropertyRangeAxiom) {

					OWLObjectPropertyRangeAxiom aux = (OWLObjectPropertyRangeAxiom) axiom;
					Property role = getRoleExpression(aux.getProperty());

					ClassDescription subclass = descFactory.getPropertySomeRestriction(role.getPredicate(), !role.isInverse());
					List<ClassDescription> superDescriptions = getSuperclassExpressions(((OWLObjectPropertyRangeAxiom) axiom).getRange());

					addSubclassAxioms(dl_onto, subclass, superDescriptions);

				} else if (axiom instanceof OWLSubObjectPropertyOfAxiom) {

					OWLSubObjectPropertyOfAxiom aux = (OWLSubObjectPropertyOfAxiom) axiom;
					Property subrole = getRoleExpression(aux.getSubProperty());
					Property superrole = getRoleExpression(aux.getSuperProperty());

					SubPropertyAxiomImpl roleinc = (SubPropertyAxiomImpl) descFactory.createSubPropertyAxiom(subrole, superrole);

					dl_onto.addAssertion(roleinc);

				} else if (axiom instanceof OWLFunctionalObjectPropertyAxiom) {
					if (profile.order() < LanguageProfile.OWL2QL.order())
						throw new TranslationException();
					OWLFunctionalObjectPropertyAxiom aux = (OWLFunctionalObjectPropertyAxiom) axiom;
					Property role = getRoleExpression(aux.getProperty());
					PropertyFunctionalAxiom funct = descFactory.createPropertyFunctionalAxiom(role);

					dl_onto.addAssertion(funct);

				} else if (axiom instanceof OWLInverseFunctionalObjectPropertyAxiom) {
					if (profile.order() < LanguageProfile.OWL2QL.order())
						throw new TranslationException();
					OWLInverseFunctionalObjectPropertyAxiom aux = (OWLInverseFunctionalObjectPropertyAxiom) axiom;
					Property role = getRoleExpression(aux.getProperty());
					Property invrole = descFactory.createProperty(role.getPredicate(), !role.isInverse());
					PropertyFunctionalAxiom funct = descFactory.createPropertyFunctionalAxiom(invrole);

					dl_onto.addAssertion(funct);

				} else if (axiom instanceof OWLIndividualAxiom) {
					/*
					 * Individual axioms are intentionally ignored by the
					 * translator
					 */
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
							dl_onto.addConcept(predicateFactory.getClassPredicate(uri));
						}
					} else if (entity instanceof OWLObjectProperty) {
						String uri = entity.asOWLObjectProperty().getIRI().toString();
						dl_onto.addRole(predicateFactory.getObjectPropertyPredicate(uri));
					} else if (entity instanceof OWLDataProperty) {
						String uri = entity.asOWLDataProperty().getIRI().toString();
						dl_onto.addRole(predicateFactory.getDataPropertyPredicate(uri));
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
					log.warn("WARNING ignoring axiom: {}", axiom.toString());
				}
			} catch (TranslationException e) {
				log.warn("WARNING ignoring axiom: {}", axiom.toString());
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
			SubClassAxiomImpl inc = (SubClassAxiomImpl) descFactory.createSubClassAxiom(subDescription, superDescription);
			dl_onto.addAssertion(inc);
		} else {
			log.debug("Generating encoding for {} subclassof {}", subDescription, superDescription);

			/*
			 * We found an existential, we need to get an auxiliary set of
			 * subClassAssertion
			 */
			List<SubDescriptionAxiom> aux = auxiliaryAssertions.get(superDescription);
			if (aux == null) {
				/*
				 * no aux subclass assertions found for this exists R.A,
				 * creating a new one
				 */
				PropertySomeClassRestriction eR = (PropertySomeClassRestriction) superDescription;
				Predicate role = eR.getPredicate();
				OClass filler = eR.getFiller();

				Property auxRole = descFactory.createProperty(predicateFactory.getObjectPropertyPredicate(URI
						.create(OntologyImpl.AUXROLEURI + auxRoleCounter)));
				auxRoleCounter += 1;

				/* Creating the new subrole assertions */
				SubPropertyAxiomImpl subrole = (SubPropertyAxiomImpl) descFactory.createSubPropertyAxiom(auxRole,
						descFactory.createProperty(role, eR.isInverse()));
				/* Creatin the range assertion */
				SubClassAxiomImpl subclass = (SubClassAxiomImpl) descFactory.createSubClassAxiom(
						descFactory.getPropertySomeRestriction(auxRole.getPredicate(), true), filler);
				aux = new LinkedList<SubDescriptionAxiom>();
				aux.add(subclass);
				aux.add(subrole);

				dl_onto.addAssertion(subclass);
				dl_onto.addAssertion(subrole);
			}

			SubPropertyAxiomImpl roleinclusion = (SubPropertyAxiomImpl) aux.get(1);
			Property role = roleinclusion.getSub();
			PropertySomeRestriction domain = descFactory.getPropertySomeRestriction(role.getPredicate(), false);
			/* Taking the domain of the aux role as the including */
			SubClassAxiomImpl inc = (SubClassAxiomImpl) descFactory.createSubClassAxiom(subDescription, domain);
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
			role = descFactory.createObjectProperty(rolExpression.asOWLObjectProperty().getIRI().toURI());
		} else if (rolExpression instanceof OWLObjectInverseOf) {
			if (profile.order() < LanguageProfile.OWL2QL.order())
				throw new TranslationException();
			OWLObjectInverseOf aux = (OWLObjectInverseOf) rolExpression;
			role = descFactory.createProperty(
					this.predicateFactory.getObjectPropertyPredicate(aux.getInverse().asOWLObjectProperty().getIRI().toURI()), true);
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
				SubClassAxiomImpl inclusion1 = (SubClassAxiomImpl) descFactory.createSubClassAxiom(subclass, superclass);
				SubClassAxiomImpl inclusion2 = (SubClassAxiomImpl) descFactory.createSubClassAxiom(superclass, subclass);
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
				SubPropertyAxiomImpl inclusion1 = (SubPropertyAxiomImpl) descFactory.createSubPropertyAxiom(subrole, superole);
				SubPropertyAxiomImpl inclusion2 = (SubPropertyAxiomImpl) descFactory.createSubPropertyAxiom(superole, subrole);
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
			role = descFactory.createDataProperty(rolExpression.asOWLDataProperty().getIRI().toURI());
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
				ClassDescription cd = descFactory.createPropertySomeClassRestriction(role.getPredicate(), role.isInverse(),
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
				ClassDescription cd = descFactory.getPropertySomeRestriction(role.getPredicate(), role.isInverse());
				result.add(cd);
			} else if (filler instanceof OWLDatatype) {
				Property role = getRoleExpression(property);
				ClassDescription cd = descFactory.createPropertySomeDataTypeRestriction(role.getPredicate(), role.isInverse(),
						getDataTypeExpression(filler));
				result.add(cd);
			}
		} else {
			result.add(getSubclassExpression(owlExpression));
		}
		return result;
	}

	private DataType getDataTypeExpression(OWLDataRange filler) throws TranslationException {
		OWLDatatype datatype = (OWLDatatype) filler;
		switch(getColumnType(datatype)) {
			case LITERAL: return descFactory.createDataType(OBDAVocabulary.RDFS_LITERAL);
			case STRING: return descFactory.createDataType(OBDAVocabulary.XSD_STRING);
			case INTEGER: return descFactory.createDataType(OBDAVocabulary.XSD_INTEGER);
			case DOUBLE: return descFactory.createDataType(OBDAVocabulary.XSD_DOUBLE);
			case DATETIME: return descFactory.createDataType(OBDAVocabulary.XSD_DATETIME);
			case BOOLEAN: return descFactory.createDataType(OBDAVocabulary.XSD_BOOLEAN);
			default: return null;
		}
	}

	private ClassDescription getSubclassExpression(OWLClassExpression owlExpression) throws TranslationException {
		ClassDescription cd = null;
		if (owlExpression instanceof OWLClass) {
			URI uri = ((OWLClass) owlExpression).getIRI().toURI();
			Predicate p = predicateFactory.getClassPredicate(uri);
			cd = descFactory.createClass(p);

		} else if (owlExpression instanceof OWLDataMinCardinality) {
			if (profile.order() < LanguageProfile.DLLITEA.order())
				throw new TranslationException();
			OWLDataMinCardinality rest = (OWLDataMinCardinality) owlExpression;
			int cardinatlity = rest.getCardinality();
			OWLDataRange range = rest.getFiller();
			if (cardinatlity != 1 || range != null) {
				throw new TranslationException();
			}
			URI uri = rest.getProperty().asOWLDataProperty().getIRI().toURI();
			Predicate attribute = predicateFactory.getDataPropertyPredicate(uri);
			cd = descFactory.getPropertySomeRestriction(attribute, false);

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
			URI uri = propExp.getNamedProperty().getIRI().toURI();
			Predicate role = predicateFactory.getObjectPropertyPredicate(uri);

			if (propExp instanceof OWLInverseObjectPropertiesAxiom) {
				cd = descFactory.getPropertySomeRestriction(role, true);
			} else {
				cd = descFactory.getPropertySomeRestriction(role, false);
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
			URI uri = propExp.getNamedProperty().getIRI().toURI();
			Predicate role = predicateFactory.getObjectPropertyPredicate(uri);

			if (propExp instanceof OWLObjectInverseOf) {
				cd = descFactory.getPropertySomeRestriction(role, true);
			} else {
				cd = descFactory.getPropertySomeRestriction(role, false);
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
			URI uri = propExp.getIRI().toURI();
			Predicate role = predicateFactory.getDataPropertyPredicate(uri);
			cd = descFactory.getPropertySomeRestriction(role, false);
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

			Predicate classproperty = predicateFactory.getClassPredicate(namedclass.getIRI().toURI());
			URIConstant c = predicateFactory.getURIConstant(indv.asOWLNamedIndividual().getIRI().toURI());

			Description desc = ofac.createClass(classproperty);
			Description equivalent = null;
			if (equivalenceMap != null)
				equivalent = equivalenceMap.get(classproperty);

			if (equivalent == null)
				return descFactory.createClassAssertion(classproperty, c);
			else {
				return descFactory.createClassAssertion(((OClass) equivalent).getPredicate(), c);
			}

		} else if (axiom instanceof OWLObjectPropertyAssertionAxiom) {

			/*
			 * Role assertions
			 */

			OWLObjectPropertyAssertionAxiom assertion = (OWLObjectPropertyAssertionAxiom) axiom;
			OWLObjectPropertyExpression propertyExperssion = assertion.getProperty();

			URI property = null;
			OWLIndividual subject = null;
			OWLIndividual object = null;

			if (propertyExperssion instanceof OWLObjectProperty) {
				OWLObjectProperty namedclass = (OWLObjectProperty) propertyExperssion;
				property = namedclass.getIRI().toURI();

				subject = assertion.getSubject();
				object = assertion.getObject();

			} else if (propertyExperssion instanceof OWLObjectInverseOf) {
				OWLObjectProperty namedclass = ((OWLObjectInverseOf) propertyExperssion).getInverse().getNamedProperty();
				property = namedclass.getIRI().toURI();
				subject = assertion.getObject();
				object = assertion.getSubject();
			}

			if (subject.isAnonymous()) {
				throw new RuntimeException("Found anonymous individual, this feature is not supported");
			}

			if (object.isAnonymous()) {
				throw new RuntimeException("Found anonymous individual, this feature is not supported");
			}
			Predicate p = predicateFactory.getObjectPropertyPredicate(property);
			URIConstant c1 = predicateFactory.getURIConstant(subject.asOWLNamedIndividual().getIRI().toURI());
			URIConstant c2 = predicateFactory.getURIConstant(object.asOWLNamedIndividual().getIRI().toURI());

			Description desc = ofac.createProperty(p);
			Description equivalent = null;
			if (equivalenceMap != null)
				equivalent = equivalenceMap.get(p);

			if (equivalent == null)
				return descFactory.createObjectPropertyAssertion(p, c1, c2);
			else {
				Property equiProp = (Property) equivalent;
				if (!equiProp.isInverse()) {
					return descFactory.createObjectPropertyAssertion(equiProp.getPredicate(), c1, c2);
				} else {
					return descFactory.createObjectPropertyAssertion(equiProp.getPredicate(), c2, c1);
				}
			}

		} else if (axiom instanceof OWLDataPropertyAssertionAxiom) {

			/*
			 * Attribute assertions
			 */

			OWLDataPropertyAssertionAxiom assertion = (OWLDataPropertyAssertionAxiom) axiom;
			OWLDataProperty propertyExperssion = (OWLDataProperty) assertion.getProperty();

			URI property = propertyExperssion.getIRI().toURI();
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

			Predicate p = predicateFactory.getDataPropertyPredicate(property);
			URIConstant c1 = predicateFactory.getURIConstant(subject.asOWLNamedIndividual().getIRI().toURI());
			ValueConstant c2 = predicateFactory.getValueConstant(object.getLiteral(), type);

			Description equivalent = null;
			if (equivalenceMap != null) {
				equivalent = equivalenceMap.get(p);
			}

			if (equivalent == null) {
				return descFactory.createDataPropertyAssertion(p, c1, c2);
			} else {
				Property equiProp = (Property) equivalent;
				return descFactory.createDataPropertyAssertion(equiProp.getPredicate(), c1, c2);
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
				|| datatype.getBuiltInDatatype() == OWL2Datatype.RDF_XML_LITERAL // rdf:XmlLiteral
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

	private Predicate getDataTypePredicate(Predicate.COL_TYPE type) {
		switch(type) {
			case LITERAL: return OBDAVocabulary.RDFS_LITERAL;
			case STRING: return OBDAVocabulary.XSD_STRING;
			case INTEGER: return OBDAVocabulary.XSD_INTEGER;
			case DOUBLE: return OBDAVocabulary.XSD_DOUBLE;
			case DATETIME: return OBDAVocabulary.XSD_DATETIME;
			case BOOLEAN: return OBDAVocabulary.XSD_BOOLEAN;
			default: 
				return OBDAVocabulary.RDFS_LITERAL;
		}
	}
}
