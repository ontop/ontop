package org.obda.reformulation.dllite;

import java.util.Collection;
import java.util.Collections;
import java.util.Comparator;
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

		/* Simpliying the query by removing redundant atoms w.r.t. to CQC */
		HashSet<CQIE> oldqueries = new HashSet<CQIE>(5000);
		for (CQIE q : anonymizedProgram.getRules()) {
			oldqueries.add(CQCUtilities.removeRundantAtoms(q));
		}
		oldqueries.addAll(anonymizedProgram.getRules());

		HashSet<CQIE> result = new HashSet<CQIE>(5000);
		result.addAll(oldqueries);

		/*
		 * Main loop of the rewriter
		 */

		boolean loop = true;
		while (loop) {
			loop = false;
			HashSet<CQIE> newqueries = new HashSet<CQIE>(1000);

			/*
			 * Handling simple inclusions, none involves existentials on the
			 * right side of an inclusion
			 */
			for (CQIE oldquery : oldqueries) {

				HashSet<PositiveInclusion> relevantInclusions = new HashSet<PositiveInclusion>(1000);
				for (Atom atom : oldquery.getBody()) {
					relevantInclusions.addAll(getRightNotExistential(atom.getPredicate()));
				}

				for (CQIE newcq : piApplicator.apply(oldquery, relevantInclusions)) {
					newqueries.add(anonymizer.anonymize(newcq));
				}

			}

			/*
			 * Optimizing the set
			 */

			newqueries = CQCUtilities.removeDuplicateAtoms(newqueries);

			/*
			 * Handling existential inclusions, and unification We will collect
			 * all predicates mantioned in the old queries, we will collect all
			 * the existential inclusions relevant for those predicates and sort
			 * them by inverse and not inverse. Last we apply these groups of
			 * inclusions at the same time. All of them will share one single
			 * unification atempt.
			 */

			// Collecting relevant predicates

			HashSet<Predicate> predicates = new HashSet<Predicate>(1000);
			for (CQIE oldquery : oldqueries) {
				for (Atom atom : oldquery.getBody()) {
					predicates.add(atom.getPredicate());
				}
			}

			// Collecting relevant inclusion
			for (Predicate predicate : predicates) {
				Set<PositiveInclusion> relevantInclusion = getRightExistential(predicate);
				// sorting inverse and not inverse
				Set<PositiveInclusion> relevantinverse = new HashSet<PositiveInclusion>(1000);
				Set<PositiveInclusion> relevantnotinverse = new HashSet<PositiveInclusion>(1000);
				for (PositiveInclusion inc : relevantInclusion) {
					DLLiterConceptInclusionImpl samplepi = (DLLiterConceptInclusionImpl) inc;
					ExistentialConceptDescriptionImpl ex = (ExistentialConceptDescriptionImpl) samplepi.getIncluding();
					if (ex.isInverse())
						relevantinverse.add(inc);
					else
						relevantnotinverse.add(inc);
				}

				Set<CQIE> relevantQueries = new HashSet<CQIE>(1000);
				for (CQIE query : oldqueries) {
					if (containsPredicate(query, predicate))
						relevantQueries.add(query);
				}

				newqueries.addAll(piApplicator.applyExistentialInclusions(relevantQueries, relevantinverse));
				newqueries.addAll(piApplicator.applyExistentialInclusions(relevantQueries, relevantnotinverse));
			}

			/*
			 * Removing duplicated atoms in each of the queries to simplify
			 */

			newqueries = CQCUtilities.removeDuplicateAtoms(newqueries);

			/* Removing trivially redundant queries */
			LinkedList<CQIE> newquerieslist = new LinkedList<CQIE>();
			newquerieslist.addAll(newqueries);
			removeContainedQueriesSyntacticSorter(newquerieslist);
			log.debug("Removing trivially redundant queries from this iteration. Initial set size: {} Resulting size: {}", newqueries
					.size(), newquerieslist.size());

			/*
			 * Preparing the set of queries for the next iteration.
			 */

			// loop = loop | result.addAll(newqueries);

			oldqueries = new HashSet<CQIE>(newquerieslist.size() * 2);
			for (CQIE newquery : newquerieslist) {
				if (result.add(newquery)) {
					loop = true;
					oldqueries.add(newquery);
				}
			}

			if (loop) {
				oldqueries = newqueries;
			}
		}
		
		LinkedList<CQIE> resultlist = new LinkedList<CQIE>();
		resultlist.addAll(result);

		/* One last pass of the syntactic containment checker */
		removeContainedQueriesSyntacticSorter(resultlist);
		
		DatalogProgram resultprogram = new DatalogProgramImpl();
		resultprogram.appendRule(resultlist);

		log.debug("Computed reformulation: \n{}", resultprogram);

		return resultprogram;
	}

	private void removeContainedQueriesSyntacticSorter(List<CQIE> queries) {
		Comparator<CQIE> lenghtComparator = new Comparator<CQIE>() {

			@Override
			public int compare(CQIE o1, CQIE o2) {
				return o2.getBody().size() - o1.getBody().size();
			}
		};

		Collections.sort(queries, lenghtComparator);

	}

	private boolean isContainedSyntactic(CQIE cq1, CQIE cq2) {
		if (!cq2.getHead().toString().equals(cq1.getHead().toString())) {
			return false;
		}

		for (Atom atom : cq2.getBody()) {
			if (!cq1.getBody().contains(atom))
				return false;
		}

		return true;

	}

	private HashSet<CQIE> removeContainedQueries(Collection<CQIE> queries) {
		HashSet<CQIE> result = new HashSet<CQIE>(queries.size());

		LinkedList<CQIE> workingcopy = new LinkedList<CQIE>();
		workingcopy.addAll(queries);

		for (int i = 0; i < workingcopy.size(); i++) {
			CQCUtilities cqcutil = new CQCUtilities(workingcopy.get(i));
			for (int j = i + 1; j < workingcopy.size(); j++) {
				if (cqcutil.isContainedIn(workingcopy.get(j))) {
					workingcopy.remove(i);
					i = -1;
					break;
				}

				CQCUtilities cqcutil2 = new CQCUtilities(workingcopy.get(j));
				if (cqcutil2.isContainedIn(workingcopy.get(i)))
					workingcopy.remove(j);

			}
		}
		result.addAll(workingcopy);
		return result;
	}

	private boolean containsPredicate(CQIE q, Predicate predicate) {
		for (Atom atom : q.getBody()) {
			if (atom.getPredicate().equals(predicate))
				return true;
		}
		return q.getHead().getPredicate().equals(predicate);
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
