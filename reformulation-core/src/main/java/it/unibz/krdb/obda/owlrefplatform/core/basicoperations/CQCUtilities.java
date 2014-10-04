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
import it.unibz.krdb.obda.model.impl.VariableImpl;
import it.unibz.krdb.obda.owlrefplatform.core.dagjgrapht.DataDependencies;

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

	private final Function freezeHead;
	
	/***
	 * An index of all the facts obtained by freezing this query.
	 */
	private final Map<Predicate, List<Function>> freezeChasedBody;

	private static final OBDADataFactory fac = OBDADataFactoryImpl.getInstance();

	/***
	 * Constructs a CQC utility using the given query. If Sigma is not null and
	 * not empty, then it will also be used to verify containment w.r.t.\ Sigma.
	 * 
	 * @param query
	 *            A conjunctive query
	 * @param sigma
	 *            A set of ABox dependencies
	 */
	public CQCUtilities(CQIE query) {
		FreezeCQ c2cq = new FreezeCQ(query.getHead(), query.getBody());
		freezeHead = c2cq.getHead();	
		freezeChasedBody = c2cq.getBodyAtoms();
	}
	
	public CQCUtilities(CQIE query, DataDependencies sigma) {
		Set<Function> chasedBody = chaseAtoms(query.getBody(), sigma.getRules());
		
		FreezeCQ c2cq = new FreezeCQ(query.getHead(), chasedBody);
		freezeHead = c2cq.getHead();	
		freezeChasedBody = c2cq.getBodyAtoms();
	}

	private CQCUtilities(CQIE query, List<CQIE> rules) {
		// rules are applied only once to each atom (no saturation)
		Set<Function> chasedBody = chaseAtoms(query.getBody(), rules);
		
		FreezeCQ c2cq = new FreezeCQ(query.getHead(), chasedBody);
		freezeHead = c2cq.getHead();
		freezeChasedBody = c2cq.getBodyAtoms();				
	}

	
	private static boolean isOptimizable(CQIE query) {
		for (Function b : query.getBody())
			if (b.isBooleanFunction())   
				return false;   
		return true;
	}

	
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
	/**
	 * This method is used to chase foreign key constraint rule in which the rule
	 * has only one atom in the body.
	 * 
	 * IMPORTANT: each rule is applied once to each atom
	 * 
	 * @param rules
	 * @return
	 */
	private static Set<Function> chaseAtoms(Collection<Function> atoms, List<CQIE> rules) {

		Set<Function> derivedAtoms = new HashSet<Function>();
		for (Function fact : atoms) {
			derivedAtoms.add(fact);
			for (CQIE rule : rules) {
				Function ruleBody = rule.getBody().get(0);
				Map<Variable, Term> theta = Unifier.getMGU(ruleBody, fact);
				// ESSENTIAL THAT THE RULES IN SIGMA ARE "FRESH"
				if (theta != null && !theta.isEmpty()) {
					Function ruleHead = rule.getHead();
					Function newFact = (Function)ruleHead.clone();
					Unifier.applyUnifierToGetFact(newFact, theta); 
					derivedAtoms.add(newFact);
				}
			}
		}
		return derivedAtoms;
	}
	
	public static final class FreezeCQ {
		
		private final Map<Variable, ValueConstant> substitution = new HashMap<Variable, ValueConstant>(50);
		
		/**
		 * Counter for the naming of the new constants. This
		 * is needed to provide a numbering to each of the new constants
		 */
		private int constantcounter = 1;
		
		private final Function head;
		private final Map<Predicate, List<Function>> factMap;
		
		/***
		 * Computes a query in which all terms have been replaced by
		 * ValueConstants that have the no type and have the same 'name' as the
		 * original variable.
		 * 
		 * This new query can be used for query containment checking.
		 * 
		 * @param q
		 */
		
		public FreezeCQ(Function head, Collection<Function> body) { 
			this.head = (Function) head.clone();
			freezeAtom(this.head);

			factMap = new HashMap<Predicate, List<Function>>(body.size() * 2);

			for (Function atom : body) 
				// not boolean, not algebra, not arithmetic, not datatype
				if (atom != null && atom.isDataFunction()) {
					Function fact = (Function) atom.clone();
					freezeAtom(fact);
					
					Predicate p = fact.getPredicate();
					List<Function> facts = factMap.get(p);
					if (facts == null) {
						facts = new LinkedList<Function>();
						factMap.put(p, facts);
					}
					facts.add(fact);
			}
		}
		
		public Function getHead() {
			return head;
		}
		
		public Map<Predicate, List<Function>> getBodyAtoms() {
			return factMap;
		}
		
		/***
		 * Replaces each term inside the atom with a constant. 
		 * 
		 * IMPORTANT: this method goes only to level 2 of terms
		 * 
		 * @param atom
		 */
		private void freezeAtom(Function atom) {
			List<Term> headterms = atom.getTerms();
			for (int i = 0; i < headterms.size(); i++) {
				Term term = headterms.get(i);
				if (term instanceof Variable) {
					ValueConstant replacement = null;
					if (term instanceof Variable) {
						replacement = substitution.get(term);
						if (replacement == null) {
							replacement = getCanonicalConstant(((Variable) term).getName() + constantcounter);
							constantcounter++;
							substitution.put((Variable) term, replacement);
						}
					} 
					else if (term instanceof ValueConstant) {
						replacement = getCanonicalConstant(((ValueConstant) term).getValue() + constantcounter);
						constantcounter++;
					} 
					else if (term instanceof URIConstant) {
						replacement = getCanonicalConstant(((URIConstant) term).getURI() + constantcounter);
						constantcounter++;
					}
					headterms.set(i, replacement);
				} 
				else if (term instanceof Function) {
					Function function = (Function) term;
					List<Term> functionterms = function.getTerms();
					for (int j = 0; j < functionterms.size(); j++) {
						Term fterm = functionterms.get(j);
						if (fterm instanceof Variable) {
							ValueConstant replacement = null;
							if (fterm instanceof VariableImpl) {
								replacement = substitution.get(fterm);
								if (replacement == null) {
									replacement = getCanonicalConstant(((VariableImpl)fterm).getName() + constantcounter);
									constantcounter++;
									substitution.put((Variable) fterm, replacement);
								}
							} 
							else {
								replacement = getCanonicalConstant(((ValueConstant)fterm).getValue() + constantcounter);
								constantcounter++;
							}
							functionterms.set(j, replacement);
						}
					}
				}
			}
		}

		private static ValueConstant getCanonicalConstant(String nameFragment) {
			return fac.getConstantLiteral("CAN" + nameFragment);		
		}
		
	}
	

	
	/***
	 * True if the query used to construct this CQCUtilities object is is
	 * contained in 'query'.
	 * 
	 * @param query
	 * @return
	 */
	public boolean isContainedIn(CQIE query) {

		if (!query.getHead().getFunctionSymbol().equals(freezeHead.getFunctionSymbol()))
			return false;

        List<Function> body = query.getBody();
        if (body.isEmpty())
            return false;
        
        for (Function queryatom : body) {
			if (!freezeChasedBody.containsKey(queryatom.getFunctionSymbol())) 
				return false;
		}

		return hasAnswer(query);
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
    /**
     * TODO!!!
     *
     * @param query
     * @return
     */
	
	private boolean hasAnswer(CQIE query) {
		query = query.clone();
		QueryAnonymizer.deAnonymize(query);

		int bodysize = query.getBody().size();
		int maxIdxReached = -1;
		Stack<CQIE> queryStack = new Stack<CQIE>();
		CQIE currentQuery = query;
		List<Function> currentBody = currentQuery.getBody();
		queryStack.push(null);

		HashMap<Integer, Stack<Function>> choicesMap = new HashMap<Integer, Stack<Function>>(bodysize * 2);

		if (currentBody.size() == 0)
			return true;

		int currentAtomIdx = 0;
		while (currentAtomIdx >= 0) {
			if (currentAtomIdx > maxIdxReached)
				maxIdxReached = currentAtomIdx;

			Function currentAtom = currentBody.get(currentAtomIdx);							
			Predicate currentPredicate = currentAtom.getPredicate();

			// Looking for options for this atom 
			Stack<Function> factChoices = choicesMap.get(currentAtomIdx);
			if (factChoices == null) {
				
				// we have never reached this atom, setting up the initial list
				// of choices from the original fact list.
				 
				factChoices = new Stack<Function>();
				factChoices.addAll(freezeChasedBody.get(currentPredicate));
				choicesMap.put(currentAtomIdx, factChoices);
			}

			boolean choiceMade = false;
			CQIE newquery = null;
			while (!factChoices.isEmpty()) {
				Map<Variable, Term> mgu = Unifier.getMGU(currentAtom, factChoices.pop());
				if (mgu == null) {
					// No way to use the current fact 
					continue;
				}
				newquery = Unifier.applyUnifier(currentQuery, mgu);
				
				// Stopping early if we have chosen an MGU that has no
				// possibility of being successful because of the head.
				
				if (Unifier.getMGU(freezeHead, newquery.getHead()) == null) {
					
					// There is no chance to unify the two heads, hence this
					// fact is not good.
					continue;
				}
				
				// The current fact was applicable, no conflicts so far, we can
				// advance to the next atom
				choiceMade = true;
				break;

			}
			if (!choiceMade) {
				
				// Reseting choices state and backtracking and resetting the set
				// of choices for the current position
				factChoices.addAll(freezeChasedBody.get(currentPredicate));
				currentAtomIdx -= 1;
				if (currentAtomIdx == -1)
					break;
				currentQuery = queryStack.pop();
				currentBody = currentQuery.getBody();

			} else {

				if (currentAtomIdx == bodysize - 1) {
					// we found a successful set of facts 
					return true;
				}

				// Advancing to the next index 
				queryStack.push(currentQuery);
				currentAtomIdx += 1;
				currentQuery = newquery;
				currentBody = currentQuery.getBody();
			}
		}

		return false;
	}


	private static final Comparator<CQIE> lenghtComparator = new Comparator<CQIE>() {
		@Override
		public int compare(CQIE o1, CQIE o2) {
			return o2.getBody().size() - o1.getBody().size();
		}
	};

	
	

	
	public static DatalogProgram removeContainedQueriesSorted(DatalogProgram program, List<CQIE> foreignKeyRules) {
		
		if (foreignKeyRules == null || foreignKeyRules.isEmpty()) 
			return program;
				
		DatalogProgram result = OBDADataFactoryImpl.getInstance().getDatalogProgram();
		result.setQueryModifiers(program.getQueryModifiers());
		List<CQIE> queries = new LinkedList<CQIE>();
		queries.addAll(program.getRules());
		
		// log.debug("Sorting...");
		Collections.sort(queries, lenghtComparator);
				
		removeContainedQueries(queries, foreignKeyRules);
		
		result.appendRule(queries);
		return result;
	}
	
	/***
	 * Removes queries that are contained syntactically, using the method
	 * isContainedInSyntactic(CQIE q1, CQIE 2). To make the process more
	 * efficient, we first sort the list of queries as to have longer queries
	 * first and shorter queries last.
	 * 
	 * Removal of queries is done in two main double scans. The first scan goes
	 * top-down/down-top, the second scan goes down-top/top-down
	 * 
	 * @param queries
	 */
	
	// only in TreeRedReformulator
	public static List<CQIE> removeContainedQueriesSorted(List<CQIE> queries, DataDependencies sigma) {
		
		if (sigma == null) 
			return queries;
		
		List<CQIE> queriesCopy = new LinkedList<CQIE>();
		queriesCopy.addAll(queries);
		
		// log.debug("Sorting...");
		Collections.sort(queriesCopy, lenghtComparator);
		
		removeContainedQueriesInternal(queriesCopy, sigma);
		
		return queriesCopy;
	}

	// only in TreeWitnessRewriter
	public static List<CQIE> removeContainedQueries(List<CQIE> queries, DataDependencies sigma) {
		
		if (sigma == null) 
			return queries;
		
		List<CQIE> queriesCopy = new LinkedList<CQIE>();
		queriesCopy.addAll(queries);
		
		removeContainedQueriesInternal(queriesCopy, sigma);
		
		return queriesCopy;
	}

	
	/***
	 * Removes queries that are contained syntactically, using the method
	 * isContainedInSyntactic(CQIE q1, CQIE 2). To make the process more
	 * efficient, we first sort the list of queries as to have longer queries
	 * first and shorter queries last.
	 * 
	 * Removal of queries is done in two main double scans. The first scan goes
	 * top-down/down-top, the second scan goes down-top/top-down
	 * 
	 * @param queries
	 */
	private static void removeContainedQueries(List<CQIE> queries, List<CQIE> rules) {

//		int initialsize = queries.size();
//		log.debug("Optimzing w.r.t. CQC. Initial size: {}:", initialsize);
//		double startime = System.currentTimeMillis();

		for (int i = 0; i < queries.size(); i++) {
			CQIE query = queries.get(i);
			if (isOptimizable(query))   {
				CQCUtilities cqc = new CQCUtilities(query, rules);
				for (int j = queries.size() - 1; j > i; j--) {
					CQIE query2 = queries.get(j);
					if (cqc.isContainedIn(query2)) {
//						log.debug("REMOVE (FK): " + queries.get(i));
						queries.remove(i);
						i -= 1;
						break;
					}
				}
			}
		}

		// second pass
		for (int i = (queries.size() - 1); i >= 0; i--) {
			CQIE query = queries.get(i);
			if (isOptimizable(query))   {
				CQCUtilities cqc = new CQCUtilities(query, rules);
				for (int j = 0; j < i; j++) {
					if (cqc.isContainedIn(queries.get(j))) {
//						log.debug("REMOVE (FK): " + queries.get(i));
						queries.remove(i);
						break;
					}
				}
			}
		}
		
//		int newsize = queries.size();
//		double endtime = System.currentTimeMillis();
//		double time = (endtime - startime) / 1000;
//		log.debug("Resulting size: {}  Time elapsed: {}", newsize, time);
	}
	
	private static void removeContainedQueriesInternal(List<CQIE> queries, DataDependencies sigma) {

		for (int i = 0; i < queries.size(); i++) {
			CQIE query = queries.get(i);
			CQCUtilities cqc = new CQCUtilities(query, sigma);
			for (int j = queries.size() - 1; j > i; j--) {
				CQIE query2 = queries.get(j);
				if (cqc.isContainedIn(query2)) {
//					log.debug("REMOVE (SIGMA): " + queries.get(i));
					queries.remove(i);
					i -= 1;
					break;
				}
			}
		}

		for (int i = (queries.size() - 1); i >= 0; i--) {
			CQCUtilities cqc = new CQCUtilities(queries.get(i), sigma);
			for (int j = 0; j < i; j++) {
				if (cqc.isContainedIn(queries.get(j))) {
//					log.debug("REMOVE (SIGMA): " + queries.get(i));
					queries.remove(i);
					break;
				}
			}
		}
	}
}
