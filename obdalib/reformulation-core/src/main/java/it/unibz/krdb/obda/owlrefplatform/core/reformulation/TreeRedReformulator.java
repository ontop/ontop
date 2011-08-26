package it.unibz.krdb.obda.owlrefplatform.core.reformulation;

import it.unibz.krdb.obda.model.Atom;
import it.unibz.krdb.obda.model.CQIE;
import it.unibz.krdb.obda.model.DatalogProgram;
import it.unibz.krdb.obda.model.OBDADataFactory;
import it.unibz.krdb.obda.model.Predicate;
import it.unibz.krdb.obda.model.Atom;
import it.unibz.krdb.obda.model.OBDAQuery;
import it.unibz.krdb.obda.model.impl.OBDADataFactoryImpl;
import it.unibz.krdb.obda.owlrefplatform.core.basicoperations.AtomUnifier;
import it.unibz.krdb.obda.owlrefplatform.core.basicoperations.CQCUtilities;
import it.unibz.krdb.obda.owlrefplatform.core.basicoperations.MemoryUtils;
import it.unibz.krdb.obda.owlrefplatform.core.basicoperations.PositiveInclusionApplicator;
import it.unibz.krdb.obda.owlrefplatform.core.basicoperations.QueryAnonymizer;
import it.unibz.krdb.obda.owlrefplatform.core.ontology.Ontology;
import it.unibz.krdb.obda.owlrefplatform.core.ontology.Ontology;
import it.unibz.krdb.obda.owlrefplatform.core.ontology.SubDescriptionAxiom;
import it.unibz.krdb.obda.owlrefplatform.core.ontology.imp.SubClassAxiomImpl;
import it.unibz.krdb.obda.owlrefplatform.core.ontology.imp.PropertySomeDescriptionImpl;
import it.unibz.krdb.obda.owlrefplatform.core.translator.OWLAPI2Translator;
import it.unibz.krdb.obda.utils.QueryUtils;

