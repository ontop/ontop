package it.unibz.krdb.obda.owlrefplatform.core.ontology.imp;

import it.unibz.krdb.obda.model.Predicate;
import it.unibz.krdb.obda.owlrefplatform.core.ontology.Assertion;
import it.unibz.krdb.obda.owlrefplatform.core.ontology.ConceptDescription;
import it.unibz.krdb.obda.owlrefplatform.core.ontology.DLLiterOntology;
import it.unibz.krdb.obda.owlrefplatform.core.ontology.PositiveInclusion;
import it.unibz.krdb.obda.owlrefplatform.core.ontology.RoleDescription;

import java.net.URI;
import java.util.Collection;
import java.util.HashMap;
import java.util.HashSet;
import java.util.LinkedHashSet;
import java.util.Map;
import java.util.Set;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class DLLiterOntologyImpl implements DLLiterOntology {

	private Set<PositiveInclusion>					assertions					= null;
	private Set<ConceptDescription>					concepts					= null;
	private Set<RoleDescription>					roles						= null;
	private URI										ontouri						= null;

	private Set<Assertion>							originalassertions			= null;

	/* Assertions indexed by right side predicate */
	private Map<Predicate, Set<PositiveInclusion>>	rightAssertionIndex			= null;

	private Map<Predicate, Set<PositiveInclusion>>	rightNonExistentialIndex	= null;

	private Map<Predicate, Set<PositiveInclusion>>	rightExistentialIndex		= null;

	/* Assertions indexed by left side predicate */
	private Map<Predicate, Set<PositiveInclusion>>	leftAssertionIndex			= null;

	Logger											log							= LoggerFactory.getLogger(this.getClass());

	public boolean									isSaturated					= false;

	// private Set<PositiveInclusion> assertions = null;

	public DLLiterOntologyImpl(URI uri) {
		ontouri = uri;
		originalassertions = new LinkedHashSet<Assertion>();
		concepts = new HashSet<ConceptDescription>();
		roles = new HashSet<RoleDescription>();
	}

	@Override
	public void addAssertion(Assertion assertion) {
		if (originalassertions.contains(assertion))
			return;
		isSaturated = false;
		originalassertions.add(assertion);
	}

	@Override
	public Set<Assertion> getAssertions() {
		return originalassertions;
	}

	@Override
	public void addAssertions(Collection<Assertion> ass) {
		isSaturated = false;
		originalassertions.addAll(ass);
	}

	@Override
	public void addConcept(ConceptDescription cd) {
		concepts.add(cd);
	}

	@Override
	public void addConcepts(Collection<ConceptDescription> cd) {
		isSaturated = false;
		concepts.addAll(cd);
	}

	@Override
	public void addRole(RoleDescription rd) {
		isSaturated = false;
		roles.add(rd);
	}

	@Override
	public void addRoles(Collection<RoleDescription> rd) {
		isSaturated = false;
		roles.addAll(rd);
	}

	@Override
	public Set<ConceptDescription> getConcepts() {

		return concepts;
	}

	@Override
	public Set<RoleDescription> getRoles() {
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
	public Set<PositiveInclusion> getByIncluding(Predicate pred) {
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
	public Set<PositiveInclusion> getByIncludingExistOnly(Predicate pred) {
		return rightExistentialIndex.get(pred);
	}

	public Set<PositiveInclusion> getByIncludingNoExist(Predicate pred) {
		return rightNonExistentialIndex.get(pred);
	}

	@Override
	public Set<PositiveInclusion> getByIncluded(Predicate pred) {
		return leftAssertionIndex.get(pred);
	}

	// public Set<PositiveInclusion> getByIncludedExistOnly(Predicate pred);
	//
	// public Set<PositiveInclusion> getByIncludedNoExist(Predicate pred);

	@Override
	public void saturate() {
		if (isSaturated) {
//			log.debug("Ontology is already saturdated");
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
		assertions = new HashSet<PositiveInclusion>();
		leftAssertionIndex = new HashMap<Predicate, Set<PositiveInclusion>>();
		rightAssertionIndex = new HashMap<Predicate, Set<PositiveInclusion>>();
		rightNonExistentialIndex = new HashMap<Predicate, Set<PositiveInclusion>>();
		rightExistentialIndex = new HashMap<Predicate, Set<PositiveInclusion>>();

		/*
		 * Loading the initial assertions, filtering postive inlusions and
		 * indexing
		 */
		for (Assertion assertion : originalassertions) {
			if (assertion instanceof PositiveInclusion) {
				PositiveInclusion pi = (PositiveInclusion) assertion;
				assertions.add(pi);
				index(pi);
			}
		}

		/* Saturating is-a hierachy loop */
		boolean loop = true;
		while (loop) {
			loop = false;
			HashSet<PositiveInclusion> newInclusions = new HashSet<PositiveInclusion>();
			for (PositiveInclusion pi1 : assertions) {
				for (PositiveInclusion pi2 : assertions) {
					if ((pi1 instanceof DLLiterConceptInclusionImpl) && (pi2 instanceof DLLiterConceptInclusionImpl)) {
						DLLiterConceptInclusionImpl ci1 = (DLLiterConceptInclusionImpl) pi1;
						DLLiterConceptInclusionImpl ci2 = (DLLiterConceptInclusionImpl) pi2;
						if (ci1.getIncluding().equals(ci2.getIncluded())) {
							DLLiterConceptInclusionImpl newinclusion = new DLLiterConceptInclusionImpl(ci1.getIncluded(),
									ci2.getIncluding());
							newInclusions.add(newinclusion);
						} else if (ci1.getIncluded().equals(ci2.getIncluding())) {
							DLLiterConceptInclusionImpl newinclusion = new DLLiterConceptInclusionImpl(ci2.getIncluded(),
									ci1.getIncluding());
							newInclusions.add(newinclusion);
						}
					} else if ((pi1 instanceof DLLiterRoleInclusionImpl) && (pi2 instanceof DLLiterRoleInclusionImpl)) {
						DLLiterRoleInclusionImpl ci1 = (DLLiterRoleInclusionImpl) pi1;
						DLLiterRoleInclusionImpl ci2 = (DLLiterRoleInclusionImpl) pi2;
						if (ci1.getIncluding().equals(ci2.getIncluded())) {
							DLLiterRoleInclusionImpl newinclusion = new DLLiterRoleInclusionImpl(ci1.getIncluded(), ci2.getIncluding());
							newInclusions.add(newinclusion);
						} else if (ci1.getIncluded().equals(ci2.getIncluding())) {
							DLLiterRoleInclusionImpl newinclusion = new DLLiterRoleInclusionImpl(ci2.getIncluded(), ci1.getIncluding());
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

	private void indexAll(Collection<PositiveInclusion> pis) {
		for (PositiveInclusion pi : pis) {
			index(pi);
		}
	}

	private void index(PositiveInclusion pi) {
		if (pi instanceof DLLiterConceptInclusionImpl) {
			DLLiterConceptInclusionImpl cpi = (DLLiterConceptInclusionImpl) pi;
			ConceptDescription description1 = cpi.getIncluded();
			ConceptDescription description2 = cpi.getIncluding();

			/* Processing right side */
			if (description2 instanceof AtomicConceptDescriptionImpl) {
				AtomicConceptDescriptionImpl acd = (AtomicConceptDescriptionImpl) description2;
				Set<PositiveInclusion> rightAssertion = getRight(acd.getPredicate());
				rightAssertion.add(pi);

				Set<PositiveInclusion> rightNonExistential = getRightNotExistential(acd.getPredicate());
				rightNonExistential.add(pi);

			} else if (description2 instanceof ExistentialConceptDescriptionImpl) {
				ExistentialConceptDescriptionImpl ecd = (ExistentialConceptDescriptionImpl) description2;
				Set<PositiveInclusion> rightAssertion = getRight(ecd.getPredicate());
				rightAssertion.add(pi);

				Set<PositiveInclusion> rightExistential = getRightExistential(ecd.getPredicate());
				rightExistential.add(pi);
			}

			/* Processing left side */
			if (description1 instanceof AtomicConceptDescriptionImpl) {
				AtomicConceptDescriptionImpl acd = (AtomicConceptDescriptionImpl) description1;
				Set<PositiveInclusion> leftAssertion = getLeft(acd.getPredicate());
				leftAssertion.add(pi);

			} else if (description1 instanceof ExistentialConceptDescriptionImpl) {
				ExistentialConceptDescriptionImpl ecd = (ExistentialConceptDescriptionImpl) description1;
				Set<PositiveInclusion> leftAssertion = getLeft(ecd.getPredicate());
				leftAssertion.add(pi);
			}

		} else if (pi instanceof DLLiterRoleInclusionImpl) {
			DLLiterRoleInclusionImpl cpi = (DLLiterRoleInclusionImpl) pi;

			RoleDescription description1 = cpi.getIncluded();
			RoleDescription description2 = cpi.getIncluding();

			/* Processing right side */
			if (description2 instanceof AtomicRoleDescriptionImpl) {
				AtomicRoleDescriptionImpl acd = (AtomicRoleDescriptionImpl) description2;
				Set<PositiveInclusion> rightAssertion = getRight(acd.getPredicate());
				rightAssertion.add(pi);

				Set<PositiveInclusion> rightNonExistential = getRightNotExistential(acd.getPredicate());
				rightNonExistential.add(pi);

			}

			/* Processing left side */
			if (description1 instanceof AtomicRoleDescriptionImpl) {
				AtomicRoleDescriptionImpl acd = (AtomicRoleDescriptionImpl) description1;
				Set<PositiveInclusion> leftAssertion = getLeft(acd.getPredicate());
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

	private Set<PositiveInclusion> getRight(Predicate pred) {
		Set<PositiveInclusion> assertions = rightAssertionIndex.get(pred);
		if (assertions == null) {
			assertions = new LinkedHashSet<PositiveInclusion>();
			rightAssertionIndex.put(pred, assertions);
		}
		return assertions;
	}

	private Set<PositiveInclusion> getLeft(Predicate pred) {
		Set<PositiveInclusion> assertions = leftAssertionIndex.get(pred);
		if (assertions == null) {
			assertions = new LinkedHashSet<PositiveInclusion>();
			leftAssertionIndex.put(pred, assertions);
		}
		return assertions;
	}

	private Set<PositiveInclusion> getRightNotExistential(Predicate pred) {
		Set<PositiveInclusion> assertions = rightNonExistentialIndex.get(pred);
		if (assertions == null) {
			assertions = new LinkedHashSet<PositiveInclusion>();
			rightNonExistentialIndex.put(pred, assertions);
		}
		return assertions;
	}

	private Set<PositiveInclusion> getRightExistential(Predicate pred) {
		Set<PositiveInclusion> assertions = rightExistentialIndex.get(pred);
		if (assertions == null) {
			assertions = new LinkedHashSet<PositiveInclusion>();
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
	private Set<Assertion> computeExistentials() {
		HashSet<Assertion> newassertion = new HashSet<Assertion>(1000);
		for (Assertion assertion : originalassertions) {
			if (assertion instanceof DLLiterRoleInclusionImpl) {
				DLLiterRoleInclusionImpl rinclusion = (DLLiterRoleInclusionImpl) assertion;
				RoleDescription r1 = rinclusion.getIncluded();
				RoleDescription r2 = rinclusion.getIncluding();

				ExistentialConceptDescriptionImpl e11 = new ExistentialConceptDescriptionImpl(r1.getPredicate(), r1.isInverse());
				;
				ExistentialConceptDescriptionImpl e12 = new ExistentialConceptDescriptionImpl(r2.getPredicate(), r2.isInverse());
				ExistentialConceptDescriptionImpl e21 = new ExistentialConceptDescriptionImpl(r1.getPredicate(), !r1.isInverse());
				ExistentialConceptDescriptionImpl e22 = new ExistentialConceptDescriptionImpl(r2.getPredicate(), !r2.isInverse());

				DLLiterConceptInclusionImpl inc1 = new DLLiterConceptInclusionImpl(e11, e12);
				DLLiterConceptInclusionImpl inc2 = new DLLiterConceptInclusionImpl(e21, e22);
				newassertion.add(inc1);
				newassertion.add(inc2);
			}
		}
		return newassertion;
	}

}
