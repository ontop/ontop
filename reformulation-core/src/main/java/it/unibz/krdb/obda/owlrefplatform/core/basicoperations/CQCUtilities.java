package it.unibz.krdb.obda.owlrefplatform.core.basicoperations;

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

import it.unibz.krdb.obda.model.*;
import it.unibz.krdb.obda.model.impl.OBDADataFactoryImpl;
import java.util.*;

/***
 * A class that allows you to perform different operations related to query
 * containment on conjunctive queries.
 * 
 * Two usages: 
 *    - simplifying queries with DL atoms
 *    - simplifying mapping queries with SQL atoms
 * 
 * @author Mariano Rodriguez Muro
 * 
 */
public class CQCUtilities {

	private static final OBDADataFactory fac = OBDADataFactoryImpl.getInstance();


	public static final Comparator<CQIE> ComparatorCQIE = new Comparator<CQIE>() {
		@Override
		public int compare(CQIE o1, CQIE o2) {
			return o2.getBody().size() - o1.getBody().size();
		}
	};

	public static final CQContainmentCheckSyntactic SYNTACTIC_CHECK = new CQContainmentCheckSyntactic();
	

	/***
	 * Removes queries that are contained syntactically, using the method
	 * isContainedIn(CQIE q1, CQIE 2). 
	 * 
	 * Removal of queries is done in two main double scans. The first scan goes
	 * top-down/down-top, the second scan goes down-top/top-down
	 * 
	 * @param queries
	 */
	
	public static void removeContainedQueries(List<CQIE> queries, CQContainmentCheck containment) {

		{
			Iterator<CQIE> iterator = queries.iterator();
			while (iterator.hasNext()) {
				CQIE query = iterator.next();
				ListIterator<CQIE> iterator2 = queries.listIterator(queries.size());
				while (iterator2.hasPrevious()) {
					CQIE query2 = iterator2.previous(); 
					if (query2 == query)
						break;
					if (containment.isContainedIn(query, query2)) {
						iterator.remove();
						break;
					}
				}
			}
		}
		{
			// second pass from the end
			ListIterator<CQIE> iterator = queries.listIterator(queries.size());
			while (iterator.hasPrevious()) {
				CQIE query = iterator.previous();
				Iterator<CQIE> iterator2 = queries.iterator();
				while (iterator2.hasNext()) {
					CQIE query2 = iterator2.next();
					if (query2 == query)
						break;
					if (containment.isContainedIn(query, query2)) {
//						log.debug("REMOVE (SIGMA): " + result.get(i));
						iterator.remove();
						break;
					}
				}
			}
		}
	}
	
	/**
	 * 
	 * @param program
	 * @param rules
	 */
	public static void optimizeQueryWithSigmaRules(DatalogProgram program, LinearInclusionDependencies sigma) {
		
		List<CQIE> result = new LinkedList<CQIE>();
		
		// for each rule in the query
		for (CQIE query : program.getRules()) {
			
			// get query head, body
			Function queryHead = query.getHead();
			List<Function> queryBody = query.getBody();
			// for each atom in query body
			for (int i = 0; i < queryBody.size(); i++) {
				Set<Function> atomsToRemove = new HashSet<Function>();
				Function atomQuery = queryBody.get(i);

				// for each tbox rule
				for (CQIE rule : sigma.getRules(atomQuery.getFunctionSymbol())) {
					// try to unify current query body atom with tbox rule body atom
					// ESSENTIAL THAT THE RULES IN SIGMA ARE "FRESH" -- see LinearInclusionDependencies.addRule				
					Function ruleBody = rule.getBody().get(0);
					Unifier theta = UnifierUtilities.getMGU(ruleBody, atomQuery);
					if (theta == null || theta.isEmpty()) {
						continue;
					}
					// if unifiable, apply to head of tbox rule
					Function ruleHead = rule.getHead();
					Function copyRuleHead = (Function) ruleHead.clone();
					UnifierUtilities.applyUnifier(copyRuleHead, theta);

					atomsToRemove.add(copyRuleHead);
				}

				for (int j = 0; j < queryBody.size(); j++) {
					if (j == i) {
						continue;
					}
					Function current = queryBody.get(j);
					if (atomsToRemove.contains(current)) {
						queryBody.remove(j);
						j -= 1;
						if (j < i) {
							i -= 1;
						}
					}
				}
			}
			result.add(fac.getCQIE(queryHead, queryBody));
		}
		program.removeAllRules();
		program.appendRule(result);
	}
	
	
	
	

	
	
	
	
