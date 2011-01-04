package org.obda.reformulation.dllite;

import java.util.Collection;
import java.util.HashMap;
import java.util.HashSet;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.obda.query.domain.Atom;
import org.obda.query.domain.CQIE;
import org.obda.query.domain.DatalogProgram;
import org.obda.query.domain.Predicate;
import org.obda.query.domain.Query;
import org.obda.query.domain.imp.DatalogProgramImpl;
import org.obda.reformulation.domain.Assertion;
import org.obda.reformulation.domain.ConceptDescription;
import org.obda.reformulation.domain.PositiveInclusion;
import org.obda.reformulation.domain.RoleDescription;
import org.obda.reformulation.domain.imp.AtomicConceptDescriptionImpl;
import org.obda.reformulation.domain.imp.AtomicRoleDescriptionImpl;
import org.obda.reformulation.domain.imp.DLLiterConceptInclusionImpl;
import org.obda.reformulation.domain.imp.DLLiterRoleInclusionImpl;
import org.obda.reformulation.domain.imp.ExistentialConceptDescriptionImpl;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class TreeRedReformulator implements QueryRewriter {

	private QueryAnonymizer							anonymizer					= null;
	private AtomUnifier								unifier						= null;
	private PositiveInclusionApplicator				piApplicator				= null;
	private Set<PositiveInclusion>					assertions					= null;

	private List<Assertion>							originalassertions			= null;
	/* Assertions indexed by left side predicate */
	// private Map<Predicate, Set<PositiveInclusion>> leftAssertionIndex = null;

	/* Assertions indexed by left side predicate */
	private Map<Predicate, Set<PositiveInclusion>>	rightAssertionIndex			= null;

	private Map<Predicate, Set<PositiveInclusion>>	rightNonExistentialIndex	= null;

	private Map<Predicate, Set<PositiveInclusion>>	rightExistentialIndex		= null;

	Logger											log							= LoggerFactory.getLogger(DLRPerfectReformulator.class);

	public TreeRedReformulator(List<Assertion> assertions) {
		this.originalassertions = assertions;

		saturateAssertions();

		piApplicator = new PositiveInclusionApplicator();
		unifier = new AtomUnifier();
		anonymizer = new QueryAnonymizer();

	}

	public Query rewrite(Query input) throws Exception {

		if (!(input instanceof DatalogProgram)) {
			throw new Exception("Rewriting exception: The input must be a DatalogProgram instance");
		}

		DatalogProgram prog = (DatalogProgram) input;

		log.debug("Starting query rewrting. Received query: \n{}", prog);

		if (!prog.isUCQ()) {
			throw new Exception("Rewriting exception: The input is not a valid union of conjuctive queries");
		}

		/* Query preprocessing */
		log.debug("Anonymizing the query");

		DatalogProgram anonymizedProgram = anonymizer.anonymize(prog);

		HashSet<CQIE> oldqueries = new HashSet<CQIE>();
		oldqueries.addAll(anonymizedProgram.getRules());

		HashSet<CQIE> result = new HashSet<CQIE>();
		result.addAll(oldqueries);

		boolean loop = true;
		while (loop) {
			loop = false;
			HashSet<CQIE> newqueries = new HashSet<CQIE>();

			for (CQIE oldquery : oldqueries) {

				HashSet<PositiveInclusion> relevantInclusions = new HashSet<PositiveInclusion>();
				for (Atom atom : oldquery.getBody()) {
					relevantInclusions.addAll(getRight(atom.getPredicate()));
				}

				newqueries.addAll(piApplicator.apply(oldquery, relevantInclusions));

			}

			for (CQIE newquery : newqueries) {
				loop = loop | result.add(anonymizer.anonymize(newquery));
			}
			
			if (loop) {
				oldqueries = new HashSet<CQIE>();
				for (CQIE newquery : newqueries) {
					oldqueries.add(anonymizer.anonymize(newquery));
				}
			}
		}
		LinkedList<CQIE> resultlist = new LinkedList<CQIE>();
		resultlist.addAll(result);

		DatalogProgram resultprogram = new DatalogProgramImpl();
		resultprogram.appendRule(resultlist);
		return resultprogram;
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
		// leftAssertionIndex = new HashMap<Predicate,
		// Set<PositiveInclusion>>();
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

		/* Saturating loop */
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
							DLLiterConceptInclusionImpl newinclusion = new DLLiterConceptInclusionImpl(ci1.getIncluded(), ci2
									.getIncluding());
							newInclusions.add(newinclusion);
						}
					} else if ((pi1 instanceof DLLiterRoleInclusionImpl) && (pi2 instanceof DLLiterRoleInclusionImpl)) {
						DLLiterRoleInclusionImpl ci1 = (DLLiterRoleInclusionImpl) pi1;
						DLLiterRoleInclusionImpl ci2 = (DLLiterRoleInclusionImpl) pi2;
						if (ci1.getIncluding().equals(ci2.getIncluded())) {
							DLLiterRoleInclusionImpl newinclusion = new DLLiterRoleInclusionImpl(ci1.getIncluded(), ci2.getIncluding());
							newInclusions.add(newinclusion);
						}
					}
				}
			}

			loop = loop || assertions.addAll(newInclusions);
			if (loop)
				indexAll(newInclusions);
		}
	}

	private void indexAll(Collection<PositiveInclusion> pis) {
		for (PositiveInclusion pi : pis) {
			index(pi);
		}
	}

	private void index(PositiveInclusion pi) {
		if (pi instanceof DLLiterConceptInclusionImpl) {
			DLLiterConceptInclusionImpl cpi = (DLLiterConceptInclusionImpl) pi;
			// ConceptDescription description1 = cpi.getIncluded();
			ConceptDescription description2 = cpi.getIncluding();

			// /* Processing left side */
			// if (description1 instanceof AtomicConceptDescriptionImpl) {
			// AtomicConceptDescriptionImpl acd = (AtomicConceptDescriptionImpl)
			// description1;
			// Set<PositiveInclusion> leftAssertion =
			// getLeft(acd.getPredicate());
			// leftAssertion.add(pi);
			// } else if (description1 instanceof
			// ExistentialConceptDescriptionImpl) {
			// ExistentialConceptDescriptionImpl ecd =
			// (ExistentialConceptDescriptionImpl) description1;
			// Set<PositiveInclusion> leftAssertion =
			// getLeft(ecd.getPredicate());
			// leftAssertion.add(pi);
			// }

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

		} else if (pi instanceof DLLiterRoleInclusionImpl) {
			DLLiterRoleInclusionImpl cpi = (DLLiterRoleInclusionImpl) pi;

			// RoleDescription description1 = cpi.getIncluded();
			RoleDescription description2 = cpi.getIncluding();

			// /* Processing left side */
			// if (description1 instanceof AtomicRoleDescriptionImpl) {
			// AtomicRoleDescriptionImpl acd = (AtomicRoleDescriptionImpl)
			// description1;
			// Set<PositiveInclusion> leftAssertion =
			// getLeft(acd.getPredicate());
			// leftAssertion.add(pi);
			// }
			/* Processing right side */
			if (description2 instanceof AtomicRoleDescriptionImpl) {
				AtomicRoleDescriptionImpl acd = (AtomicRoleDescriptionImpl) description2;
				Set<PositiveInclusion> rightAssertion = getRight(acd.getPredicate());
				rightAssertion.add(pi);

				Set<PositiveInclusion> rightNonExistential = getRightNotExistential(acd.getPredicate());
				rightNonExistential.add(pi);

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
			assertions = new HashSet<PositiveInclusion>();
			rightAssertionIndex.put(pred, assertions);
		}
		return assertions;
	}

	private Set<PositiveInclusion> getRightNotExistential(Predicate pred) {
		Set<PositiveInclusion> assertions = rightNonExistentialIndex.get(pred);
		if (assertions == null) {
			assertions = new HashSet<PositiveInclusion>();
			rightNonExistentialIndex.put(pred, assertions);
		}
		return assertions;
	}

	private Set<PositiveInclusion> getRightExistential(Predicate pred) {
		Set<PositiveInclusion> assertions = rightExistentialIndex.get(pred);
		if (assertions == null) {
			assertions = new HashSet<PositiveInclusion>();
			rightExistentialIndex.put(pred, assertions);
		}
		return assertions;
	}

	@Override
	public void updateAssertions(List<Assertion> ass) {
		this.originalassertions = ass;
	}

}
