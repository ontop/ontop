package org.semanticweb.ontop.owlrefplatform.core.reformulation;

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

import java.util.Iterator;
import java.util.LinkedHashSet;
import java.util.LinkedList;
import java.util.List;

import org.semanticweb.ontop.model.CQIE;
import org.semanticweb.ontop.model.DatalogProgram;
import org.semanticweb.ontop.model.Function;
import org.semanticweb.ontop.model.OBDADataFactory;
import org.semanticweb.ontop.model.OBDAException;
import org.semanticweb.ontop.model.OBDAQuery;
import org.semanticweb.ontop.model.impl.OBDADataFactoryImpl;
import org.semanticweb.ontop.ontology.Axiom;
import org.semanticweb.ontop.ontology.Ontology;
import org.semanticweb.ontop.ontology.SubDescriptionAxiom;
import org.semanticweb.ontop.owlrefplatform.core.basicoperations.PositiveInclusionApplicator;
import org.semanticweb.ontop.owlrefplatform.core.basicoperations.QueryAnonymizer;
import org.semanticweb.ontop.owlrefplatform.core.basicoperations.Unifier;
import org.semanticweb.ontop.utils.QueryUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class DLRPerfectReformulator implements QueryRewriter {

	/**
	 * 
	 */
	private static final long serialVersionUID = -6641916058733198535L;
	private final static QueryAnonymizer anonymizer = new QueryAnonymizer();
	private final static Unifier unifier = new Unifier();

	private final static PositiveInclusionApplicator piApplicator = new PositiveInclusionApplicator();
	private List<Axiom> assertions = new LinkedList<Axiom>();

	private OBDADataFactory fac = OBDADataFactoryImpl.getInstance();

	Logger log = LoggerFactory.getLogger(DLRPerfectReformulator.class);

	public DLRPerfectReformulator() {

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
	private DatalogProgram reformulate(DatalogProgram q) throws OBDAException {

		LinkedHashSet<CQIE> prog = new LinkedHashSet<CQIE>();
		// List<CQIE> queries = q.getRules();
		prog.addAll(q.getRules());
		// Linke<Integer> newRules = new HashSet<Integer>();
		boolean loopagain = true;
		while (loopagain) {
			loopagain = false;
			Iterator<CQIE> it = prog.iterator();
			LinkedHashSet<CQIE> newSet = new LinkedHashSet<CQIE>();
			while (it.hasNext()) {
				CQIE cqie = it.next();
				// newRules.add(cqie);
				List<Function> body = cqie.getBody();
				// Part A
				for (int atomidx = 0; atomidx < body.size(); atomidx++) {
					Function currentAtom = (Function) body.get(atomidx);
					Iterator<Axiom> ait = assertions.iterator();
					while (ait.hasNext()) {
						Axiom ass = ait.next();
						if (ass instanceof SubDescriptionAxiom) {
							SubDescriptionAxiom pi = (SubDescriptionAxiom) ass;
							if (piApplicator.isPIApplicable(pi, currentAtom)) {
								CQIE newquery = piApplicator.applyPI(cqie, pi, atomidx);
								if (!prog.contains(newquery) && !newSet.contains(newquery)) {
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
								if (!prog.contains(newQuery) && !newSet.contains(newQuery)) {
									newSet.add(newQuery);
									loopagain = true;
								}
							}
						}
					}
				}

			}
			// prog.appendRule(newSet);
			// queries = newSet;
			prog.addAll(newSet);
		}

		DatalogProgram out = fac.getDatalogProgram();
		QueryUtils.copyQueryModifiers(q, out);
		for (CQIE rule : prog) {
			out.appendRule(rule);
		}

		return out;
	}

	// if not an instance of DatalogProgramImpl or if not
	// a UCQ then return invalid argument exception
	// if a predicate in the query is not a special predicate and is not
	// in the factory, then
	// reformulates according to PerfectRef
	// #############################

	public OBDAQuery rewrite(OBDAQuery input) throws OBDAException {

		if (!(input instanceof DatalogProgram)) {
			throw new OBDAException("Rewriting exception: The input must be a DatalogProgram instance");
		}

		DatalogProgram prog = (DatalogProgram) input;

		log.debug("Starting query rewrting. Received query: \n{}", prog);

		if (!prog.isUCQ()) {
			throw new OBDAException("Rewriting exception: The input is not a valid union of conjuctive queries");
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
	public void setTBox(Ontology ontology) {
		assertions.clear();
		this.assertions.addAll(ontology.getAssertions());

	}

	@Override
	public void setCBox(Ontology sigma) {
		// This reformulator is not able to handle ABox dependecies

	}

	@Override
	public void initialize() {
	}

}
