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

	private final Map<CQIE,FreezeCQ> freezeCQcache = new HashMap<CQIE,FreezeCQ>();
	
	private final LinearInclusionDependencies sigma;
	
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
	public CQCUtilities() {
		sigma = null;
	}
	
	public CQCUtilities(LinearInclusionDependencies sigma) {
		this.sigma = sigma;
	}
	
	
	/**
	 * This method is used to chase foreign key constraint rule in which the rule
	 * has only one atom in the body.
	 * 
	 * IMPORTANT: each rule is applied once to each atom
	 * 
	 * @param rules
	 * @return
	 */
	private static Set<Function> chaseAtoms(Collection<Function> atoms, LinearInclusionDependencies dependencies) {

		Set<Function> derivedAtoms = new HashSet<Function>();
		for (Function fact : atoms) {
			derivedAtoms.add(fact);
			for (CQIE rule : dependencies.getRules(fact.getFunctionSymbol())) {
				Function ruleBody = rule.getBody().get(0);
				Map<Variable, Term> theta = Unifier.getMGU(ruleBody, fact);
				// ESSENTIAL THAT THE RULES IN SIGMA ARE "FRESH" -- see LinearInclusionDependencies.addRule
				if (theta != null && !theta.isEmpty()) {
					Function ruleHead = rule.getHead();
					Function newFact = (Function)ruleHead.clone();
					// unify to get fact is needed because the dependencies are not necessarily full
					// (in other words, they may contain existentials in the head)
					Unifier.applyUnifierToGetFact(newFact, theta); 
					derivedAtoms.add(newFact);
				}
			}
		}
		return derivedAtoms;
	}
	
	public static final class FreezeCQ {
		
		private final Map<Variable, ValueConstant> substitution = new HashMap<Variable, ValueConstant>();
		
		/**
		 * Counter for the naming of the new constants. This
		 * is needed to provide a numbering to each of the new constants
		 */
		private int constantcounter = 1;
		
		private final Function head;
		/***
		 * An index of all the facts obtained by freezing this query.
		 */
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
			this.head = freezeAtom(head);

			factMap = new HashMap<Predicate, List<Function>>(body.size() * 2);

			for (Function atom : body) 
				// not boolean, not algebra, not arithmetic, not datatype
				if (atom != null && atom.isDataFunction()) {
					Function fact = freezeAtom(atom);
					
					Predicate pred = fact.getFunctionSymbol();
					List<Function> facts = factMap.get(pred);
					if (facts == null) {
						facts = new LinkedList<Function>();
						factMap.put(pred, facts);
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
		 *            the commented code is never executed (or does not change anything)
		 * 
		 * @param atom
		 * @return freeze atom
		 */
		private Function freezeAtom(Function atom) {
			atom = (Function) atom.clone();

			List<Term> headterms = atom.getTerms();
			for (int i = 0; i < headterms.size(); i++) {
				Term term = headterms.get(i);
				if (term instanceof Variable) {
					ValueConstant //replacement = null;
//					if (term instanceof Variable) {
						replacement = substitution.get(term);
						if (replacement == null) {
							replacement = getCanonicalConstant(((Variable) term).getName() + constantcounter);
							constantcounter++;
							substitution.put((Variable) term, replacement);
						}
//					} 
//					else if (term instanceof ValueConstant) {
//						replacement = getCanonicalConstant(((ValueConstant) term).getValue() + constantcounter);
//						constantcounter++;
//					} 
//					else if (term instanceof URIConstant) {
//						replacement = getCanonicalConstant(((URIConstant) term).getURI() + constantcounter);
//						constantcounter++;
//					}
					headterms.set(i, replacement);
				} 
				else if (term instanceof Function) {
					Function function = (Function) term;
					List<Term> functionterms = function.getTerms();
					for (int j = 0; j < functionterms.size(); j++) {
						Term fterm = functionterms.get(j);
						if (fterm instanceof Variable) {
							ValueConstant // replacement = null;
//							if (fterm instanceof VariableImpl) {
								replacement = substitution.get(fterm);
								if (replacement == null) {
									replacement = getCanonicalConstant(((VariableImpl)fterm).getName() + constantcounter);
									constantcounter++;
									substitution.put((Variable) fterm, replacement);
								}
//							} 
//							else {
//								replacement = getCanonicalConstant(((ValueConstant)fterm).getValue() + constantcounter);
//								constantcounter++;
//							}
							functionterms.set(j, replacement);
						}
					}
				}
			}
			return atom;
		}

		private static ValueConstant getCanonicalConstant(String nameFragment) {
			return fac.getConstantLiteral("CAN" + nameFragment);		
		}	
	}
	

	
	/***
	 * True if the first query is contained in the second query
	 *    (in other words, the first query is more specific, it has fewer answers)
	 * 
	 * @param q1
	 * @param q2
	 * @return true if the first query is contained in the second query
	 */
	public boolean isContainedIn(CQIE q1, CQIE q2) {

		if (!q2.getHead().getFunctionSymbol().equals(q1.getHead().getFunctionSymbol()))
			return false;

        List<Function> q2body = q2.getBody();
        if (q2body.isEmpty())
            return false;

        FreezeCQ q1freeze = freezeCQcache.get(q1);
        if (q1freeze == null) {
        	Collection<Function> q1body = q1.getBody();
        	if (sigma != null)
        		q1body = chaseAtoms(q1body, sigma);
        	
        	q1freeze = new FreezeCQ(q1.getHead(), q1body);
    		freezeCQcache.put(q1, q1freeze);
        }
           
        for (Function q2atom : q2body) 
			if (!q1freeze.getBodyAtoms().containsKey(q2atom.getFunctionSymbol())) { 
				// in particular, !q2atom.isDataFunction() 
				return false;
			}
				
		return hasAnswer(q1freeze, q2);
	}

    /**
     * TODO!!!
     *
     * @param query
     * @return
     */
	
	private static boolean hasAnswer(FreezeCQ c2cq, CQIE query) {
		
		query = QueryAnonymizer.deAnonymize(query);

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
				factChoices.addAll(c2cq.getBodyAtoms().get(currentPredicate));
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
				
				if (Unifier.getMGU(c2cq.getHead(), newquery.getHead()) == null) {
					
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
				factChoices.addAll(c2cq.getBodyAtoms().get(currentPredicate));
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
	
	//  in TreeRedReformulator and unfolder (foreign keys)
	public List<CQIE> removeContainedQueriesSorted(List<CQIE> queries) {
		// log.debug("Sorting...");
		Collections.sort(queries, new Comparator<CQIE>() {
			@Override
			public int compare(CQIE o1, CQIE o2) {
				return o2.getBody().size() - o1.getBody().size();
			}
		});
		
		return removeContainedQueries(queries);
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
	
	// only in TreeWitnessRewriter
	public List<CQIE> removeContainedQueries(List<CQIE> queries) {

		List<CQIE> result = new LinkedList<CQIE>();
		
		for (CQIE query : queries) {
			boolean add = true;
			ListIterator<CQIE> iterator2 = queries.listIterator(queries.size());
			while (iterator2.hasPrevious()) {
				CQIE query2 = iterator2.previous(); 
				if (query2 == query)
					break;
				if (isContainedIn(query, query2)) {
					add = false;
					break;
				}
			}
			if (add) 
				result.add(query);
		}

		// second pass from the end
		ListIterator<CQIE> iterator = result.listIterator(result.size());
		while (iterator.hasPrevious()) {
			CQIE query = iterator.previous();
			Iterator<CQIE> iterator2 = result.iterator();
			while (iterator2.hasNext()) {
				CQIE query2 = iterator2.next();
				if (query2 == query)
					break;
				if (isContainedIn(query, query2)) {
//					log.debug("REMOVE (SIGMA): " + result.get(i));
					iterator.remove();
					break;
				}
			}
		}
		
		return result;
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
					Map<Variable, Term> theta = Unifier.getMGU(ruleBody, atomQuery);
					if (theta == null || theta.isEmpty()) {
						continue;
					}
					// if unifiable, apply to head of tbox rule
					Function ruleHead = rule.getHead();
					Function copyRuleHead = (Function) ruleHead.clone();
					Unifier.applyUnifier(copyRuleHead, theta);

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