	/***
	 * Returns true if the query can be answered using the ground atoms in the
	 * body of our canonical query. The method is recursive, hence expensive.
	 * 
	 * @param query
	 * @return
	 */
/*
	private boolean hasAnswerRecursive(CQIE query) {

		List<Function> body = query.getBody();

		int atomidx = -1;
		for (Function currentAtomTry : body) {
			atomidx += 1;
			List<Function> relevantFacts = factMap.get(currentAtomTry.getPredicate());
			if (relevantFacts == null)
				return false;

			for (Function currentGroundAtom : relevantFacts) {

				Map<Variable, Term> mgu = Unifier.getMGU(currentAtomTry, currentGroundAtom);
				if (mgu == null)
					continue;

				CQIE satisfiedquery = Unifier.applyUnifier(query, mgu);
				satisfiedquery.getBody().remove(atomidx);

				if (satisfiedquery.getBody().size() == 0)
					if (canonicalhead.equals(satisfiedquery.getHead()))
						return true;
				
				// Stopping early if we have chosen an MGU that has no
				// possibility of being successful because of the head.
				 
				if (Unifier.getMGU(canonicalhead, satisfiedquery.getHead()) == null) {
					continue;
				}
				if (hasAnswerRecursive(satisfiedquery))
					return true;

			}
		}
		return false;
	}
*/
	/***
	 * Implements a stack based, non-recursive query answering mechanism.
	 * 
	 * @param query2
	 * @return
	 */
/*	
	private boolean hasAnswerNonRecursive1(CQIE query2) {

		query2 = QueryAnonymizer.deAnonymize(query2);

		LinkedList<CQIE> querystack = new LinkedList<CQIE>();
		querystack.addLast(query2);
		LinkedList<Integer> lastAttemptIndexStack = new LinkedList<Integer>();
		lastAttemptIndexStack.addLast(0);

		while (!querystack.isEmpty()) {

			CQIE currentquery = querystack.pop();
			List<Function> body = currentquery.getBody();
			int atomidx = lastAttemptIndexStack.pop();
			Function currentAtomTry = body.get(atomidx);

			for (int groundatomidx = 0; groundatomidx < canonicalbody.size(); groundatomidx++) {
				Function currentGroundAtom = (Function) canonicalbody.get(groundatomidx);

				Map<Variable, Term> mgu = Unifier.getMGU(currentAtomTry, currentGroundAtom);
				if (mgu != null) {
					CQIE satisfiedquery = Unifier.applyUnifier(currentquery.clone(), mgu);
					satisfiedquery.getBody().remove(atomidx);

					if (satisfiedquery.getBody().size() == 0) {
						if (canonicalhead.equals(satisfiedquery.getHead())) {
							return true;
						}
					}

				}
			}

		}

		return false;
	}
*/
	
