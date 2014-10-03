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
import it.unibz.krdb.obda.ontology.*;
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

	private CQIE canonicalQuery = null;

	private final Set<Predicate> canonicalpredicates = new HashSet<Predicate>(50);

	/***
	 * An index of all the facts obtained by freezing this query.
	 */
	private final Map<Predicate, List<Function>> factMap;

	private List<CQIE> rules = null; // a somewhat non-trivial interaction with the first if in the constructor 

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
	public CQCUtilities(CQIE query, DataDependencies sigma) {
		//this.sigma = sigma;
		if (sigma != null) {
			// log.debug("Using dependencies to chase the query");
			query = chaseQuery(query, sigma);
		}
		canonicalQuery = getCanonicalQuery(query);
		
		factMap = new HashMap<Predicate, List<Function>>(canonicalQuery.getBody().size() * 2);
		for (Function atom : canonicalQuery.getBody()) {
			Function fact = (Function) atom;
			if (!fact.isDataFunction())
				continue;
			
			addFact(fact);
		}
	}

	public CQCUtilities(CQIE query, List<CQIE> rules) {

		factMap = new HashMap<Predicate, List<Function>>();
		
		for (Function b : query.getBody())
			if (b.isBooleanFunction())   
				return;   // here rules may remain null even if the parameter was non-null
		
		this.rules = rules;
		if (rules != null && !rules.isEmpty()) {			
			canonicalQuery = getCanonicalQuery(query);
						
			// Map the facts
			for (Function fact : chaseQuery(canonicalQuery.getBody(), rules)) 
				addFact(fact);
		}
	}

	private void addFact(Function fact) {
		Predicate p = fact.getPredicate();
		canonicalpredicates.add(p);
		List<Function> facts = factMap.get(p);
		if (facts == null) {
			facts = new LinkedList<Function>();
			factMap.put(p, facts);
		}
		facts.add(fact);
	}

	
	/***
	 * This method will "chase" a query with respect to a set of ABox
	 * dependencies. This will introduce atoms that are implied by the presence
	 * of other atoms. This operation may introduce an infinite number of new
	 * atoms, since there might be A ISA exists R and exists inv(R) ISA A
	 * dependencies. To avoid infinite cyles we will chase up to a certain depth
	 * only.
	 * 
	 * To improve performance, sigma should already be saturated.
	 * 
	 * @param query
	 * @param sigma
	 * @return
	 */
	private static CQIE chaseQuery(CQIE query, DataDependencies sigma) {

		LinkedHashSet<Function> body = new LinkedHashSet<Function>();
		body.addAll(query.getBody());

		LinkedHashSet<Function> newbody = new LinkedHashSet<Function>();
		newbody.addAll(query.getBody());

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

		LinkedList<Function> bodylist = new LinkedList<Function>();
		bodylist.addAll(body);
		
		CQIE newquery = fac.getCQIE(query.getHead(), bodylist);
		newquery.setQueryModifiers(query.getQueryModifiers());

		return newquery;
	}

	/**
	 * This method is used to chase foreign key constraint rule in which the rule
	 * has only one atom in the body.
	 * 
	 * @param rules
	 * @return
	 */
	private static List<Function> chaseQuery(List<Function> body, List<CQIE> rules) {

		List<Function> facts = new ArrayList<Function>();
		for (Function fact : body) {
			facts.add(fact);
			for (CQIE rule : rules) {
				Function ruleBody = rule.getBody().get(0);
				Map<Variable, Term> theta = Unifier.getMGU(ruleBody, fact);
				if (theta != null && !theta.isEmpty()) {
					Function ruleHead = rule.getHead();
					Function newFact = (Function)ruleHead.clone();
					Unifier.applyUnifierToGetFact(newFact, theta);
					facts.add(newFact);
				}
			}
		}
		return facts;
	}
	
	/***
	 * Computes a query in which all variables have been replaced by
	 * ValueConstants that have the no type and have the same 'name' as the
	 * original variable.
	 * 
	 * This new query can be used for query containment checking.
	 * 
	 * @param q
	 */
	public static CQIE getCanonicalQuery(CQIE q) { // public only for the test
		CQIE canonicalquery = q.clone();

		int constantcounter = 1;

		Map<Variable, Term> substitution = new HashMap<Variable, Term>(50);
		Function head = canonicalquery.getHead();
		constantcounter = getCanonicalAtom(head, constantcounter, substitution);

		for (Function atom : canonicalquery.getBody()) {
			constantcounter = getCanonicalAtom(atom, constantcounter, substitution);
		}
		return canonicalquery;
	}

	/***
	 * Replaces each Variable inside the atom with a constant. It will return
	 * the counter updated + n, where n is the number of constants introduced by
	 * the method.
	 * 
	 * @param atom
	 * @param constantcounter
	 *            an initial counter for the naming of the new constants. This
	 *            is needed to provide a numbering to each of the new constants
	 * @return
	 */
	private static int getCanonicalAtom(Function atom, int constantcounter, Map<Variable, Term> currentMap) {
		List<Term> headterms = atom.getTerms();
		for (int i = 0; i < headterms.size(); i++) {
			Term term = headterms.get(i);
			if (term instanceof Variable) {
				Term substitution = null;
				if (term instanceof Variable) {
					substitution = currentMap.get(term);
					if (substitution == null) {
						ValueConstant newconstant = fac.getConstantLiteral("CAN" + ((Variable) term).getName() + constantcounter);
						constantcounter += 1;
						currentMap.put((Variable) term, newconstant);
						substitution = newconstant;
					}
				} else if (term instanceof ValueConstant) {
					ValueConstant newconstant = fac.getConstantLiteral("CAN" + ((ValueConstant) term).getValue() + constantcounter);
					constantcounter += 1;
					substitution = newconstant;
				} else if (term instanceof URIConstant) {
					ValueConstant newconstant = fac.getConstantLiteral("CAN" + ((URIConstant) term).getURI() + constantcounter);
					constantcounter += 1;
					substitution = newconstant;
				}
				headterms.set(i, substitution);
			} else if (term instanceof Function) {
				Function function = (Function) term;
				List<Term> functionterms = function.getTerms();
				for (int j = 0; j < functionterms.size(); j++) {
					Term fterm = functionterms.get(j);
					if (fterm instanceof Variable) {
						Term substitution = null;
						if (fterm instanceof VariableImpl) {
							substitution = currentMap.get(fterm);
							if (substitution == null) {
								ValueConstant newconstant = fac.getConstantLiteral("CAN" + ((VariableImpl) fterm).getName()
										+ constantcounter);
								constantcounter += 1;
								currentMap.put((Variable) fterm, newconstant);
								substitution = newconstant;

							}
						} else {
							ValueConstant newconstant = fac.getConstantLiteral("CAN" + ((ValueConstant) fterm).getValue()
									+ constantcounter);
							constantcounter += 1;
							substitution = newconstant;
						}
						functionterms.set(j, substitution);
					}
				}
			}
		}
		return constantcounter;
	}

	/***
	 * True if the query used to construct this CQCUtilities object is is
	 * contained in 'query'.
	 * 
	 * @param query
	 * @return
	 */
	public boolean isContainedIn(CQIE query) {

		if (!query.getHead().getFunctionSymbol().equals(canonicalQuery.getHead().getFunctionSymbol()))
			return false;

        List<Function> body = query.getBody();
        if (body.isEmpty())
            return false;
        
        for (Function queryatom : body) {
			if (!canonicalpredicates.contains(queryatom.getFunctionSymbol())) {
				return false;
			}
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
		Function currentAtom = null;
		queryStack.push(null);

		HashMap<Integer, Stack<Function>> choicesMap = new HashMap<Integer, Stack<Function>>(bodysize * 2);

		if (currentBody.size() == 0)
			return true;

		int currentAtomIdx = 0;
		while (currentAtomIdx >= 0) {
			if (currentAtomIdx > maxIdxReached)
				maxIdxReached = currentAtomIdx;

			currentAtom = currentBody.get(currentAtomIdx);			
				
			Predicate currentPredicate = currentAtom.getPredicate();

			// Looking for options for this atom 
			Stack<Function> factChoices = choicesMap.get(currentAtomIdx);
			if (factChoices == null) {
				
				// we have never reached this atom, setting up the initial list
				// of choices from the original fact list.
				 
				factChoices = new Stack<Function>();
				factChoices.addAll(factMap.get(currentPredicate));
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
				
				if (Unifier.getMGU(canonicalQuery.getHead(), newquery.getHead()) == null) {
					
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
				factChoices.addAll(factMap.get(currentPredicate));
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

	
	

	
	public static DatalogProgram removeContainedQueriesSorted(DatalogProgram program, boolean twopasses, List<CQIE> foreignKeyRules) {
		DatalogProgram result = OBDADataFactoryImpl.getInstance().getDatalogProgram();
		result.setQueryModifiers(program.getQueryModifiers());
		List<CQIE> rules = new LinkedList<CQIE>();
		rules.addAll(program.getRules());
		rules = removeContainedQueries(rules, twopasses, foreignKeyRules, true);
		result.appendRule(rules);
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
	public static List<CQIE> removeContainedQueriesSorted(List<CQIE> queries, boolean twopasses, DataDependencies sigma) {
		return removeContainedQueries(queries, twopasses, sigma, true);
	}

	public static List<CQIE> removeContainedQueries(List<CQIE> queriesInput, boolean twopasses, DataDependencies sigma) {
		return removeContainedQueries(queriesInput, twopasses, sigma, false);
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
	 * @param queriesInput
	 */
	public static List<CQIE> removeContainedQueries(List<CQIE> queriesInput, boolean twopasses, List<CQIE> rules, boolean sort) {

		List<CQIE> queries = new LinkedList<CQIE>();
		queries.addAll(queriesInput);
		
//		int initialsize = queries.size();
//		log.debug("Optimzing w.r.t. CQC. Initial size: {}:", initialsize);
//		double startime = System.currentTimeMillis();

		if (sort) {
			// log.debug("Sorting...");
			Collections.sort(queries, lenghtComparator);
		}

		
		if (rules != null && !rules.isEmpty()) {
			for (int i = 0; i < queries.size(); i++) {
				CQIE query = queries.get(i);
				CQCUtilities cqc = new CQCUtilities(query, rules);
				if (cqc.rules != null)
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

			if (twopasses) {
				for (int i = (queries.size() - 1); i >= 0; i--) {
					CQCUtilities cqc = new CQCUtilities(queries.get(i), rules);
					if (cqc.rules != null)
					for (int j = 0; j < i; j++) {
						if (cqc.isContainedIn(queries.get(j))) {
//							log.debug("REMOVE (FK): " + queries.get(i));
							queries.remove(i);
							break;
						}
					}
				}
			}
		}
		
//		int newsize = queries.size();
//		double endtime = System.currentTimeMillis();
//		double time = (endtime - startime) / 1000;
//		log.debug("Resulting size: {}  Time elapsed: {}", newsize, time);
		
		return queries;
	}
	
	public static List<CQIE> removeContainedQueries(List<CQIE> queriesInput, boolean twopasses, DataDependencies sigma, boolean sort) {

		List<CQIE> queries = new LinkedList<CQIE>();
		queries.addAll(queriesInput);
		
//		int initialsize = queries.size();
//		log.debug("Optimzing w.r.t. CQC. Initial size: {}:", initialsize);
//		double startime = System.currentTimeMillis();

		if (sort) {
			// log.debug("Sorting...");
			Collections.sort(queries, lenghtComparator);
		}

		if (sigma != null) {
			for (int i = 0; i < queries.size(); i++) {
				CQIE query = queries.get(i);
				CQCUtilities cqc = new CQCUtilities(query, sigma);
				for (int j = queries.size() - 1; j > i; j--) {
					CQIE query2 = queries.get(j);
					if (cqc.isContainedIn(query2)) {
//						log.debug("REMOVE (SIGMA): " + queries.get(i));
						queries.remove(i);
						i -= 1;
						break;
					}
				}
			}
	
			if (twopasses) {
				for (int i = (queries.size() - 1); i >= 0; i--) {
					CQCUtilities cqc = new CQCUtilities(queries.get(i), sigma);
					for (int j = 0; j < i; j++) {
						if (cqc.isContainedIn(queries.get(j))) {
//							log.debug("REMOVE (SIGMA): " + queries.get(i));
							queries.remove(i);
							break;
						}
					}
				}
			}
		}
//		int newsize = queries.size();
//		double endtime = System.currentTimeMillis();
//		double time = (endtime - startime) / 1000;
//		log.debug("Resulting size: {}  Time elapsed: {}", newsize, time);
		
		return queries;
	}
}
