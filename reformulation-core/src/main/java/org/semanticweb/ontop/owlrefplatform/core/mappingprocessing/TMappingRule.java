package org.semanticweb.ontop.owlrefplatform.core.mappingprocessing;

import org.semanticweb.ontop.model.*;
import org.semanticweb.ontop.model.impl.OBDADataFactoryImpl;
import org.semanticweb.ontop.owlrefplatform.core.basicoperations.*;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

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
	// an OR-connected list of AND-connected atomic filters
	private final List<List<Function>> filterAtoms;	  
	private final CQContainmentCheck cqc;

	
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
	
	public TMappingRule(Function head, List<Function> body, CQContainmentCheck cqc) {
		this.databaseAtoms = new ArrayList<>(body.size()); // we estimate the size
		
		List<Function> filters = new ArrayList<>(body.size());
		
		for (Function atom : body) {
			if (atom.getFunctionSymbol() instanceof BuiltinPredicate) {
				Function clone = (Function)atom.clone();
				filters.add(clone);
			}
			else {
				// database atom, we need to replace all constants by filters
				databaseAtoms.add(replaceConstants(atom, filters));			
			}
		}
		if (filters.isEmpty()) 
			this.filterAtoms = Collections.emptyList();
		else 
			this.filterAtoms = Collections.singletonList(filters);
		
		this.head = replaceConstants(head, filters);
		this.stripped = fac.getCQIE(this.head, databaseAtoms);
		this.cqc = cqc;
	}

	
	private int freshVarCount = 0;
	private final Map<Constant, Variable> valueMap = new HashMap<>();
	
	private Function replaceConstants(Function a, List<Function> filters) {
		Function atom = (Function)a.clone();
		
		for (int i = 0; i < atom.getTerms().size(); i++) {
			Term term = atom.getTerm(i);
			if (term instanceof Constant) {
				// Found a constant, replacing with a fresh variable
				// and adding the new equality atom
				Constant c = (Constant)term;
				Variable var = valueMap.get(c);
				if (var == null) {
					freshVarCount++;
					var = fac.getVariable("?FreshVar" + freshVarCount);
					valueMap.put(c, var);
					filters.add(fac.getFunctionEQ(var, c));
				}
				atom.setTerm(i, var);
			}
		}
		
		return atom;
	}
	
	public TMappingRule(TMappingRule baseRule, List<Function> conditionsOR) {
		this.databaseAtoms = cloneList(baseRule.databaseAtoms);
		this.head = (Function)baseRule.head.clone();

		this.filterAtoms = new ArrayList<>(baseRule.filterAtoms.size() + 1);
		for (List<Function> baseList: baseRule.filterAtoms)
			filterAtoms.add(cloneList(baseList));		
		filterAtoms.add(conditionsOR);

		this.stripped = fac.getCQIE(head, databaseAtoms);
		this.cqc = baseRule.cqc;
	}
	
	
	public TMappingRule(Function head, TMappingRule baseRule) {
		this.filterAtoms = new ArrayList<>(baseRule.filterAtoms.size());
		for (List<Function> baseList: baseRule.filterAtoms)
			filterAtoms.add(cloneList(baseList));
		
		this.databaseAtoms = cloneList(baseRule.databaseAtoms);
		this.head = (Function)head.clone();
		
		this.stripped = fac.getCQIE(head, databaseAtoms);
		this.cqc = baseRule.cqc;
	}
	
	
	public static List<Function> cloneList(List<Function> list) {
		List<Function> newlist = new ArrayList<>(list.size());
		for (Function atom : list) {
			Function clone = (Function)atom.clone();
			newlist.add(clone);	
		}
		return newlist;
	}
	
	@Deprecated // TEST ONLY
	Function getHead() {
		return head;
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
			
			Iterator<List<Function>> iterOR = filterAtoms.iterator();
			List<Function> list = iterOR.next(); // IMPORTANT: assume that conditions is non-empty
			Function mergedConditions = getMergedByAND(list);
			while (iterOR.hasNext()) {
				list = iterOR.next();
				Function e = getMergedByAND(list);
				mergedConditions = fac.getFunctionOR(e, mergedConditions);				
			}
			
			combinedBody.add(mergedConditions);
		}
		else
			combinedBody = databaseAtoms;
		
		CQIE cq = fac.getCQIE(head, combinedBody);
		EQNormalizer.enforceEqualities(cq);
		return cq;
	}
	
	/***
	 * Takes a list of boolean atoms and returns one single atom
	 * representing the conjunction 
	 * 
	 * ASSUMPTION: the list is non-empty
	 * 
	 * Example: A -> A
	 *          A, B -> AND(B,A)
	 *          A, B, C -> AND(C,AND(B,A))
	 * 
	 */
	
	private static Function getMergedByAND(List<Function> list) {
		Iterator<Function> iterAND = list.iterator();
		Function mergedConditions = iterAND.next();
		while (iterAND.hasNext()) {
			Function e = iterAND.next();
			mergedConditions = fac.getFunctionAND(e, mergedConditions);				
		}		
		return mergedConditions;
	}
	
	
	public boolean isFact() {
		return databaseAtoms.isEmpty() && filterAtoms.isEmpty();
	}
	
	public List<Term> getHeadTerms() {
		return head.getTerms();
	}
	
	public int databaseAtomsSize() {
		return databaseAtoms.size();
	}
	
	public List<List<Function>> getConditions() {
		return filterAtoms;
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
