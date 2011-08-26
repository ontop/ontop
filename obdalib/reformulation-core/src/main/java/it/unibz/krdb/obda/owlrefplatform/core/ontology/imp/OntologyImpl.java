package it.unibz.krdb.obda.owlrefplatform.core.ontology.imp;

import it.unibz.krdb.obda.model.Predicate;
import it.unibz.krdb.obda.owlrefplatform.core.ontology.Axiom;
import it.unibz.krdb.obda.owlrefplatform.core.ontology.ClassDescription;
import it.unibz.krdb.obda.owlrefplatform.core.ontology.Ontology;
import it.unibz.krdb.obda.owlrefplatform.core.ontology.OntologyFactory;
import it.unibz.krdb.obda.owlrefplatform.core.ontology.Property;
import it.unibz.krdb.obda.owlrefplatform.core.ontology.SubDescriptionAxiom;

import java.net.URI;
import java.util.Collection;
import java.util.HashMap;
import java.util.HashSet;
import java.util.LinkedHashSet;
import java.util.Map;
import java.util.Set;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class OntologyImpl implements Ontology {

	private Set<SubDescriptionAxiom>					assertions					= null;
	private Set<ClassDescription>						concepts					= null;
	private Set<Property>								roles						= null;
	private URI											ontouri						= null;

	private Set<Axiom>									originalassertions			= null;

	/* Assertions indexed by right side predicate */
	private Map<Predicate, Set<SubDescriptionAxiom>>	rightAssertionIndex			= null;

	private Map<Predicate, Set<SubDescriptionAxiom>>	rightNonExistentialIndex	= null;

	private Map<Predicate, Set<SubDescriptionAxiom>>	rightExistentialIndex		= null;

	/* Assertions indexed by left side predicate */
	private Map<Predicate, Set<SubDescriptionAxiom>>	leftAssertionIndex			= null;

	Logger												log							= LoggerFactory.getLogger(this.getClass());

	public boolean										isSaturated					= false;

	private final OntologyFactory				factory						= OntologyFactoryImpl.getInstance();

	// private Set<PositiveInclusion> assertions = null;

	OntologyImpl(URI uri) {
		ontouri = uri;
		originalassertions = new LinkedHashSet<Axiom>();
		concepts = new HashSet<ClassDescription>();
		roles = new HashSet<Property>();
	}

	@Override
	public OntologyImpl clone() {
		OntologyImpl clone = factory.createOntology(URI.create(ontouri.toString()));
		clone.originalassertions.addAll(originalassertions);
		clone.concepts.addAll(concepts);
		clone.roles.addAll(roles);
		clone.isSaturated = isSaturated;

		if (assertions != null) {
			clone.assertions = new HashSet<SubDescriptionAxiom>();
			clone.assertions.addAll(assertions);
		}

		if (rightAssertionIndex != null) {
			clone.rightAssertionIndex = new HashMap<Predicate, Set<SubDescriptionAxiom>>();
			clone.rightAssertionIndex.putAll(rightAssertionIndex);

		}

		if (rightNonExistentialIndex != null) {
			clone.rightNonExistentialIndex = new HashMap<Predicate, Set<SubDescriptionAxiom>>();
			clone.rightNonExistentialIndex.putAll(rightNonExistentialIndex);

		}

		if (rightExistentialIndex != null) {
			clone.rightExistentialIndex = new HashMap<Predicate, Set<SubDescriptionAxiom>>();
			clone.rightExistentialIndex.putAll(rightExistentialIndex);

		}

		if (leftAssertionIndex != null) {
			clone.leftAssertionIndex = new HashMap<Predicate, Set<SubDescriptionAxiom>>();
			clone.leftAssertionIndex.putAll(leftAssertionIndex);

		}

		return clone;
	}

	@Override
	public void addAssertion(Axiom assertion) {
		if (originalassertions.contains(assertion))
			return;
		isSaturated = false;
		originalassertions.add(assertion);
	}

	@Override
	public Set<Axiom> getAssertions() {
		return originalassertions;
	}

	@Override
	public void addAssertions(Collection<Axiom> ass) {
		isSaturated = false;
		originalassertions.addAll(ass);
	}

	@Override
	public void addConcept(ClassDescription cd) {
		concepts.add(cd);
	}

	@Override
	public void addConcepts(Collection<ClassDescription> cd) {
		isSaturated = false;
		concepts.addAll(cd);
	}

	@Override
	public void addRole(Property rd) {
		isSaturated = false;
		roles.add(rd);
	}

	@Override
	public void addRoles(Collection<Property> rd) {
		isSaturated = false;
		roles.addAll(rd);
	}

	@Override
	public Set<ClassDescription> getConcepts() {

		return concepts;
	}

	@Override
	public Set<Property> getRoles() {
		return roles;
	}

	@Override
	public URI getUri() {
		return ontouri;
	}

	/***
	 * This will retrun all the assertions whose right side concept description
	 * refers to the predicate 'pred'
	 * 
	 * @param pred
	 * @return
	 */
	public Set<SubDescriptionAxiom> getByIncluding(Predicate pred) {
		return rightAssertionIndex.get(pred);
	}

	/***
	 * As before but it will only return assetions where the right side is an
	 * existential role concept description
	 * 
	 * @param pred
	 * @param onlyAtomic
	 * @return
	 */
	public Set<SubDescriptionAxiom> getByIncludingExistOnly(Predicate pred) {
		return rightExistentialIndex.get(pred);
	}

	public Set<SubDescriptionAxiom> getByIncludingNoExist(Predicate pred) {
		return rightNonExistentialIndex.get(pred);
	}

	@Override
	public Set<SubDescriptionAxiom> getByIncluded(Predicate pred) {
		return leftAssertionIndex.get(pred);
	}

	// public Set<PositiveInclusion> getByIncludedExistOnly(Predicate pred);
	//
	// public Set<PositiveInclusion> getByIncludedNoExist(Predicate pred);

	@Override
	public void saturate() {
		if (isSaturated) {
			// log.debug("Ontology is already saturdated");
			return;
		}
		log.debug("Given assertions: {}", originalassertions);
		/*
		 * Our strategy requires that for every aciom R ISA S, we also have the
		 * axioms \exists R ISA \exist S and \exists R- ISA \exists S- this
		 * allows us to keep the cycles to a minimum
		 */
		originalassertions.addAll(computeExistentials());
		saturateAssertions();

		log.debug("Computed assertions: {}", this.originalassertions);
		isSaturated = true;

	}

	/***
	 * Saturates the set of assertions and creates the indexes for these based
	 * on their predicates. It only takes into account positive inclusions. PIs
	 * with qualififed existntital concepts are ignored.
	 * 
	 * To saturate, we do
	 * 
	 * For each pair of assertions C1 ISA C2, C2 ISA C3, we compute C1 ISA C3.
	 */
	private void saturateAssertions() {
		assertions = new HashSet<SubDescriptionAxiom>();
		leftAssertionIndex = new HashMap<Predicate, Set<SubDescriptionAxiom>>();
		rightAssertionIndex = new HashMap<Predicate, Set<SubDescriptionAxiom>>();
		rightNonExistentialIndex = new HashMap<Predicate, Set<SubDescriptionAxiom>>();
		rightExistentialIndex = new HashMap<Predicate, Set<SubDescriptionAxiom>>();

		/*
		 * Loading the initial assertions, filtering postive inlusions and
		 * indexing
		 */
		for (Axiom assertion : originalassertions) {
			if (assertion instanceof SubDescriptionAxiom) {
				SubDescriptionAxiom pi = (SubDescriptionAxiom) assertion;
				assertions.add(pi);
				index(pi);
			}
		}

		/* Saturating is-a hierachy loop */
		boolean loop = true;
		while (loop) {
			loop = false;
			HashSet<SubDescriptionAxiom> newInclusions = new HashSet<SubDescriptionAxiom>();
			for (SubDescriptionAxiom pi1 : assertions) {
				for (SubDescriptionAxiom pi2 : assertions) {
					if ((pi1 instanceof SubClassAxiomImpl) && (pi2 instanceof SubClassAxiomImpl)) {
						SubClassAxiomImpl ci1 = (SubClassAxiomImpl) pi1;
						SubClassAxiomImpl ci2 = (SubClassAxiomImpl) pi2;
						if (ci1.getSuper().equals(ci2.getSub())) {
							SubClassAxiomImpl newinclusion = factory.createSubClassAxiom(ci1.getSub(), ci2.getSuper());
							newInclusions.add(newinclusion);
						} else if (ci1.getSub().equals(ci2.getSuper())) {
							SubClassAxiomImpl newinclusion = factory.createSubClassAxiom(ci2.getSub(), ci1.getSuper());
							newInclusions.add(newinclusion);
						}
					} else if ((pi1 instanceof SubPropertyAxiomImpl) && (pi2 instanceof SubPropertyAxiomImpl)) {
						SubPropertyAxiomImpl ci1 = (SubPropertyAxiomImpl) pi1;
						SubPropertyAxiomImpl ci2 = (SubPropertyAxiomImpl) pi2;
						if (ci1.getSuper().equals(ci2.getSub())) {
							SubPropertyAxiomImpl newinclusion = factory.createSubPropertyAxiom(ci1.getSub(), ci2.getSuper());
							newInclusions.add(newinclusion);
						} else if (ci1.getSub().equals(ci2.getSuper())) {
							SubPropertyAxiomImpl newinclusion = factory.createSubPropertyAxiom(ci2.getSub(), ci1.getSuper());
							newInclusions.add(newinclusion);
						}
					}
				}
			}

			loop = loop || assertions.addAll(newInclusions);
			if (loop)
				indexAll(newInclusions);
		}

		// /* saturating A ISA ER (if A ISA ER and R ISA S -> A ISA ES) */
		// /* This will be used for SQO and for optimizing the applicaiton of
		// existential restrictions */
		// HashSet<PositiveInclusion> newExistentials = new
		// HashSet<PositiveInclusion>();
		// for (PositiveInclusion pi: assertions) {
		// if (!(pi instanceof DLLiterConceptInclusionImpl))
		// continue;
		// DLLiterConceptInclusionImpl ci = (DLLiterConceptInclusionImpl)pi;
		// if (!(ci.getIncluding() instanceof
		// ExistentialConceptDescriptionImpl)) {
		// continue;
		// }
		// ExistentialConceptDescriptionImpl ex =
		// (ExistentialConceptDescriptionImpl)ci.getIncluding();
		//
		//
		//
		// }

	}

	private void indexAll(Collection<SubDescriptionAxiom> pis) {
		for (SubDescriptionAxiom pi : pis) {
			index(pi);
		}
	}

	private void index(SubDescriptionAxiom pi) {
		if (pi instanceof SubClassAxiomImpl) {
			SubClassAxiomImpl cpi = (SubClassAxiomImpl) pi;
			ClassDescription description1 = cpi.getSub();
			ClassDescription description2 = cpi.getSuper();

			/* Processing right side */
			if (description2 instanceof ClassImpl) {
				ClassImpl acd = (ClassImpl) description2;
				Set<SubDescriptionAxiom> rightAssertion = getRight(acd.getPredicate());
				rightAssertion.add(pi);

				Set<SubDescriptionAxiom> rightNonExistential = getRightNotExistential(acd.getPredicate());
				rightNonExistential.add(pi);

			} else if (description2 instanceof PropertySomeRestrictionImpl) {
				PropertySomeRestrictionImpl ecd = (PropertySomeRestrictionImpl) description2;
				Set<SubDescriptionAxiom> rightAssertion = getRight(ecd.getPredicate());
				rightAssertion.add(pi);

				Set<SubDescriptionAxiom> rightExistential = getRightExistential(ecd.getPredicate());
				rightExistential.add(pi);
			}

			/* Processing left side */
			if (description1 instanceof ClassImpl) {
				ClassImpl acd = (ClassImpl) description1;
				Set<SubDescriptionAxiom> leftAssertion = getLeft(acd.getPredicate());
				leftAssertion.add(pi);

			} else if (description1 instanceof PropertySomeRestrictionImpl) {
				PropertySomeRestrictionImpl ecd = (PropertySomeRestrictionImpl) description1;
				Set<SubDescriptionAxiom> leftAssertion = getLeft(ecd.getPredicate());
				leftAssertion.add(pi);
			}

		} else if (pi instanceof SubPropertyAxiomImpl) {
			SubPropertyAxiomImpl cpi = (SubPropertyAxiomImpl) pi;

			Property description1 = cpi.getSub();
			Property description2 = cpi.getSuper();

			/* Processing right side */
			if (description2 instanceof PropertyImpl) {
				PropertyImpl acd = (PropertyImpl) description2;
				Set<SubDescriptionAxiom> rightAssertion = getRight(acd.getPredicate());
				rightAssertion.add(pi);

				Set<SubDescriptionAxiom> rightNonExistential = getRightNotExistential(acd.getPredicate());
				rightNonExistential.add(pi);

			}

			/* Processing left side */
			if (description1 instanceof PropertyImpl) {
				PropertyImpl acd = (PropertyImpl) description1;
				Set<SubDescriptionAxiom> leftAssertion = getLeft(acd.getPredicate());
				leftAssertion.add(pi);

			}

		}
	}

	// private Set<PositiveInclusion> getLeft(Predicate pred) {
	// Set<PositiveInclusion> assertions = leftAssertionIndex.get(pred);
	// if (assertions == null) {
	// assertions = new HashSet<PositiveInclusion>();
	// leftAssertionIndex.put(pred, assertions);
	// }
	// return assertions;
	// }

	private Set<SubDescriptionAxiom> getRight(Predicate pred) {
		Set<SubDescriptionAxiom> assertions = rightAssertionIndex.get(pred);
		if (assertions == null) {
			assertions = new LinkedHashSet<SubDescriptionAxiom>();
			rightAssertionIndex.put(pred, assertions);
		}
		return assertions;
	}

	private Set<SubDescriptionAxiom> getLeft(Predicate pred) {
		Set<SubDescriptionAxiom> assertions = leftAssertionIndex.get(pred);
		if (assertions == null) {
			assertions = new LinkedHashSet<SubDescriptionAxiom>();
			leftAssertionIndex.put(pred, assertions);
		}
		return assertions;
	}

	private Set<SubDescriptionAxiom> getRightNotExistential(Predicate pred) {
		Set<SubDescriptionAxiom> assertions = rightNonExistentialIndex.get(pred);
		if (assertions == null) {
			assertions = new LinkedHashSet<SubDescriptionAxiom>();
			rightNonExistentialIndex.put(pred, assertions);
		}
		return assertions;
	}

	private Set<SubDescriptionAxiom> getRightExistential(Predicate pred) {
		Set<SubDescriptionAxiom> assertions = rightExistentialIndex.get(pred);
		if (assertions == null) {
			assertions = new LinkedHashSet<SubDescriptionAxiom>();
			rightExistentialIndex.put(pred, assertions);
		}
		return assertions;
	}

	/***
	 * This method adds to the TBox a pair of axioms ER ISA ES and ER- ISA ES-
	 * for each role inclusion R ISA S found in the ontology.
	 * 
	 * @return The set of extra existential assertions that need to be added to
	 *         the ontology to account for the semantics of role inclusions
	 *         w.r.t. their domains and ranges.
	 */
	private Set<Axiom> computeExistentials() {
		HashSet<Axiom> newassertion = new HashSet<Axiom>(1000);
		for (Axiom assertion : originalassertions) {
			if (assertion instanceof SubPropertyAxiomImpl) {
				SubPropertyAxiomImpl rinclusion = (SubPropertyAxiomImpl) assertion;
				Property r1 = rinclusion.getSub();
				Property r2 = rinclusion.getSuper();

				PropertySomeRestrictionImpl e11 = factory.createPropertySomeRestriction(r1.getPredicate(), r1.isInverse());
				;
				PropertySomeRestrictionImpl e12 = factory.createPropertySomeRestriction(r2.getPredicate(), r2.isInverse());
				PropertySomeRestrictionImpl e21 = factory.createPropertySomeRestriction(r1.getPredicate(), !r1.isInverse());
				PropertySomeRestrictionImpl e22 = factory.createPropertySomeRestriction(r2.getPredicate(), !r2.isInverse());

				SubClassAxiomImpl inc1 = factory.createSubClassAxiom(e11, e12);
				SubClassAxiomImpl inc2 = factory.createSubClassAxiom(e21, e22);
				newassertion.add(inc1);
				newassertion.add(inc2);
			}
		}
		return newassertion;
	}

}
