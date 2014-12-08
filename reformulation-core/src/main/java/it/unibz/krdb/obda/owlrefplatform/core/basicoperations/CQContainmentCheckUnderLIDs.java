package it.unibz.krdb.obda.owlrefplatform.core.basicoperations;

import java.util.ArrayList;
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

public class CQContainmentCheckUnderLIDs implements CQContainmentCheck {

	private final Map<CQIE,FreezeCQ> freezeCQcache = new HashMap<CQIE,FreezeCQ>();
	
	private final LinearInclusionDependencies sigma;
	
	private static final OBDADataFactory fac = OBDADataFactoryImpl.getInstance();

	/***
	 * Constructs a CQC utility using the given query. If Sigma is not null and
	 * not empty, then it will also be used to verify containment w.r.t.\ Sigma.
	 *
	 */
	public CQContainmentCheckUnderLIDs() {
		sigma = null;
	}

	/**
	 * *@param sigma
	 * A set of ABox dependencies
	 */
	public CQContainmentCheckUnderLIDs(LinearInclusionDependencies sigma) {
		this.sigma = sigma;
	}
	
	
	/**
	 * This method is used to chase foreign key constraint rule in which the rule
	 * has only one atom in the body.
	 * 
	 * IMPORTANT: each rule is applied once to each atom
	 * 
	 * @param atoms
	 * @return
	 */
	private static Set<Function> chaseAtoms(Collection<Function> atoms, LinearInclusionDependencies dependencies) {

		Set<Function> derivedAtoms = new HashSet<Function>();
		for (Function fact : atoms) {
			derivedAtoms.add(fact);
			for (CQIE rule : dependencies.getRules(fact.getFunctionSymbol())) {
				Function ruleBody = rule.getBody().get(0);
				Substitution theta = UnifierUtilities.getMGU(ruleBody, fact);
				// ESSENTIAL THAT THE RULES IN SIGMA ARE "FRESH" -- see LinearInclusionDependencies.addRule
				if (theta != null && !theta.isEmpty()) {
					Function ruleHead = rule.getHead();
					Function newFact = (Function)ruleHead.clone();
					// unify to get fact is needed because the dependencies are not necessarily full
					// (in other words, they may contain existentials in the head)
					SubstitutionUtilities.applySubstitutionToGetFact(newFact, theta);
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
		 */
		
		public FreezeCQ(Function head, Collection<Function> body) { 
			
			Map<Variable, ValueConstant> substitution = new HashMap<Variable, ValueConstant>();
						
			this.head = freezeAtom(head, substitution);

			this.factMap = new HashMap<Predicate, List<Function>>(body.size() * 2);

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
		
		public List<Function> getBodyAtoms(Predicate p) {
			return factMap.get(p);
		}
		
		/***
		 * Replaces each variable inside the atom with a fresh constant. 
		 * 
		 * @param atom
		 * @return freeze atom
		 */
		private static Function freezeAtom(Function atom, Map<Variable, ValueConstant> substitution) {
			
			List<Term> newTerms = new LinkedList<Term>();
			for (Term term : atom.getTerms()) 
				if (term instanceof Function) {
					Function function = (Function) term;
					newTerms.add(freezeAtom(function, substitution));
				}	
				else
					newTerms.add(freezeTerm(term, substitution));		
			
			return fac.getFunction(atom.getFunctionSymbol(), newTerms);
		}

		private static Term freezeTerm(Term term, Map<Variable, ValueConstant> substitution) {
			
			if (term instanceof Variable) {
				// interface Variables is implemented by VariableImpl and AnonymousVariable  
				if (!(term instanceof AnonymousVariable)) {
					ValueConstant replacement = substitution.get(term);
					if (replacement == null) {
						replacement = fac.getConstantFreshLiteral();
						substitution.put((Variable) term, replacement);
					}
					return replacement;
				}
				else
					// AnonymousVariablea are replaced simply by fresh constants  
					return fac.getConstantFreshLiteral();
			}
			else 
				return term;
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
			
			// ROMAN: fix for facts
			if (bodysize == 0) {
				if (UnifierUtilities.getMGU(head, query.getHead()) == null)
					return true;
				return false;
			}
					
			ArrayList<Stack<Function>> choicesMap = new ArrayList<Stack<Function>>(bodysize);
			
			Stack<CQIE> queryStack = new Stack<CQIE>();
			queryStack.push(null);   // to ensure that the last pop works fine

			CQIE currentQuery = query;			
			int currentAtomIdx = 0;
			
			while (currentAtomIdx >= 0) {

				Function currentAtom = currentQuery.getBody().get(currentAtomIdx);							

				// looking for options for this atom 
				Stack<Function> factChoices;
				if (currentAtomIdx >= choicesMap.size()) {			
					// we have never reached this atom, setting up the initial list
					// of choices from the original fact list.				 
					factChoices = new Stack<Function>();
					factChoices.addAll(factMap.get(currentAtom.getFunctionSymbol()));
					choicesMap.add(currentAtomIdx, factChoices);
				}
				else
					factChoices = choicesMap.get(currentAtomIdx);

				boolean choiceMade = false;
				CQIE newquery = null;
				while (!factChoices.isEmpty()) {
					Substitution mgu = UnifierUtilities.getMGU(currentAtom, factChoices.pop());
					if (mgu == null) {
						// No way to use the current fact 
						continue;
					}
					
					newquery = SubstitutionUtilities.applySubstitution(currentQuery, mgu);
					
					// stopping early if we have chosen an MGU that has no
					// possibility of being successful because of the head.				
					if (UnifierUtilities.getMGU(head, newquery.getHead()) == null) {
						// there is no chance to unify the two heads, hence this
						// fact is not good.
						continue;
					}

					// the current fact was applicable, no conflicts so far, we can
					// advance to the next atom
					choiceMade = true;
					break;
				}
				if (!choiceMade) {
					
					// reseting choices state and backtracking and resetting the set
					// of choices for the current position
					factChoices.addAll(factMap.get(currentAtom.getFunctionSymbol()));
					currentAtomIdx--;
					currentQuery = queryStack.pop();
				} 
				else {
					if (currentAtomIdx == bodysize - 1) {
						// we have found a successful set of facts 
						return true;
					}

					// advancing to the next index 
					queryStack.push(currentQuery);
					currentAtomIdx++;
					currentQuery = newquery;
				}
			}
			// exhausted all the choices
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

//		ROMAN: this was plain wrong -- facts can also be contained in queries  
//			   (see the fix in hasAnswer)
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
			if (!q1freeze.factMap.containsKey(q2atom.getFunctionSymbol())) { 
				// in particular, !q2atom.isDataFunction() 
				return false;
			}
				
		return q1freeze.hasAnswer(q2);
	}	
	
	@Override
	public String toString() {
		return sigma.toString();
	}
}
