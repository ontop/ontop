package it.unibz.krdb.obda.owlrefplatform.core.ontology.imp;

import it.unibz.krdb.obda.model.OBDADataFactory;
import it.unibz.krdb.obda.model.Predicate;
import it.unibz.krdb.obda.model.impl.OBDADataFactoryImpl;
import it.unibz.krdb.obda.owlrefplatform.core.ontology.AtomicConceptDescription;
import it.unibz.krdb.obda.owlrefplatform.core.ontology.ConceptDescription;
import it.unibz.krdb.obda.owlrefplatform.core.ontology.DLLiterOntology;
import it.unibz.krdb.obda.owlrefplatform.core.ontology.DescriptionFactory;
import it.unibz.krdb.obda.owlrefplatform.core.ontology.ExistentialConceptDescription;
import it.unibz.krdb.obda.owlrefplatform.core.ontology.FunctionalRoleAssertion;
import it.unibz.krdb.obda.owlrefplatform.core.ontology.LanguageProfile;
import it.unibz.krdb.obda.owlrefplatform.core.ontology.PositiveInclusion;
import it.unibz.krdb.obda.owlrefplatform.core.ontology.QualifiedExistentialConceptDescription;
import it.unibz.krdb.obda.owlrefplatform.core.ontology.RoleDescription;

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
import org.semanticweb.owl.model.OWLDataMinCardinalityRestriction;
import org.semanticweb.owl.model.OWLDataProperty;
import org.semanticweb.owl.model.OWLDataPropertyDomainAxiom;
import org.semanticweb.owl.model.OWLDataPropertyExpression;
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
import org.semanticweb.owl.model.OWLIndividualAxiom;
import org.semanticweb.owl.model.OWLInverseFunctionalObjectPropertyAxiom;
import org.semanticweb.owl.model.OWLInverseObjectPropertiesAxiom;
import org.semanticweb.owl.model.OWLObjectIntersectionOf;
import org.semanticweb.owl.model.OWLObjectMinCardinalityRestriction;
import org.semanticweb.owl.model.OWLObjectProperty;
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

public class OWLAPITranslator {

	private OBDADataFactory									predicateFactory	= null;
	private DescriptionFactory								descFactory			= null;
	private HashSet<String>									objectproperties	= null;
	private HashSet<String>									dataproperties		= null;

	private final LanguageProfile							profile				= LanguageProfile.DLLITEA;

	Logger													log					= LoggerFactory.getLogger(OWLAPITranslator.class);

	public static String									AUXROLEURI			= "ER.A-AUXROLE";

	/*
	 * If we need to construct auxiliary subclass axioms for A ISA exists R.C we
	 * put them in this map to avoid generating too many auxiliary
	 * roles/classes.
	 */
	final Map<ConceptDescription, List<PositiveInclusion>>	auxiliaryAssertions	= new HashMap<ConceptDescription, List<PositiveInclusion>>();

	int														auxRoleCounter		= 0;

	public OWLAPITranslator() {
		predicateFactory = OBDADataFactoryImpl.getInstance();
		descFactory = new BasicDescriptionFactory();
		objectproperties = new HashSet<String>();
		dataproperties = new HashSet<String>();
	}

