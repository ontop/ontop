package it.unibz.krdb.obda.owlrefplatform.core.basicoperations;

import java.util.Collection;
import java.util.HashMap;
import java.util.HashSet;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Set;

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

	private final Map<CQIE,FreezeCQ> freezeCQcache = new HashMap<>();
	
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

		Set<Function> derivedAtoms = new HashSet<>();
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
					SubstitutionUtilities.applySubstitution(newFact, theta); // ToGetFact
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
			
			Map<Variable, ValueConstant> substitution = new HashMap<>();
						
			this.head = freezeAtom(head, substitution);

			this.factMap = new HashMap<>(body.size() * 2);

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
			return atom;
			
/*			
			List<Term> newTerms = new LinkedList<>();
			for (Term term : atom.getTerms()) 
				if (term instanceof Function) {
					Function function = (Function) term;
					newTerms.add(freezeAtom(function, substitution));
				}	
				else
					newTerms.add(freezeTerm(term, substitution));		
			
			return fac.getFunction(atom.getFunctionSymbol(), newTerms);
*/
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

		
		private Substitution computeHomomorphism(CQIE query) {
			SubstitutionBuilder sb = new  SubstitutionBuilder();

			boolean headResult = HomomorphismUtilities.extendHomomorphism(sb, query.getHead(), head);
			if (!headResult)
				return null;
			
			Substitution sub = HomomorphismUtilities.computeHomomorphism(sb, query.getBody(), factMap);
			
			return sub;
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
		return (computeHomomorphsim(q1, q2) != null);
	}
	
	@Override
	public Substitution computeHomomorphsim(CQIE q1, CQIE q2) {
		if (!q2.getHead().getFunctionSymbol().equals(q1.getHead().getFunctionSymbol()))
			return null;

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
				return null;
			}
				
		return q1freeze.computeHomomorphism(q2);
	}	
	
	@Override
	public String toString() {
		return sigma.toString();
	}
}
