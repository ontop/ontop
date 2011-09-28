package it.unibz.krdb.obda.owlrefplatform.core.translator;

import it.unibz.krdb.obda.model.OBDADataFactory;
import it.unibz.krdb.obda.model.Predicate;
import it.unibz.krdb.obda.model.URIConstant;
import it.unibz.krdb.obda.model.ValueConstant;
import it.unibz.krdb.obda.model.impl.OBDADataFactoryImpl;
import it.unibz.krdb.obda.owlrefplatform.core.ontology.Assertion;
import it.unibz.krdb.obda.owlrefplatform.core.ontology.ClassDescription;
import it.unibz.krdb.obda.owlrefplatform.core.ontology.Description;
import it.unibz.krdb.obda.owlrefplatform.core.ontology.LanguageProfile;
import it.unibz.krdb.obda.owlrefplatform.core.ontology.OClass;
import it.unibz.krdb.obda.owlrefplatform.core.ontology.Ontology;
import it.unibz.krdb.obda.owlrefplatform.core.ontology.OntologyFactory;
import it.unibz.krdb.obda.owlrefplatform.core.ontology.Property;
import it.unibz.krdb.obda.owlrefplatform.core.ontology.PropertyFunctionalAxiom;
import it.unibz.krdb.obda.owlrefplatform.core.ontology.PropertySomeClassRestriction;
import it.unibz.krdb.obda.owlrefplatform.core.ontology.PropertySomeRestriction;
import it.unibz.krdb.obda.owlrefplatform.core.ontology.SubDescriptionAxiom;
import it.unibz.krdb.obda.owlrefplatform.core.ontology.imp.OntologyFactoryImpl;
import it.unibz.krdb.obda.owlrefplatform.core.ontology.imp.SubClassAxiomImpl;
import it.unibz.krdb.obda.owlrefplatform.core.ontology.imp.SubPropertyAxiomImpl;

