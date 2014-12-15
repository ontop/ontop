package it.unibz.krdb.obda.owlrefplatform.core.mappingprocessing;

import it.unibz.krdb.obda.model.BuiltinPredicate;
import it.unibz.krdb.obda.model.CQIE;
import it.unibz.krdb.obda.model.Constant;
import it.unibz.krdb.obda.model.Function;
import it.unibz.krdb.obda.model.OBDADataFactory;
import it.unibz.krdb.obda.model.Term;
import it.unibz.krdb.obda.model.Variable;
import it.unibz.krdb.obda.model.impl.OBDADataFactoryImpl;
import it.unibz.krdb.obda.owlrefplatform.core.basicoperations.CQContainmentCheckUnderLIDs;
import it.unibz.krdb.obda.owlrefplatform.core.basicoperations.EQNormalizer;
import it.unibz.krdb.obda.owlrefplatform.core.basicoperations.Substitution;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;

/***
 * Splits a given {@link mapping} into builtin predicates ({@link conditions})
 * and all other atoms ({@link stripped}), which are checked for containment 
 * by the TMapping construction algorithm.
 */

public class TMappingRule {
	
	private static final OBDADataFactory fac = OBDADataFactoryImpl.getInstance();
	
	private final Function head;
	private final List<Function> databaseAtoms;	
	private final CQIE stripped;
	private final List<Function> filterAtoms;	
	private final CQContainmentCheckUnderLIDs cqc;   

	
	/***
	 * Given a mappings in {@link currentMapping}, this method will
	 * return a new mappings in which no constants appear in the body of
	 * database predicates. This is done by replacing the constant occurrence
	 * with a fresh variable, and adding a new equality condition to the body of
	 * the mapping.
	 * <p/>
	 * 
	 * For example, let the mapping m be
	 * <p/>
	 * A(x) :- T(x,y,22)
	 * 
	 * <p>
	 * Then this method will replace m by the mapping m'
	 * <p>
	 * A(x) :- T(x,y,z), EQ(z,22)
	 * 
	 */
	
	public TMappingRule(Function head, List<Function> body, CQContainmentCheckUnderLIDs cqc) {
		this.filterAtoms = new ArrayList<>(body.size()); // we estimate the size
		this.databaseAtoms = new ArrayList<>(body.size());
		
		int freshVarCount = 0;
		
		for (Function atom : body) {
			Function clone = (Function)atom.clone();
			if (clone.getFunctionSymbol() instanceof BuiltinPredicate) {
				filterAtoms.add(clone);
			}
			else {
				// database atom, we need to replace all constants by filters
				for (int i = 0; i < clone.getTerms().size(); i++) {
					Term term = clone.getTerm(i);
					if (term instanceof Constant) {
						// Found a constant, replacing with a fresh variable
						// and adding the new equality atom.
						freshVarCount++;
						Variable freshVariable = fac.getVariable("?FreshVar" + freshVarCount);
						filterAtoms.add(fac.getFunctionEQ(freshVariable, term));
						clone.setTerm(i, freshVariable);
					}
				}
				databaseAtoms.add(clone);			
			}
		}
		// TODO: would it not be a good idea to eliminate constants from the head as well?
		this.head = (Function)head.clone();
		this.stripped = fac.getCQIE(head, databaseAtoms);
		this.cqc = cqc;
	}

	public TMappingRule(TMappingRule baseRule, List<Function> conditionsOR) {
		this.databaseAtoms = cloneList(baseRule.databaseAtoms);
		this.head = (Function)baseRule.head.clone();

		List<Function> conditions1 = cloneList(baseRule.filterAtoms);
		List<Function> conditions2 = cloneList(conditionsOR);
		
		Function orAtom = fac.getFunctionOR(getMergedConditions(conditions1), 
											getMergedConditions(conditions2));
		filterAtoms = new ArrayList<>(1);
		filterAtoms.add(orAtom);

		this.stripped = fac.getCQIE(head, databaseAtoms);
		this.cqc = baseRule.cqc;
	}
	
	
	public TMappingRule(Function head, TMappingRule baseRule) {
		this.filterAtoms = cloneList(baseRule.filterAtoms);
		this.databaseAtoms = cloneList(baseRule.databaseAtoms);
		this.head = (Function)head.clone();
		
		this.stripped = fac.getCQIE(head, databaseAtoms);
		this.cqc = baseRule.cqc;
	}
	
	
	private static List<Function> cloneList(List<Function> list) {
		List<Function> newlist = new ArrayList<>(list.size());
		for (Function atom : list) {
			Function clone = (Function)atom.clone();
			newlist.add(clone);	
		}
		return newlist;
	}
	
	
	public boolean isConditionsEmpty() {
		return filterAtoms.isEmpty();
	}
	
	public Substitution computeHomomorphsim(TMappingRule other) {
		return cqc.computeHomomorphsim(stripped, other.stripped);
	}
	
	public CQIE asCQIE() {
		List<Function> combinedBody;
		if (!filterAtoms.isEmpty()) {
			combinedBody = new ArrayList<>(databaseAtoms.size() + filterAtoms.size()); 
			combinedBody.addAll(databaseAtoms);
			combinedBody.addAll(filterAtoms);
		}
		else
			combinedBody = databaseAtoms;
		
		CQIE cq = fac.getCQIE(head, combinedBody);
		EQNormalizer.enforceEqualities(cq);
		return cq;
	}
	
	public boolean isFact() {
		return databaseAtoms.isEmpty() && filterAtoms.isEmpty();
	}
	
	public List<Term> getHeadTerms() {
		return head.getTerms();
	}
	
	public List<Function> getConditions() {
		return filterAtoms;
	}
	
	/***
	 * Takes a conjunctive boolean atoms and returns one single atom
	 * representing the conjunction 
	 * 
	 * ASSUMPTION: conditions is NOT empty
	 * 
	 * Example: A -> A
	 *          A, B -> AND(B,A)
	 *          A, B, C -> AND(C,AND(B,A))
	 * 
	 * @return
	 */
	private static Function getMergedConditions(List<Function> conditions) {
		Iterator<Function> iter = conditions.iterator();
		Function mergedConditions = iter.next(); // IMPORTANT: assume that conditions is non-empty
		while (iter.hasNext()) {
			Function e = iter.next();
			mergedConditions = fac.getFunctionAND(e, mergedConditions);
		}
		return mergedConditions;
	}
	
	@Override
	public int hashCode() {
		return head.hashCode() ^ databaseAtoms.hashCode() ^ filterAtoms.hashCode();
	}
	
	@Override
	public boolean equals(Object other) {
		if (other instanceof TMappingRule) {
			TMappingRule otherRule = (TMappingRule)other;
			return (head.equals(otherRule.head) && 
					databaseAtoms.equals(otherRule.databaseAtoms) && 
					filterAtoms.equals(otherRule.filterAtoms));
		}
		return false;
	}

	@Override 
	public String toString() {
		return head + " <- " + databaseAtoms + " AND " + filterAtoms;
	}
}
