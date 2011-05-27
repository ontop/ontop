package org.obda.owlrefplatform.core.reformulation;

import inf.unibz.it.obda.model.Atom;
import inf.unibz.it.obda.model.CQIE;
import inf.unibz.it.obda.model.DatalogProgram;
import inf.unibz.it.obda.model.OBDADataFactory;
import inf.unibz.it.obda.model.Query;
import inf.unibz.it.obda.model.impl.DatalogProgramImpl;
import inf.unibz.it.obda.model.impl.OBDADataFactoryImpl;
import inf.unibz.it.obda.utils.QueryUtils;

import java.util.HashSet;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;

import org.obda.owlrefplatform.core.basicoperations.AtomUnifier;
import org.obda.owlrefplatform.core.basicoperations.PositiveInclusionApplicator;
import org.obda.owlrefplatform.core.basicoperations.QueryAnonymizer;
import org.obda.owlrefplatform.core.ontology.Assertion;
import org.obda.owlrefplatform.core.ontology.PositiveInclusion;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class DLRPerfectReformulator implements QueryRewriter {

	private QueryAnonymizer				anonymizer		= null;
	private AtomUnifier					unifier			= null;
	private PositiveInclusionApplicator	piApplicator	= null;
	private List<Assertion>				assertions		= null;
	
	private OBDADataFactory fac = OBDADataFactoryImpl.getInstance();

	Logger								log				= LoggerFactory.getLogger(DLRPerfectReformulator.class);

	public DLRPerfectReformulator(List<Assertion> ass) {
		this.assertions = ass;
		piApplicator = new PositiveInclusionApplicator();
		unifier = new AtomUnifier();
		anonymizer = new QueryAnonymizer();
	}

	/***
	 * Reformulates the query. Internally, the queries are stored in a List,
	 * however, a HashSet is used in parallel to detect when new queries are
	 * being generated. In the HashSet we store the Integer that identifies the
	 * query's string (getHash).
	 * 
	 * 
	 * @param q
	 * @return
	 * @throws Exception
	 */
	private DatalogProgram reformulate(DatalogProgram q) throws Exception {

		DatalogProgram prog = fac.getDatalogProgram();

		QueryUtils.copyQueryModifiers(q, prog);

		List<CQIE> queries = q.getRules();
		prog.appendRule(queries);
		HashSet<Integer> newRules = new HashSet<Integer>();
		boolean loopagain = true;
		while (loopagain) {
			loopagain = false;
			Iterator<CQIE> it = queries.iterator();
			LinkedList<CQIE> newSet = new LinkedList<CQIE>();
			while (it.hasNext()) {
				CQIE cqie = it.next();
				newRules.add(cqie.hashCode());
				List<Atom> body = cqie.getBody();
				// Part A
				for (int atomidx = 0; atomidx < body.size(); atomidx++) {
					Atom currentAtom = body.get(atomidx);
					Iterator<Assertion> ait = assertions.iterator();
					while (ait.hasNext()) {
						Assertion ass = ait.next();
						if (ass instanceof PositiveInclusion) {
							PositiveInclusion pi = (PositiveInclusion) ass;
							if (piApplicator.isPIApplicable(pi, currentAtom)) {
								CQIE newquery = piApplicator.applyPI(cqie, pi, atomidx);
								if (newRules.add(newquery.hashCode())) {
									newSet.add(newquery);
									loopagain = true;
								}
							}
						}
					}
				}
				// Part B unification
				for (int i = 0; i < body.size(); i++) {
					for (int j = i + 1; j < body.size(); j++) {
						if (i != j) {
							CQIE newQuery = unifier.unify(cqie, i, j);
							if (newQuery != null) {
								anonymizer.anonymize(newQuery, i);
								newSet.add(newQuery);
								loopagain = true;
							}
						}
					}
				}

			}
			// prog.appendRule(newSet);
			queries = newSet;
			prog.appendRule(queries);
		}
		return prog;
	}

	// if not an instance of DatalogProgramImpl or if not
	// a UCQ then return invalid argument exception
	// if a predicate in the query is not a special predicate and is not
	// in the factory, then
	// reformulates according to PerfectRef
	// #############################

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
		QueryAnonymizer ano = new QueryAnonymizer();
		DatalogProgram anonymizedProgram = ano.anonymize(prog);

		log.debug("Reformulating");
		DatalogProgram reformulation = reformulate(anonymizedProgram);
		log.debug("Done reformulating. Output: \n{}", reformulation);

		return reformulation;

	}

	@Override
	public void updateAssertions(List<Assertion> ass) {

		this.assertions = ass;
	}

}
