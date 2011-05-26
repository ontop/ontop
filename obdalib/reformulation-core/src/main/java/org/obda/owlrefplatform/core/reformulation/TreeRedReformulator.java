package org.obda.owlrefplatform.core.reformulation;

import inf.unibz.it.obda.model.Atom;
import inf.unibz.it.obda.model.CQIE;
import inf.unibz.it.obda.model.DatalogProgram;
import inf.unibz.it.obda.model.Predicate;
import inf.unibz.it.obda.model.Query;
import inf.unibz.it.obda.model.impl.DatalogProgramImpl;
import inf.unibz.it.obda.tool.utils.QueryUtils;

import java.util.Collection;
import java.util.HashMap;
import java.util.HashSet;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.obda.owlrefplatform.core.basicoperations.AtomUnifier;
import org.obda.owlrefplatform.core.basicoperations.CQCUtilities;
import org.obda.owlrefplatform.core.basicoperations.PositiveInclusionApplicator;
import org.obda.owlrefplatform.core.basicoperations.QueryAnonymizer;
import org.obda.owlrefplatform.core.ontology.Assertion;
import org.obda.owlrefplatform.core.ontology.ConceptDescription;
import org.obda.owlrefplatform.core.ontology.PositiveInclusion;
import org.obda.owlrefplatform.core.ontology.RoleDescription;
import org.obda.owlrefplatform.core.ontology.imp.AtomicConceptDescriptionImpl;
import org.obda.owlrefplatform.core.ontology.imp.AtomicRoleDescriptionImpl;
import org.obda.owlrefplatform.core.ontology.imp.DLLiterConceptInclusionImpl;
import org.obda.owlrefplatform.core.ontology.imp.DLLiterRoleInclusionImpl;
import org.obda.owlrefplatform.core.ontology.imp.ExistentialConceptDescriptionImpl;
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

	Logger											log							= LoggerFactory.getLogger(TreeRedReformulator.class);

	public TreeRedReformulator(List<Assertion> assertions) {
		this.originalassertions = assertions;
		log.debug("Given assertions: {}", assertions);

		/*
		 * Our strategy requires that for every aciom R ISA S, we also have the
		 * axioms \exists R ISA \exist S and \exists R- ISA \exists S- this
		 * allows us to keep the cycles to a minimum
		 */
		originalassertions.addAll(computeExistentials());

		/*
		 * Our strategy requires saturation to minimize the number of cycles
		 * that will be necessary to compute reformulation.
		 */
		saturateAssertions();

		log.debug("Computed assertions: {}", this.assertions);

		piApplicator = new PositiveInclusionApplicator();
		unifier = new AtomUnifier();
		anonymizer = new QueryAnonymizer();

	}

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

	public Query rewrite(Query input) throws Exception {

		long starttime = System.currentTimeMillis();

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

		log.debug("Removing redundant atoms by query containment");
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

		log.debug("Starting maing rewriting loop");
		boolean loop = true;
		while (loop) {
			loop = false;
			HashSet<CQIE> newqueriesbyPI = new HashSet<CQIE>(1000);

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
					newqueriesbyPI.add(anonymizer.anonymize(newcq));
				}

			}

			/*
			 * Optimizing the set
			 */
			newqueriesbyPI = CQCUtilities.removeDuplicateAtoms(newqueriesbyPI);

			/*
			 * Handling existential inclusions, and unification We will collect
			 * all predicates mantioned in the old queries, we will collect all
			 * the existential inclusions relevant for those predicates and sort
			 * them by inverse and not inverse. Last we apply these groups of
			 * inclusions at the same time. All of them will share one single
			 * unification atempt.
			 */

			HashSet<CQIE> newqueriesbyunificationandPI = new HashSet<CQIE>(1000);

			// Collecting relevant predicates

			HashSet<Predicate> predicates = new HashSet<Predicate>(1000);
			for (CQIE oldquery : oldqueries) {
				for (Atom atom : oldquery.getBody()) {
					predicates.add(atom.getPredicate());
				}
			}

			for (CQIE oldquery : newqueriesbyPI) {
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

				/*
				 * Collecting the relevant queries from the old set, and the
				 * queries that have been just produced
				 */
				Set<CQIE> relevantQueries = new HashSet<CQIE>(1000);
				for (CQIE query : oldqueries) {
					if (containsPredicate(query, predicate))
						relevantQueries.add(query);
				}

				for (CQIE query : newqueriesbyPI) {
					if (containsPredicate(query, predicate))
						relevantQueries.add(query);
				}

				/*
				 * Applying the existential inclusions and unifications
				 * (targeted) removing duplicate atoms before adding the queries
				 * to the set.
				 */

				newqueriesbyunificationandPI.addAll(CQCUtilities.removeDuplicateAtoms(piApplicator.applyExistentialInclusions(
						relevantQueries, relevantinverse)));
				newqueriesbyunificationandPI.addAll(CQCUtilities.removeDuplicateAtoms(piApplicator.applyExistentialInclusions(
						relevantQueries, relevantnotinverse)));
			}

			/*
			 * Removing duplicated atoms in each of the queries to simplify
			 */

			newqueriesbyPI = CQCUtilities.removeDuplicateAtoms(newqueriesbyPI);
			newqueriesbyunificationandPI = CQCUtilities.removeDuplicateAtoms(newqueriesbyunificationandPI);

			/* Removing trivially redundant queries */
			LinkedList<CQIE> newquerieslist = new LinkedList<CQIE>();
			// newquerieslist.addAll(newqueriesbyPI);
			newquerieslist.addAll(newqueriesbyunificationandPI);

			/*
			 * Preparing the set of queries for the next iteration.
			 */

			result.addAll(newqueriesbyPI);

			oldqueries = new HashSet<CQIE>(newquerieslist.size() * 2);
			for (CQIE newquery : newquerieslist) {
				if (result.add(newquery)) {
					loop = true;
					oldqueries.add(newquery);
				}
			}

			// loop = loop | result.addAll(newqueries);
			// if (loop) {
			// oldqueries = newqueries;
			// }
		}
		log.debug("Main loop ended");
		LinkedList<CQIE> resultlist = new LinkedList<CQIE>();
		resultlist.addAll(result);

		/* One last pass of the syntactic containment checker */

		log.debug("Removing trivially contained queries");
		CQCUtilities.removeContainedQueriesSyntacticSorter(resultlist, false);

//		if (resultlist.size() < 300) {
			log.debug("Removing CQC contained queries");
			CQCUtilities.removeContainedQueriesSorted(resultlist, false);
//		}

		DatalogProgram resultprogram = new DatalogProgramImpl();
		resultprogram.appendRule(resultlist);

		long endtime = System.currentTimeMillis();

		QueryUtils.copyQueryModifiers(input, resultprogram);
		
		log.debug("Computed reformulation: \n{}", resultprogram);
		log.debug("Final size of the reformulation: {}", resultlist.size());
		double seconds = (endtime - starttime) / 1000;
		log.info("Time elapsed for reformulation: {}s", seconds);
		
		
		return resultprogram;
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
						} else if (ci1.getIncluded().equals(ci2.getIncluding())) {
							DLLiterConceptInclusionImpl newinclusion = new DLLiterConceptInclusionImpl(ci2.getIncluded(), ci1
									.getIncluding());
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