import java.util.HashSet;
import java.util.LinkedList;
import java.util.List;
import java.util.Set;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class TreeRedReformulator implements QueryRewriter {

	private QueryAnonymizer				anonymizer		= null;
	private AtomUnifier					unifier			= null;
	private PositiveInclusionApplicator	piApplicator	= null;

	Logger								log				= LoggerFactory.getLogger(TreeRedReformulator.class);

	OBDADataFactory						fac				= OBDADataFactoryImpl.getInstance();

	SemanticQueryOptimizer				sqoOptimizer	= null;

	/***
	 * The TBox used for reformulating.
	 */
	private Ontology					ontology		= null;

	/***
	 * The set of ABox dependencies that will be used to optimize the
	 * reformulation.
	 */
	private Ontology				sigma			= null;

	public TreeRedReformulator() {

		unifier = new AtomUnifier();
		anonymizer = new QueryAnonymizer();

		// sqoOptimizer = new SemanticQueryOptimizer(fac, rightAssertionIndex);

		piApplicator = new PositiveInclusionApplicator(sqoOptimizer);

	}

	public OBDAQuery rewrite(OBDAQuery input) throws Exception {
		//		

		log.debug("Query reformulation started...");

		// Runtime.getRuntime().runFinalization ();
		// Runtime.getRuntime().gc ();
		// Thread.currentThread ().yield ();

		double starttime = System.currentTimeMillis();

		if (!(input instanceof DatalogProgram)) {
			throw new Exception("Rewriting exception: The input must be a DatalogProgram instance");
		}

		DatalogProgram prog = (DatalogProgram) input;

		// log.debug("Starting query rewrting. Received query: \n{}", prog);

		if (!prog.isUCQ()) {
			throw new Exception("Rewriting exception: The input is not a valid union of conjuctive queries");
		}

		/* Query preprocessing */

		DatalogProgram anonymizedProgram = anonymizer.anonymize(prog);

		// log.debug("Removing redundant atoms by query containment");
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

		// log.debug("Starting maing rewriting loop");
		boolean loop = true;
		while (loop) {

			/*
			 * This is a safety check to avoid running out of memory during a
			 * reformulation.
			 */
			MemoryUtils.checkAvailableMemory();

			loop = false;
			HashSet<CQIE> newqueriesbyPI = new HashSet<CQIE>(1000);

			/*
			 * Handling simple inclusions, none involves existentials on the
			 * right side of an inclusion
			 */
			for (CQIE oldquery : oldqueries) {

				HashSet<SubDescriptionAxiom> relevantInclusions = new HashSet<SubDescriptionAxiom>(1000);
				for (Atom atom : oldquery.getBody()) {
					Set<SubDescriptionAxiom> inclusions = ontology.getByIncludingNoExist(((Atom) atom).getPredicate());
					if (inclusions != null)
						relevantInclusions.addAll(ontology.getByIncludingNoExist(((Atom) atom).getPredicate()));
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
					predicates.add(((Atom) atom).getPredicate());
				}
			}

			for (CQIE oldquery : newqueriesbyPI) {
				for (Atom atom : oldquery.getBody()) {
					predicates.add(((Atom) atom).getPredicate());
				}
			}

			// Collecting relevant inclusion
			for (Predicate predicate : predicates) {
				Set<SubDescriptionAxiom> relevantInclusion = ontology.getByIncludingExistOnly(predicate);
				if (relevantInclusion == null)
					continue;
				// sorting inverse and not inverse
				Set<SubDescriptionAxiom> relevantinverse = new HashSet<SubDescriptionAxiom>(1000);
				Set<SubDescriptionAxiom> relevantnotinverse = new HashSet<SubDescriptionAxiom>(1000);
				for (SubDescriptionAxiom inc : relevantInclusion) {
					SubClassAxiomImpl samplepi = (SubClassAxiomImpl) inc;
					PropertySomeDescriptionImpl ex = (PropertySomeDescriptionImpl) samplepi.getSuper();
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
			/* These queries are final, no need for new passes on these */
			if (sqoOptimizer != null) {
				result.addAll(sqoOptimizer.optimizeBySQO(newqueriesbyPI));
			} else {
				result.addAll(newqueriesbyPI);
			}

			/*
			 * Preparing the set of queries for the next iteration.
			 */

			newqueriesbyunificationandPI = CQCUtilities.removeDuplicateAtoms(newqueriesbyunificationandPI);
			LinkedList<CQIE> newquerieslist = new LinkedList<CQIE>();
			if (sqoOptimizer != null) {
				newquerieslist.addAll(sqoOptimizer.optimizeBySQO(newqueriesbyunificationandPI));
			} else {
				newquerieslist.addAll(newqueriesbyunificationandPI);
			}

			oldqueries = new HashSet<CQIE>(newquerieslist.size() * 2);
			for (CQIE newquery : newquerieslist) {
				if (result.add(newquery)) {
					loop = true;
					oldqueries.add(newquery);
				}
			}

		}

		List<CQIE> resultlist = new LinkedList<CQIE>();
		resultlist.addAll(result);
		log.debug("Main loop ended. Queries produced: {}", resultlist.size());

		// log.debug("Removing auxiliary queries...");
		resultlist = cleanAuxiliaryQueries(resultlist);
		log.debug("Removed auxiliary queries. New size: {}", resultlist.size());

		/* One last pass of the syntactic containment checker */

		// log.debug("Removing trivially contained queries");
		CQCUtilities.removeContainedQueriesSyntacticSorter(resultlist, false);

		// if (resultlist.size() < 300) {

		if (sigma != null) {
			log.debug("Removing redundant queries by CQC. Using {} ABox dependencies.", sigma.getAssertions().size());
		} else {
			log.debug("Removing redundatnt queries by CQC.");
		}
		CQCUtilities.removeContainedQueriesSorted(resultlist, true, sigma);
		// }

		DatalogProgram resultprogram = fac.getDatalogProgram();
		resultprogram.appendRule(resultlist);

		double endtime = System.currentTimeMillis();
		double seconds = (endtime - starttime) / 1000;

		QueryUtils.copyQueryModifiers(input, resultprogram);

		log.debug("Computed reformulation: \n{}", resultprogram);
		log.debug("Fina size of reformulation: {}, Time elapse: {}s", resultlist.size(), seconds);

		// log.info("Time elapsed for reformulation: {}s", seconds);

		return resultprogram;
	}

	private boolean containsPredicate(CQIE q, Predicate predicate) {
		for (Atom atom : q.getBody()) {
			if (((Atom) atom).getPredicate().equals(predicate))
				return true;
		}
		return q.getHead().getPredicate().equals(predicate);
	}

	/***
	 * Some queries produced by the reformulator might be auxiliary. Queries
	 * that use auxiliary predicates only meant to encode exists.R.C given that
	 * we are not handling natively the qualified existential. In the future we
	 * will do it natively, in the meantime we just remove the aux queries.
	 * 
	 * @param originalQueries
	 * @return
	 */
	private List<CQIE> cleanAuxiliaryQueries(List<CQIE> originalQueries) {
		List<CQIE> newQueries = new LinkedList<CQIE>();
		for (CQIE query : originalQueries) {
			List<Atom> body = query.getBody();
			boolean auxiliary = false;
			for (Atom atom : body) {
				if (atom.getPredicate().getName().toString().substring(0, OWLAPI2Translator.AUXROLEURI.length()).equals(
						OWLAPI2Translator.AUXROLEURI)) {
					auxiliary = true;
					break;
				}
			}
			if (!auxiliary) {
				newQueries.add(query);
			}
		}
		return newQueries;
	}

	@Override
	public void initialize() {
		// nothing to do here
	}

	@Override
	public void setTBox(Ontology ontology) {
		this.ontology = ontology.clone();

		/*
		 * Our strategy requires saturation to minimize the number of cycles
		 * that will be necessary to compute reformulation.
		 */
		this.ontology.saturate();

	}

	@Override
	public void setCBox(Ontology sigma) {

		this.sigma = (Ontology) sigma;
		if (this.sigma != null) {
			log.debug("Using {} dependencies.", sigma.getAssertions().size());
			this.sigma.saturate();
		}

	}

}