	public DLLiterOntology translate(OWLOntology owl) throws Exception {
		// ManchesterOWLSyntaxOWLObjectRendererImpl rend = new
		// ManchesterOWLSyntaxOWLObjectRendererImpl();

		DLLiterOntology dl_onto = new DLLiterOntologyImpl(owl.getURI());

		Set<OWLEntity> entities = owl.getSignature();
		Iterator<OWLEntity> eit = entities.iterator();
		while (eit.hasNext()) {
			OWLEntity entity = eit.next();
			if (entity instanceof OWLClass) {

				URI uri = entity.getURI();
				Predicate p = predicateFactory.getPredicate(uri, 1);
				ConceptDescription cd = descFactory.getAtomicConceptDescription(p);
				dl_onto.addConcept(cd);

			} else if (entity instanceof OWLObjectProperty) {

				URI uri = entity.getURI();
				objectproperties.add(uri.toString());
				Predicate p = predicateFactory.getPredicate(uri, 2);
				RoleDescription rd = descFactory.getRoleDescription(p);
				if (dataproperties.contains(uri.toString())) {
					throw new Exception("Please avoid using the same name for object and data properties.");
				} else {
					dl_onto.addRole(rd);
				}

			} else if (entity instanceof OWLDataProperty) {
				URI uri = entity.getURI();
				dataproperties.add(uri.toString());
				Predicate p = predicateFactory.getPredicate(uri, 2);
				RoleDescription rd = descFactory.getRoleDescription(p);
				if (objectproperties.contains(uri.toString())) {
					throw new Exception("Please avoid using the same name for object and data properties.");
				} else {
					dl_onto.addRole(rd);
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
					List<ConceptDescription> vec = getSubclassExpressions(equivalents);

					addConceptEquivalences(dl_onto, vec);

				} else if (axiom instanceof OWLSubClassAxiom) {

					OWLSubClassAxiom aux = (OWLSubClassAxiom) axiom;
					ConceptDescription subDescription = getSubclassExpression(aux.getSubClass());
					List<ConceptDescription> superDescriptions = getSuperclassExpressions(aux.getSuperClass());

					addSubclassAxioms(dl_onto, subDescription, superDescriptions);

				} else if (axiom instanceof OWLDataPropertyDomainAxiom) {

					OWLDataPropertyDomainAxiom aux = (OWLDataPropertyDomainAxiom) axiom;
					RoleDescription role = getRoleExpression(aux.getProperty());

					ConceptDescription subclass = descFactory.getExistentialConceptDescription(role.getPredicate(), role.isInverse());
					List<ConceptDescription> superDescriptions = getSuperclassExpressions(((OWLDataPropertyDomainAxiom) axiom).getDomain());

					addSubclassAxioms(dl_onto, subclass, superDescriptions);

				} else if (axiom instanceof OWLDataSubPropertyAxiom) {

					OWLDataSubPropertyAxiom aux = (OWLDataSubPropertyAxiom) axiom;
					RoleDescription subrole = getRoleExpression(aux.getSubProperty());
					RoleDescription superrole = getRoleExpression(aux.getSuperProperty());

					DLLiterRoleInclusionImpl roleinc = new DLLiterRoleInclusionImpl(subrole, superrole);

					dl_onto.addAssertion(roleinc);

				} else if (axiom instanceof OWLEquivalentDataPropertiesAxiom) {

					if (profile.order() < LanguageProfile.OWL2QL.order())
						throw new TranslationException();

					OWLEquivalentDataPropertiesAxiom aux = (OWLEquivalentDataPropertiesAxiom) axiom;
					List<RoleDescription> vec = getDataRoleExpressions(aux.getProperties());
					addRoleEquivalences(dl_onto, vec);

				} else if (axiom instanceof OWLEquivalentObjectPropertiesAxiom) {

					if (profile.order() < LanguageProfile.OWL2QL.order())
						throw new TranslationException();

					OWLEquivalentObjectPropertiesAxiom aux = (OWLEquivalentObjectPropertiesAxiom) axiom;
					List<RoleDescription> vec = getObjectRoleExpressions(aux.getProperties());
					addRoleEquivalences(dl_onto, vec);

				} else if (axiom instanceof OWLFunctionalDataPropertyAxiom) {
					if (profile.order() < LanguageProfile.DLLITEA.order())
						throw new TranslationException();
					OWLFunctionalDataPropertyAxiom aux = (OWLFunctionalDataPropertyAxiom) axiom;
					RoleDescription role = getRoleExpression(aux.getProperty());
					FunctionalRoleAssertion funct = new FunctionalRoleAssertionImpl(role);

					dl_onto.addAssertion(funct);

				} else if (axiom instanceof OWLInverseObjectPropertiesAxiom) {
					if (profile.order() < LanguageProfile.OWL2QL.order())
						throw new TranslationException();

					OWLInverseObjectPropertiesAxiom aux = (OWLInverseObjectPropertiesAxiom) axiom;
					OWLObjectPropertyExpression exp1 = aux.getFirstProperty();
					OWLObjectPropertyExpression exp2 = aux.getSecondProperty();
					RoleDescription role1 = getRoleExpression(exp1);
					RoleDescription role2 = getRoleExpression(exp2);

					RoleDescription invrole1 = descFactory.getRoleDescription(role1.getPredicate(), !role1.isInverse());
					RoleDescription invrole2 = descFactory.getRoleDescription(role2.getPredicate(), !role2.isInverse());

					DLLiterRoleInclusionImpl inc1 = new DLLiterRoleInclusionImpl(role1, invrole2);
					DLLiterRoleInclusionImpl inc2 = new DLLiterRoleInclusionImpl(role2, invrole1);

					dl_onto.addAssertion(inc1);
					dl_onto.addAssertion(inc2);

				} else if (axiom instanceof OWLSymmetricObjectPropertyAxiom) {
					if (profile.order() < LanguageProfile.OWL2QL.order())
						throw new TranslationException();
					OWLSymmetricObjectPropertyAxiom aux = (OWLSymmetricObjectPropertyAxiom) axiom;
					OWLObjectPropertyExpression exp1 = aux.getProperty();
					RoleDescription role = getRoleExpression(exp1);
					RoleDescription invrole = descFactory.getRoleDescription(role.getPredicate(), !role.isInverse());

					DLLiterRoleInclusionImpl symm = new DLLiterRoleInclusionImpl(invrole, role);

					dl_onto.addAssertion(symm);

				} else if (axiom instanceof OWLObjectPropertyDomainAxiom) {

					OWLObjectPropertyDomainAxiom aux = (OWLObjectPropertyDomainAxiom) axiom;
					RoleDescription role = getRoleExpression(aux.getProperty());

					ConceptDescription subclass = descFactory.getExistentialConceptDescription(role.getPredicate(), role.isInverse());
					List<ConceptDescription> superDescriptions = getSuperclassExpressions(((OWLObjectPropertyDomainAxiom) axiom)
							.getDomain());

					addSubclassAxioms(dl_onto, subclass, superDescriptions);

				} else if (axiom instanceof OWLObjectPropertyRangeAxiom) {

					OWLObjectPropertyRangeAxiom aux = (OWLObjectPropertyRangeAxiom) axiom;
					RoleDescription role = getRoleExpression(aux.getProperty());

					ConceptDescription subclass = descFactory.getExistentialConceptDescription(role.getPredicate(), !role.isInverse());
					List<ConceptDescription> superDescriptions = getSuperclassExpressions(((OWLObjectPropertyRangeAxiom) axiom).getRange());

					addSubclassAxioms(dl_onto, subclass, superDescriptions);

				} else if (axiom instanceof OWLObjectSubPropertyAxiom) {

					OWLObjectSubPropertyAxiom aux = (OWLObjectSubPropertyAxiom) axiom;
					RoleDescription subrole = getRoleExpression(aux.getSubProperty());
					RoleDescription superrole = getRoleExpression(aux.getSuperProperty());

					DLLiterRoleInclusionImpl roleinc = new DLLiterRoleInclusionImpl(subrole, superrole);

					dl_onto.addAssertion(roleinc);

				} else if (axiom instanceof OWLFunctionalObjectPropertyAxiom) {
					if (profile.order() < LanguageProfile.OWL2QL.order())
						throw new TranslationException();
					OWLFunctionalObjectPropertyAxiom aux = (OWLFunctionalObjectPropertyAxiom) axiom;
					RoleDescription role = getRoleExpression(aux.getProperty());
					FunctionalRoleAssertion funct = new FunctionalRoleAssertionImpl(role);

					dl_onto.addAssertion(funct);

				} else if (axiom instanceof OWLInverseFunctionalObjectPropertyAxiom) {
					if (profile.order() < LanguageProfile.OWL2QL.order())
						throw new TranslationException();
					OWLInverseFunctionalObjectPropertyAxiom aux = (OWLInverseFunctionalObjectPropertyAxiom) axiom;
					RoleDescription role = getRoleExpression(aux.getProperty());
					RoleDescription invrole = descFactory.getRoleDescription(role.getPredicate(), !role.isInverse());
					FunctionalRoleAssertion funct = new FunctionalRoleAssertionImpl(invrole);

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
	private void addSubclassAxioms(DLLiterOntology dl_onto, ConceptDescription subDescription, List<ConceptDescription> superDescriptions) {
		for (ConceptDescription superDescription : superDescriptions) {
			if (superDescription == null || subDescription == null) {
				log.warn("NULL: {} {}", subDescription, superDescription);
			}

			if (!(superDescription instanceof QualifiedExistentialConceptDescription)) {
				DLLiterConceptInclusionImpl inc = new DLLiterConceptInclusionImpl(subDescription, superDescription);
				dl_onto.addAssertion(inc);
			} else {
				log.debug("Generating encoding for {} subclassof {}", subDescription, superDescription);
				/*
				 * We found an existential, we need to get an auxiliary set of
				 * subClassAssertion
				 */
				List<PositiveInclusion> aux = auxiliaryAssertions.get(superDescription);
				if (aux == null) {
					/*
					 * no aux subclass assertions found for this exists R.A,
					 * creating a new one
					 */
					QualifiedExistentialConceptDescription eR = (QualifiedExistentialConceptDescription) superDescription;
					Predicate role = eR.getPredicate();
					AtomicConceptDescription filler = eR.getFiller();

					RoleDescription auxRole = descFactory.getRoleDescription(predicateFactory.getPredicate(
							URI.create(AUXROLEURI + auxRoleCounter), 2));
					auxRoleCounter += 1;

					/* Creating the new subrole assertions */
					DLLiterRoleInclusionImpl subrole = new DLLiterRoleInclusionImpl(auxRole, descFactory.getRoleDescription(role,
							eR.isInverse()));
					/* Creatin the range assertion */
					DLLiterConceptInclusionImpl subclass = new DLLiterConceptInclusionImpl(descFactory.getExistentialConceptDescription(
							auxRole.getPredicate(), true), filler);
					aux = new LinkedList<PositiveInclusion>();
					aux.add(subclass);
					aux.add(subrole);

					dl_onto.addAssertion(subclass);
					dl_onto.addAssertion(subrole);
				}

				DLLiterRoleInclusionImpl roleinclusion = (DLLiterRoleInclusionImpl) aux.get(1);
				RoleDescription role = roleinclusion.getIncluded();
				ExistentialConceptDescription domain = descFactory.getExistentialConceptDescription(role.getPredicate(), false);
				/* Taking the domain of the aux role as the including */
				DLLiterConceptInclusionImpl inc = new DLLiterConceptInclusionImpl(subDescription, domain);

			}
		}
	}

	private List<ConceptDescription> getSubclassExpressions(Collection<OWLDescription> owlExpressions) throws TranslationException {
		List<ConceptDescription> descriptions = new LinkedList<ConceptDescription>();
		for (OWLDescription owlDescription : owlExpressions) {
			descriptions.add(getSubclassExpression(owlDescription));
		}
		return descriptions;
	}

	private RoleDescription getRoleExpression(OWLObjectPropertyExpression rolExpression) throws TranslationException {
		RoleDescription role = null;

		if (rolExpression instanceof OWLObjectProperty) {
			role = descFactory.getRoleDescription(this.predicateFactory.getPredicate(rolExpression.asOWLObjectProperty().getURI(), 2));
		} else if (rolExpression instanceof OWLObjectPropertyInverse) {
			if (profile.order() < LanguageProfile.OWL2QL.order())
				throw new TranslationException();
			OWLObjectPropertyInverse aux = (OWLObjectPropertyInverse) rolExpression;
			role = descFactory.getRoleDescription(this.predicateFactory.getPredicate(aux.getInverse().asOWLObjectProperty().getURI(), 2),
					true);
		} else {
			throw new TranslationException();
		}
		return role;

	}

	private void addConceptEquivalences(DLLiterOntology ontology, List<ConceptDescription> roles) {
		for (int i = 0; i < roles.size(); i++) {
			for (int j = i + 1; j < roles.size(); j++) {
				ConceptDescription subclass = roles.get(i);
				ConceptDescription superclass = roles.get(j);
				DLLiterConceptInclusionImpl inclusion1 = new DLLiterConceptInclusionImpl(subclass, superclass);
				DLLiterConceptInclusionImpl inclusion2 = new DLLiterConceptInclusionImpl(superclass, subclass);
				ontology.addAssertion(inclusion1);
				ontology.addAssertion(inclusion2);
			}
		}
	}

	private void addRoleEquivalences(DLLiterOntology ontology, List<RoleDescription> roles) {
		for (int i = 0; i < roles.size(); i++) {
			for (int j = i + 1; j < roles.size(); j++) {
				RoleDescription subrole = roles.get(i);
				RoleDescription superole = roles.get(j);
				DLLiterRoleInclusionImpl inclusion1 = new DLLiterRoleInclusionImpl(subrole, superole);
				DLLiterRoleInclusionImpl inclusion2 = new DLLiterRoleInclusionImpl(superole, subrole);
				ontology.addAssertion(inclusion1);
				ontology.addAssertion(inclusion2);
			}
		}
	}

	private List<RoleDescription> getObjectRoleExpressions(Collection<OWLObjectPropertyExpression> rolExpressions)
			throws TranslationException {
		List<RoleDescription> result = new LinkedList<RoleDescription>();
		for (OWLObjectPropertyExpression rolExpression : rolExpressions) {
			result.add(getRoleExpression(rolExpression));
		}
		return result;
	}

	private List<RoleDescription> getDataRoleExpressions(Collection<OWLDataPropertyExpression> rolExpressions) throws TranslationException {
		List<RoleDescription> result = new LinkedList<RoleDescription>();
		for (OWLDataPropertyExpression rolExpression : rolExpressions) {
			result.add(getRoleExpression(rolExpression));
		}
		return result;
	}

	private RoleDescription getRoleExpression(OWLDataPropertyExpression rolExpression) throws TranslationException {
		RoleDescription role = null;

		if (rolExpression instanceof OWLDataProperty) {
			role = descFactory.getRoleDescription(this.predicateFactory.getPredicate(rolExpression.asOWLDataProperty().getURI(), 2));
		} else {
			throw new TranslationException();
		}
		return role;

	}

	private List<ConceptDescription> getSuperclassExpressions(OWLDescription owlExpression) throws TranslationException {
		List<ConceptDescription> result = new LinkedList<ConceptDescription>();
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
				RoleDescription role = getRoleExpression(property);
				ConceptDescription cd = descFactory.getExistentialConceptDescription(role.getPredicate(), role.isInverse(),
						(AtomicConceptDescription) getSubclassExpression(filler));
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
			RoleDescription role = getRoleExpression(property);

			ConceptDescription cd = descFactory.getExistentialConceptDescription(role.getPredicate(), role.isInverse());
			result.add(cd);

		} else {
			result.add(getSubclassExpression(owlExpression));
		}
		return result;

	}

	private ConceptDescription getSubclassExpression(OWLDescription owlExpression) throws TranslationException {
		ConceptDescription cd = null;
		if (owlExpression instanceof OWLClass) {
			URI uri = ((OWLClass) owlExpression).getURI();
			Predicate p = predicateFactory.getPredicate(uri, 1);
			cd = descFactory.getAtomicConceptDescription(p);

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
			Predicate attribute = predicateFactory.getPredicate(uri, 2);
			cd = descFactory.getExistentialConceptDescription(attribute, false);

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
			Predicate role = predicateFactory.getPredicate(uri, 2);

			if (propExp instanceof OWLObjectPropertyInverse) {
				cd = descFactory.getExistentialConceptDescription(role, true);
			} else {
				cd = descFactory.getExistentialConceptDescription(role, false);
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
			Predicate role = predicateFactory.getPredicate(uri, 2);

			if (propExp instanceof OWLObjectPropertyInverse) {
				cd = descFactory.getExistentialConceptDescription(role, true);
			} else {
				cd = descFactory.getExistentialConceptDescription(role, false);
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
			Predicate role = predicateFactory.getPredicate(uri, 2);
			cd = descFactory.getExistentialConceptDescription(role, false);

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

	}
}