	/***
	 * This method will "chase" a query with respect to a set of ABox
	 * dependencies. This will introduce atoms that are implied by the presence
	 * of other atoms. This operation may introduce an infinite number of new
	 * atoms, since there might be A ISA exists R and exists inv(R) ISA A
	 * dependencies. To avoid infinite cycles we will chase up to a certain depth
	 * only.
	 * 
	 * To improve performance, sigma should already be saturated.
	 * 
	 * @param query
	 * @param sigma
	 * @return
	 */
/*	
	private static Set<Function> chaseAtoms(Collection<Function> body, DataDependencies sigma) {

		Set<Function> newbody = new LinkedHashSet<Function>();
		newbody.addAll(body);

		boolean loop = true;
		while (loop) {
			loop = false;
			for (Function atom : body) {
				if (atom == null) 
					continue;

				Term oldTerm1 = null;
				Term oldTerm2 = null;

				Set<SubDescriptionAxiom> pis = sigma.getByIncluded(atom.getPredicate());
				if (pis == null) 
					continue;
				
				for (SubDescriptionAxiom pi : pis) {

					Description left = pi.getSub();
					if (left instanceof Property) {
						Property lefttRoleDescription = (Property) left;

						if (lefttRoleDescription.isInverse()) {
							oldTerm1 = atom.getTerm(1);
							oldTerm2 = atom.getTerm(0);
						} else {
							oldTerm1 = atom.getTerm(0);
							oldTerm2 = atom.getTerm(1);
						}
					} 
					else if (left instanceof OClass) {
						oldTerm1 = atom.getTerm(0);
					} 
					else if (left instanceof PropertySomeRestriction) {
						PropertySomeRestriction lefttAtomicRole = (PropertySomeRestriction) left;
						if (lefttAtomicRole.isInverse()) {
							oldTerm1 = atom.getTerm(1);
						} else {
							oldTerm1 = atom.getTerm(0);
						}
					} 
					else {
						throw new RuntimeException("ERROR: Unsupported dependnecy: " + pi.toString());
					}

					Description right = pi.getSuper();
					Function newAtom = null;

					if (right instanceof Property) {
						Property rightRoleDescription = (Property) right;
						Predicate newPredicate = rightRoleDescription.getPredicate();
						Term newTerm1 = null;
						Term newTerm2 = null;
						if (rightRoleDescription.isInverse()) {
							newTerm1 = oldTerm2;
							newTerm2 = oldTerm1;
						} else {
							newTerm1 = oldTerm1;
							newTerm2 = oldTerm2;
						}
						newAtom = fac.getFunction(newPredicate, newTerm1, newTerm2);
					} else if (right instanceof OClass) {
						OClass rightAtomicConcept = (OClass) right;
						Term newTerm1 = oldTerm1;
						Predicate newPredicate = rightAtomicConcept.getPredicate();
						newAtom = fac.getFunction(newPredicate, newTerm1);

					} else if (right instanceof PropertySomeRestriction) {
						// Here we need to introduce new variables, for the
						// moment
						// we only do it w.r.t.\ non-anonymous variables.
						// hence we are incomplete in containment detection.

						PropertySomeRestriction rightExistential = (PropertySomeRestriction) right;
						Predicate newPredicate = rightExistential.getPredicate();
						if (rightExistential.isInverse()) {
							Term newTerm1 = fac.getVariableNondistinguished();
							Term newTerm2 = oldTerm1;
							newAtom = fac.getFunction(newPredicate, newTerm1, newTerm2);
						} else {
							Term newTerm1 = oldTerm1;
							Term newTerm2 = fac.getVariableNondistinguished();
							newAtom = fac.getFunction(newPredicate, newTerm1, newTerm2);
						}
					} else if (right instanceof DataType) {
						// Does nothing
					} else if (right instanceof PropertySomeDataTypeRestriction) {
						PropertySomeDataTypeRestriction rightExistential = (PropertySomeDataTypeRestriction) right;
						Predicate newPredicate = rightExistential.getPredicate();
						if (rightExistential.isInverse()) {
							throw new RuntimeException("ERROR: The data property of some restriction should not have an inverse!");
						} else {
							Term newTerm1 = oldTerm1;
							Term newTerm2 = fac.getVariableNondistinguished();
							newAtom = fac.getFunction(newPredicate, newTerm1, newTerm2);
						}
					}
					else 
						throw new RuntimeException("ERROR: Unsupported dependency: " + pi.toString());

					newbody.add(newAtom);

				}
			}
			if (body.size() != newbody.size()) {
				loop = true;
				body = newbody;
				newbody = new LinkedHashSet<Function>();
				newbody.addAll(body);
			}
		}

//		LinkedList<Function> bodylist = new LinkedList<Function>();
//		bodylist.addAll(body);
//		
//		CQIE newquery = fac.getCQIE(query.getHead(), bodylist);
//		newquery.setQueryModifiers(query.getQueryModifiers());

		return newbody;
	}
*/
	
	
}
