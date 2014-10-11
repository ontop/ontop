package it.unibz.krdb.obda.owlrefplatform.core.basicoperations;

import java.util.Collection;
import java.util.HashMap;
import java.util.HashSet;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.Stack;

import it.unibz.krdb.obda.model.CQIE;
import it.unibz.krdb.obda.model.Function;
import it.unibz.krdb.obda.model.OBDADataFactory;
import it.unibz.krdb.obda.model.Predicate;
import it.unibz.krdb.obda.model.Term;
import it.unibz.krdb.obda.model.ValueConstant;
import it.unibz.krdb.obda.model.Variable;
import it.unibz.krdb.obda.model.impl.AnonymousVariable;
import it.unibz.krdb.obda.model.impl.OBDADataFactoryImpl;
import it.unibz.krdb.obda.model.impl.VariableImpl;

public class CQContainmentCheckUnderLIDs implements CQContainmentCheck {

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
	public CQContainmentCheckUnderLIDs() {
		sigma = null;
	}
	
	public CQContainmentCheckUnderLIDs(LinearInclusionDependencies sigma) {
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
			
			Map<Variable, ValueConstant> substitution = new HashMap<Variable, ValueConstant>();
						
			this.head = freezeAtom(head, substitution);

			factMap = new HashMap<Predicate, List<Function>>(body.size() * 2);

			for (Function atom : body) 
				// not boolean, not algebra, not arithmetic, not datatype
				if (atom != null && atom.isDataFunction()) {
					Function fact = freezeAtom(atom, substitution);
					
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
		private static Function freezeAtom(Function atom, Map<Variable, ValueConstant> substitution) {
			atom = (Function) atom.clone();

			List<Term> headterms = atom.getTerms();
			for (int i = 0; i < headterms.size(); i++) {
				Term term = headterms.get(i);
				if (term instanceof Variable) {
					ValueConstant replacement;
					if (!(term instanceof AnonymousVariable)) {
						replacement = substitution.get(term);
						if (replacement == null) {
							replacement = fac.getConstantFreshLiteral();
							substitution.put((Variable) term, replacement);
						}
					}
					else 
						replacement = fac.getConstantFreshLiteral();
					headterms.set(i, replacement);
				} 
				else if (term instanceof Function) {
					Function function = (Function) term;
					List<Term> functionterms = function.getTerms();
					for (int j = 0; j < functionterms.size(); j++) {
						Term fterm = functionterms.get(j);
						if (fterm instanceof Variable) {
							ValueConstant replacement;
							if (!(fterm instanceof AnonymousVariable)) {
								replacement = substitution.get(fterm);
								if (replacement == null) {
									replacement = fac.getConstantFreshLiteral();
									substitution.put((Variable) fterm, replacement);
								}
							}
							else
								replacement = fac.getConstantFreshLiteral();
							functionterms.set(j, replacement);
						}
					}
				}
			}
			return atom;
		}

	    /**
	     * TODO!!!
	     *
	     * @param query
	     * @return
	     */
		
		private boolean hasAnswer(CQIE query) {
			
			// query = QueryAnonymizer.deAnonymize(query);

			int bodysize = query.getBody().size();
			
			// TODO: fix facts
			if (bodysize == 0)
				return true;
			
			
			HashMap<Integer, Stack<Function>> choicesMap = new HashMap<Integer, Stack<Function>>(bodysize * 2);
			
			Stack<CQIE> queryStack = new Stack<CQIE>();
			queryStack.push(null);

			CQIE currentQuery = query;
			
			int currentAtomIdx = 0;
			while (currentAtomIdx >= 0) {

				Function currentAtom = currentQuery.getBody().get(currentAtomIdx);							
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
					if (Unifier.getMGU(head, newquery.getHead()) == null) {					
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
				} 
				else {

					if (currentAtomIdx == bodysize - 1) {
						// we found a successful set of facts 
						return true;
					}

					// Advancing to the next index 
					queryStack.push(currentQuery);
					currentAtomIdx += 1;
					currentQuery = newquery;
				}
			}

			return false;
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
	@Override	
	public boolean isContainedIn(CQIE q1, CQIE q2) {

		if (!q2.getHead().getFunctionSymbol().equals(q1.getHead().getFunctionSymbol()))
			return false;

//        List<Function> q2body = q2.getBody();
//        if (q2body.isEmpty())
//           return false;

        FreezeCQ q1freeze = freezeCQcache.get(q1);
        if (q1freeze == null) {
        	Collection<Function> q1body = q1.getBody();
        	if (sigma != null)
        		q1body = chaseAtoms(q1body, sigma);
        	
        	q1freeze = new FreezeCQ(q1.getHead(), q1body);
    		freezeCQcache.put(q1, q1freeze);
        }
           
        for (Function q2atom : q2.getBody()) 
			if (!q1freeze.getBodyAtoms().containsKey(q2atom.getFunctionSymbol())) { 
				// in particular, !q2atom.isDataFunction() 
				return false;
			}
				
		return q1freeze.hasAnswer(q2);
	}	
}