import java.net.URI;
import java.util.Collection;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.semanticweb.owl.model.OWLAnnotationAxiom;
import org.semanticweb.owl.model.OWLAxiom;
import org.semanticweb.owl.model.OWLClass;
import org.semanticweb.owl.model.OWLClassAssertionAxiom;
import org.semanticweb.owl.model.OWLConstant;
import org.semanticweb.owl.model.OWLDataMinCardinalityRestriction;
import org.semanticweb.owl.model.OWLDataProperty;
import org.semanticweb.owl.model.OWLDataPropertyAssertionAxiom;
import org.semanticweb.owl.model.OWLDataPropertyDomainAxiom;
import org.semanticweb.owl.model.OWLDataPropertyExpression;
import org.semanticweb.owl.model.OWLDataPropertyRangeAxiom;
import org.semanticweb.owl.model.OWLDataRange;
import org.semanticweb.owl.model.OWLDataSomeRestriction;
import org.semanticweb.owl.model.OWLDataSubPropertyAxiom;
import org.semanticweb.owl.model.OWLDescription;
import org.semanticweb.owl.model.OWLEntity;
import org.semanticweb.owl.model.OWLEquivalentClassesAxiom;
import org.semanticweb.owl.model.OWLEquivalentDataPropertiesAxiom;
import org.semanticweb.owl.model.OWLEquivalentObjectPropertiesAxiom;
import org.semanticweb.owl.model.OWLFunctionalDataPropertyAxiom;
import org.semanticweb.owl.model.OWLFunctionalObjectPropertyAxiom;
import org.semanticweb.owl.model.OWLImportsDeclaration;
import org.semanticweb.owl.model.OWLIndividual;
import org.semanticweb.owl.model.OWLIndividualAxiom;
import org.semanticweb.owl.model.OWLInverseFunctionalObjectPropertyAxiom;
import org.semanticweb.owl.model.OWLInverseObjectPropertiesAxiom;
import org.semanticweb.owl.model.OWLObjectIntersectionOf;
import org.semanticweb.owl.model.OWLObjectMinCardinalityRestriction;
import org.semanticweb.owl.model.OWLObjectProperty;
import org.semanticweb.owl.model.OWLObjectPropertyAssertionAxiom;
import org.semanticweb.owl.model.OWLObjectPropertyDomainAxiom;
import org.semanticweb.owl.model.OWLObjectPropertyExpression;
import org.semanticweb.owl.model.OWLObjectPropertyInverse;
import org.semanticweb.owl.model.OWLObjectPropertyRangeAxiom;
import org.semanticweb.owl.model.OWLObjectSomeRestriction;
import org.semanticweb.owl.model.OWLObjectSubPropertyAxiom;
import org.semanticweb.owl.model.OWLOntology;
import org.semanticweb.owl.model.OWLSubClassAxiom;
import org.semanticweb.owl.model.OWLSymmetricObjectPropertyAxiom;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class OWLAPI2Translator {

	private OBDADataFactory									predicateFactory	= null;
	private OntologyFactory									descFactory			= null;
	private HashSet<String>									objectproperties	= null;
	private HashSet<String>									dataproperties		= null;

	private final LanguageProfile							profile				= LanguageProfile.DLLITEA;

	Logger													log					= LoggerFactory.getLogger(OWLAPI2Translator.class);

	public static String									AUXROLEURI			= "ER.A-AUXROLE";

	OntologyFactory											ofac				= OntologyFactoryImpl.getInstance();

	/*
	 * If we need to construct auxiliary subclass axioms for A ISA exists R.C we
	 * put them in this map to avoid generating too many auxiliary
	 * roles/classes.
	 */
	final Map<ClassDescription, List<SubDescriptionAxiom>>	auxiliaryAssertions	= new HashMap<ClassDescription, List<SubDescriptionAxiom>>();

	int														auxRoleCounter		= 0;

	public OWLAPI2Translator() {
		predicateFactory = OBDADataFactoryImpl.getInstance();
		descFactory = new OntologyFactoryImpl();
		objectproperties = new HashSet<String>();
		dataproperties = new HashSet<String>();
	}

	public Ontology translate(OWLOntology owl) throws Exception {
		// ManchesterOWLSyntaxOWLObjectRendererImpl rend = new
		// ManchesterOWLSyntaxOWLObjectRendererImpl();

		Ontology dl_onto = descFactory.createOntology(owl.getURI());

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
				URI uri = entity.getURI();
				Predicate p = predicateFactory.getClassPredicate(uri);
				ClassDescription cd = descFactory.createClass(p);
				dl_onto.addConcept(p);

			} else if (entity instanceof OWLObjectProperty) {

				URI uri = entity.getURI();
				objectproperties.add(uri.toString());
				Predicate p = predicateFactory.getObjectPropertyPredicate(uri);
				Property rd = descFactory.createProperty(p);
				if (dataproperties.contains(uri.toString())) {
					throw new Exception("Please avoid using the same name for object and data properties.");
				} else {
					dl_onto.addRole(p);
				}

			} else if (entity instanceof OWLDataProperty) {
				URI uri = entity.getURI();
				dataproperties.add(uri.toString());
				Predicate p = predicateFactory.getDataPropertyPredicate(uri);
				Property rd = descFactory.createProperty(p);
				if (objectproperties.contains(uri.toString())) {
					throw new Exception("Please avoid using the same name for object and data properties.");
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
					Set<OWLDescription> equivalents = aux.getDescriptions();
					List<ClassDescription> vec = getSubclassExpressions(equivalents);

					addConceptEquivalences(dl_onto, vec);

				} else if (axiom instanceof OWLSubClassAxiom) {

					OWLSubClassAxiom aux = (OWLSubClassAxiom) axiom;
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
					OWLDataRange range = aux.getRange();
					if (!range.isDataType()) {
						throw new TranslationException("Unsupported data range: " + range.toString());
					}
					if (!range.isTopDataType()) {
						log.warn("WARNING: At the moment Quest only rdfs:Literal. Offending axiom: {}", axiom.toString());

					}

				} else if (axiom instanceof OWLDataSubPropertyAxiom) {

					OWLDataSubPropertyAxiom aux = (OWLDataSubPropertyAxiom) axiom;
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

				} else if (axiom instanceof OWLObjectSubPropertyAxiom) {

					OWLObjectSubPropertyAxiom aux = (OWLObjectSubPropertyAxiom) axiom;
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
				} else if (axiom instanceof OWLImportsDeclaration) {
					/*
					 * Imports
					 */
				} else {
					log.warn("WARNING ignoring axiom: {}", axiom.toString());
				}
			} catch (TranslationException e) {
				log.warn("WARNING ignoring axiom: {}", axiom.toString());
			}
		}
		return dl_onto;
	}

	/**
	 * @param dl_onto
	 * @param subDescription
	 * @param superDescriptions
	 */
	private void addSubclassAxioms(Ontology dl_onto, ClassDescription subDescription, List<ClassDescription> superDescriptions) {
		for (ClassDescription superDescription : superDescriptions) {
			if (superDescription == null || subDescription == null) {
				log.warn("NULL: {} {}", subDescription, superDescription);
			}

			if (superDescription instanceof ClassDescription) {

				/* We ignore TOP and BOTTOM (Thing and Nothing) */
				if ((superDescription instanceof OClass)
						&& ((OClass) superDescription).toString().equals("http://www.w3.org/2002/07/owl#Thing")) {
					continue;
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

					Property auxRole = descFactory.createProperty(predicateFactory.getObjectPropertyPredicate(URI.create(AUXROLEURI
							+ auxRoleCounter)));
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
	}

	private List<ClassDescription> getSubclassExpressions(Collection<OWLDescription> owlExpressions) throws TranslationException {
		List<ClassDescription> descriptions = new LinkedList<ClassDescription>();
		for (OWLDescription owlDescription : owlExpressions) {
			descriptions.add(getSubclassExpression(owlDescription));
		}
		return descriptions;
	}

	private Property getRoleExpression(OWLObjectPropertyExpression rolExpression) throws TranslationException {
		Property role = null;

		if (rolExpression instanceof OWLObjectProperty) {
			role = descFactory.createObjectProperty(rolExpression.asOWLObjectProperty().getURI());
		} else if (rolExpression instanceof OWLObjectPropertyInverse) {
			if (profile.order() < LanguageProfile.OWL2QL.order())
				throw new TranslationException();
			OWLObjectPropertyInverse aux = (OWLObjectPropertyInverse) rolExpression;
			role = descFactory.createProperty(
					this.predicateFactory.getObjectPropertyPredicate(aux.getInverse().asOWLObjectProperty().getURI()), true);
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
			role = descFactory.createDataProperty(rolExpression.asOWLDataProperty().getURI());
		} else {
			throw new TranslationException();
		}
		return role;

	}

	private List<ClassDescription> getSuperclassExpressions(OWLDescription owlExpression) throws TranslationException {
		List<ClassDescription> result = new LinkedList<ClassDescription>();
		if (owlExpression instanceof OWLObjectIntersectionOf) {
			if (profile.order() < LanguageProfile.OWL2QL.order())
				throw new TranslationException();
			OWLObjectIntersectionOf intersection = (OWLObjectIntersectionOf) owlExpression;
			Set<OWLDescription> operands = intersection.getOperands();
			for (OWLDescription operand : operands) {
				result.addAll(getSuperclassExpressions(operand));
			}
		} else if (owlExpression instanceof OWLObjectSomeRestriction) {
			if (profile.order() < LanguageProfile.OWL2QL.order())
				throw new TranslationException();

			OWLObjectSomeRestriction someexp = (OWLObjectSomeRestriction) owlExpression;
			OWLObjectPropertyExpression property = someexp.getProperty();
			OWLDescription filler = someexp.getFiller();
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

		} else if (owlExpression instanceof OWLDataSomeRestriction) {
			if (profile.order() < LanguageProfile.OWL2QL.order())
				throw new TranslationException();

			OWLDataSomeRestriction someexp = (OWLDataSomeRestriction) owlExpression;
			OWLDataPropertyExpression property = someexp.getProperty();
			OWLDataRange filler = someexp.getFiller();
			if (!(filler.isTopDataType())) {
				log.warn("WARNING: Found typed existential data property. Typing will be ignored: {}", owlExpression.toString());
			}
			Property role = getRoleExpression(property);

			ClassDescription cd = descFactory.getPropertySomeRestriction(role.getPredicate(), role.isInverse());
			result.add(cd);

		} else {
			result.add(getSubclassExpression(owlExpression));
		}
		return result;

	}

	private ClassDescription getSubclassExpression(OWLDescription owlExpression) throws TranslationException {
		ClassDescription cd = null;
		if (owlExpression instanceof OWLClass) {
			URI uri = ((OWLClass) owlExpression).getURI();
			Predicate p = predicateFactory.getClassPredicate(uri);
			cd = descFactory.createClass(p);

		} else if (owlExpression instanceof OWLDataMinCardinalityRestriction) {
			if (profile.order() < LanguageProfile.DLLITEA.order())
				throw new TranslationException();
			OWLDataMinCardinalityRestriction rest = (OWLDataMinCardinalityRestriction) owlExpression;
			int cardinatlity = rest.getCardinality();
			OWLDataRange range = rest.getFiller();
			if (cardinatlity != 1 || range != null) {
				throw new TranslationException();
			}
			URI uri = rest.getProperty().asOWLDataProperty().getURI();
			Predicate attribute = predicateFactory.getDataPropertyPredicate(uri);
			cd = descFactory.getPropertySomeRestriction(attribute, false);

		} else if (owlExpression instanceof OWLObjectMinCardinalityRestriction) {
			if (profile.order() < LanguageProfile.DLLITEA.order())
				throw new TranslationException();
			OWLObjectMinCardinalityRestriction rest = (OWLObjectMinCardinalityRestriction) owlExpression;
			int cardinatlity = rest.getCardinality();
			OWLDescription filler = rest.getFiller();
			if (cardinatlity != 1) {
				throw new TranslationException();
			}

			if (!filler.isOWLThing()) {
				throw new TranslationException();
			}
			OWLObjectPropertyExpression propExp = rest.getProperty();
			URI uri = propExp.getNamedProperty().getURI();
			Predicate role = predicateFactory.getObjectPropertyPredicate(uri);

			if (propExp instanceof OWLObjectPropertyInverse) {
				cd = descFactory.getPropertySomeRestriction(role, true);
			} else {
				cd = descFactory.getPropertySomeRestriction(role, false);
			}

		} else if (owlExpression instanceof OWLObjectSomeRestriction) {
			if (profile.order() < LanguageProfile.OWL2QL.order())
				throw new TranslationException();
			OWLObjectSomeRestriction rest = (OWLObjectSomeRestriction) owlExpression;
			OWLDescription filler = rest.getFiller();

			if (!filler.isOWLThing()) {
				throw new TranslationException();
			}
			OWLObjectPropertyExpression propExp = rest.getProperty();
			URI uri = propExp.getNamedProperty().getURI();
			Predicate role = predicateFactory.getObjectPropertyPredicate(uri);

			if (propExp instanceof OWLObjectPropertyInverse) {
				cd = descFactory.getPropertySomeRestriction(role, true);
			} else {
				cd = descFactory.getPropertySomeRestriction(role, false);
			}
		} else if (owlExpression instanceof OWLDataSomeRestriction) {
			if (profile.order() < LanguageProfile.OWL2QL.order())
				throw new TranslationException();
			OWLDataSomeRestriction rest = (OWLDataSomeRestriction) owlExpression;
			OWLDataRange filler = rest.getFiller();

			if (!filler.isTopDataType()) {
				throw new TranslationException();
			}
			OWLDataProperty propExp = (OWLDataProperty) rest.getProperty();
			URI uri = propExp.getURI();
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
		private static final long	serialVersionUID	= 7917688953760608030L;

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
			OWLDescription classExpression = assertion.getDescription();
			if (!(classExpression instanceof OWLClass) || classExpression.isOWLThing() || classExpression.isOWLNothing())
				return null;

			OWLClass namedclass = (OWLClass) classExpression;
			OWLIndividual indv = assertion.getIndividual();

			Predicate classproperty = predicateFactory.getClassPredicate(namedclass.getURI());
			URIConstant c = predicateFactory.getURIConstant(indv.getURI());

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
				property = namedclass.getURI();

				subject = assertion.getSubject();
				object = assertion.getObject();

			} else if (propertyExperssion instanceof OWLObjectPropertyInverse) {
				OWLObjectProperty namedclass = ((OWLObjectPropertyInverse) propertyExperssion).getInverse().getNamedProperty();
				property = namedclass.getURI();
				subject = assertion.getObject();
				object = assertion.getSubject();
			}
			Predicate p = predicateFactory.getObjectPropertyPredicate(property);
			URIConstant c1 = predicateFactory.getURIConstant(subject.getURI());
			URIConstant c2 = predicateFactory.getURIConstant(object.getURI());

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

			URI property = null;
			OWLIndividual subject = null;
			OWLConstant object = null;

			property = propertyExperssion.getURI();
			subject = assertion.getSubject();
			object = assertion.getObject();

			Predicate p = predicateFactory.getDataPropertyPredicate(property);
			URIConstant c1 = predicateFactory.getURIConstant(subject.getURI());
			ValueConstant c2 = predicateFactory.getValueConstant(object.getLiteral());

			Description desc = ofac.createProperty(p);
			Description equivalent = null;
			if (equivalenceMap != null)
				equivalent = equivalenceMap.get(p);

			if (equivalent == null)
				return descFactory.createDataPropertyAssertion(p, c1, c2);
			else {
				Property equiProp = (Property) equivalent;
				return descFactory.createDataPropertyAssertion(equiProp.getPredicate(), c1, c2);
			}

		} else {
			return null;
		}
	}
}
