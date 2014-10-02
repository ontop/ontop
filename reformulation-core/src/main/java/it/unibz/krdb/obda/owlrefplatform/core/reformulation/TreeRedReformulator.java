package it.unibz.krdb.obda.owlrefplatform.core.reformulation;

/*
 * #%L
 * ontop-reformulation-core
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

import it.unibz.krdb.obda.model.CQIE;
import it.unibz.krdb.obda.model.DatalogProgram;
import it.unibz.krdb.obda.model.Function;
import it.unibz.krdb.obda.model.OBDADataFactory;
import it.unibz.krdb.obda.model.OBDAException;
import it.unibz.krdb.obda.model.Predicate;
import it.unibz.krdb.obda.model.impl.OBDADataFactoryImpl;
import it.unibz.krdb.obda.model.impl.OBDAVocabulary;
import it.unibz.krdb.obda.ontology.Ontology;
import it.unibz.krdb.obda.ontology.SubDescriptionAxiom;
import it.unibz.krdb.obda.ontology.impl.OntologyImpl;
import it.unibz.krdb.obda.ontology.impl.PropertySomeRestrictionImpl;
import it.unibz.krdb.obda.ontology.impl.SubClassAxiomImpl;
import it.unibz.krdb.obda.owlrefplatform.core.basicoperations.CQCUtilities;
import it.unibz.krdb.obda.owlrefplatform.core.basicoperations.PositiveInclusionApplicator;
import it.unibz.krdb.obda.owlrefplatform.core.basicoperations.QueryAnonymizer;
import it.unibz.krdb.obda.owlrefplatform.core.basicoperations.SemanticQueryOptimizer;
import it.unibz.krdb.obda.owlrefplatform.core.dagjgrapht.TBoxReasoner;
import it.unibz.krdb.obda.owlrefplatform.core.tboxprocessing.TBoxReasonerToOntology;
import it.unibz.krdb.obda.utils.QueryUtils;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.LinkedList;
import java.util.List;
import java.util.Set;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class TreeRedReformulator implements QueryRewriter {

	private static final QueryAnonymizer anonymizer = new QueryAnonymizer();

	private static final Logger log = LoggerFactory.getLogger(TreeRedReformulator.class);

	private static final OBDADataFactory fac = OBDADataFactoryImpl.getInstance();

	private static final SemanticQueryOptimizer sqoOptimizer = null;

	private static final PositiveInclusionApplicator piApplicator = new PositiveInclusionApplicator(sqoOptimizer);

	/***
	 * The TBox used for reformulating.
	 */
	private Ontology ontology = null;

	/***
	 * The set of ABox dependencies that will be used to optimize the
	 * reformulation.
	 */
	private Ontology sigma = null;

	public TreeRedReformulator() {

		// sqoOptimizer = new SemanticQueryOptimizer(fac, rightAssertionIndex);

	}

	public DatalogProgram rewrite(DatalogProgram prog) throws OBDAException {

		log.debug("Query reformulation started...");

		// Runtime.getRuntime().runFinalization ();
		// Runtime.getRuntime().gc ();
		// Thread.currentThread ().yield ();

		double starttime = System.currentTimeMillis();

		// log.debug("Starting query rewrting. Received query: \n{}", prog);

		if (!prog.isUCQ()) {
			throw new OBDAException("Rewriting exception: The input is not a valid union of conjuctive queries");
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

			// /*
			// * This is a safety check to avoid running out of memory during a
			// * reformulation.
			// */
			// try {
			// MemoryUtils.checkAvailableMemory();
			// } catch (MemoryLowException e) {
			// // TODO Auto-generated catch block
			// e.printStackTrace();
			// }

			loop = false;
			HashSet<CQIE> newqueriesbyPI = new HashSet<CQIE>(1000);

			/*
			 * Handling simple inclusions, none involves existentials on the
			 * right side of an inclusion
			 */
			for (CQIE oldquery : oldqueries) {

				HashSet<SubDescriptionAxiom> relevantInclusions = new HashSet<SubDescriptionAxiom>(1000);
				for (Function atom : oldquery.getBody()) {
					Set<SubDescriptionAxiom> inclusions = ontology.getByIncludingNoExist(((Function) atom).getPredicate());
					if (inclusions != null)
						relevantInclusions.addAll(ontology.getByIncludingNoExist(((Function) atom).getPredicate()));
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
				for (Function atom : oldquery.getBody()) {
					predicates.add(((Function) atom).getPredicate());
				}
			}

			for (CQIE oldquery : newqueriesbyPI) {
				for (Function atom : oldquery.getBody()) {
					predicates.add(((Function) atom).getPredicate());
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
					PropertySomeRestrictionImpl ex = (PropertySomeRestrictionImpl) samplepi.getSuper();
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

		List<CQIE> resultlist = new ArrayList<CQIE>((int) (result.size() * 1.5));
		resultlist.addAll(result);
		log.debug("Main loop ended. Queries produced: {}", resultlist.size());

		// log.debug("Removing auxiliary queries...");
		resultlist = cleanAuxiliaryQueries(resultlist);
		log.debug("Removed auxiliary queries. New size: {}", resultlist.size());

		resultlist = cleanEmptyQueries(resultlist);
		log.debug("Removed empty queries. New size: {}", resultlist.size());

		// log.debug("Reformulation: {}", resultlist);

		/* One last pass of the syntactic containment checker */

		// log.debug("Removing trivially contained queries");
		CQCUtilities.removeContainedQueriesSyntacticSorter(resultlist, false);

		// if (resultlist.size() < 300) {

		if (sigma != null) {
			log.debug("Removing redundant queries by CQC. Using {} ABox dependencies.", sigma.getAssertions().size());
		} else {
			log.debug("Removing redundatnt queries by CQC.");
		}
		resultlist = CQCUtilities.removeContainedQueriesSorted(resultlist, true, sigma);
		// }

		DatalogProgram resultprogram = fac.getDatalogProgram();
		resultprogram.appendRule(resultlist);

		double endtime = System.currentTimeMillis();
		double milliseconds = (endtime - starttime) / 1000;

		QueryUtils.copyQueryModifiers(prog, resultprogram);

		// if (showreformulation)
		// log.debug("Computed reformulation: \n{}", resultprogram);
		log.debug("Reformulation size: {}, Time elapse: {}ms", resultlist.size(), milliseconds);

		// log.info("Time elapsed for reformulation: {}s", seconds);

		return resultprogram;
	}

	private boolean containsPredicate(CQIE q, Predicate predicate) {
		for (Function atom : q.getBody()) {
			if (((Function) atom).getPredicate().equals(predicate))
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
		List<CQIE> newQueries = new ArrayList<CQIE>((int) (originalQueries.size() * 1.5));
		for (CQIE query : originalQueries) {
			List<Function> body = query.getBody();
			boolean auxiliary = false;
			for (Function atom : body) {
				String name = atom.getPredicate().getName().toString();
				if (name.length() < OntologyImpl.AUXROLEURI.length())
					continue;
				if (name.substring(0, OntologyImpl.AUXROLEURI.length()).equals(OntologyImpl.AUXROLEURI)) {
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

	/***
	 * Eliminates queries that must be empty becauase there are unsatisfiable
	 * atoms. At this moment, only queries containing atoms of the form
	 * "xsd:string(?x)" are empty queries.
	 * 
	 * @param originalQueries
	 * @return
	 */
	private List<CQIE> cleanEmptyQueries(List<CQIE> originalQueries) {
		List<CQIE> newQueries = new ArrayList<CQIE>((int) (originalQueries.size() * 1.5));
		for (CQIE query : originalQueries) {
			List<Function> body = query.getBody();
			boolean empty = false;
			for (Function atom : body) {
				Predicate predicate = atom.getPredicate();
				for (Predicate datatype : OBDAVocabulary.QUEST_DATATYPE_PREDICATES) {
					if (predicate == datatype) {
						empty = true;
						break;
					}
					if (empty)
						break;
				}
				if (empty)
					break;
			}
			if (!empty) {
				newQueries.add(query);
			}
		}
		return newQueries;
	}

	@Override
	public void setTBox(TBoxReasoner reasoner, Ontology sigma) {
		this.ontology = TBoxReasonerToOntology.getOntology(reasoner);

		/*
		 * Our strategy requires saturation to minimize the number of cycles
		 * that will be necessary to compute reformulation.
		 */
		this.ontology.saturate();
		
		
		this.sigma = sigma;
		if (this.sigma != null) {
			log.debug("Using {} dependencies.", sigma.getAssertions().size());
			this.sigma.saturate();
		}

	}
}
