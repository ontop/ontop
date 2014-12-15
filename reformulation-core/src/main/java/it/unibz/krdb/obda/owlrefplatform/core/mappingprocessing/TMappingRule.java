package it.unibz.krdb.obda.owlrefplatform.core.mappingprocessing;

import it.unibz.krdb.obda.model.BuiltinPredicate;
import it.unibz.krdb.obda.model.CQIE;
import it.unibz.krdb.obda.model.Function;
import it.unibz.krdb.obda.model.OBDADataFactory;
import it.unibz.krdb.obda.model.Term;
import it.unibz.krdb.obda.model.impl.OBDADataFactoryImpl;
import it.unibz.krdb.obda.owlrefplatform.core.basicoperations.CQContainmentCheckUnderLIDs;
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
	
	private final CQIE stripped;	
	private final List<Function> conditions;	
	private final CQContainmentCheckUnderLIDs cqc;   
	
	public TMappingRule(Function head, List<Function> body, CQContainmentCheckUnderLIDs cqc) {
		conditions = new LinkedList<>();
		List<Function> newbody = new LinkedList<>();
		for (Function atom : body) {
			Function clone = (Function)atom.clone();
			if (clone.getFunctionSymbol() instanceof BuiltinPredicate) 
				conditions.add(clone);
			else 
				newbody.add(clone);			
		}
		Function newhead = (Function)head.clone();
		stripped = fac.getCQIE(newhead, newbody);
		this.cqc = cqc;
	}

	public TMappingRule(TMappingRule baseRule, List<Function> conditionsOR) {
		List<Function> newbody = cloneList(baseRule.stripped.getBody());
		Function newhead = (Function)baseRule.stripped.getHead().clone();
		stripped = fac.getCQIE(newhead, newbody);

		List<Function> conditions1 = cloneList(baseRule.conditions);
		List<Function> conditions2 = cloneList(conditionsOR);
		
		Function orAtom = fac.getFunctionOR(getMergedConditions(conditions1), 
											getMergedConditions(conditions2));
		conditions = new LinkedList<>();
		conditions.add(orAtom);

		cqc = baseRule.cqc;
	}
	
	
	public TMappingRule(Function head, TMappingRule baseRule) {
		conditions = cloneList(baseRule.conditions);

		List<Function> newbody = cloneList(baseRule.stripped.getBody());
		Function newhead = (Function)head.clone();
		stripped = fac.getCQIE(newhead, newbody);
		
		cqc = baseRule.cqc;
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
		return conditions.isEmpty();
	}
	
	public Substitution computeHomomorphsim(TMappingRule other) {
		return cqc.computeHomomorphsim(stripped, other.stripped);
	}
	
	public CQIE asCQIE() {
		List<Function> combinedBody;
		if (!conditions.isEmpty()) {
			combinedBody = new LinkedList<Function>(); 
			combinedBody.addAll(stripped.getBody());
			combinedBody.addAll(conditions);
		}
		else
			combinedBody = stripped.getBody();
		return fac.getCQIE(stripped.getHead(), combinedBody);				
	}
	
	public boolean isFact() {
		return stripped.getBody().isEmpty() && conditions.isEmpty();
	}
	
	public List<Term> getHeadTerms() {
		return stripped.getHead().getTerms();
	}
	
	public List<Function> getConditions() {
		return conditions;
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
		return stripped.hashCode() ^ conditions.hashCode();
	}
	
	@Override
	public boolean equals(Object other) {
		if (other instanceof TMappingRule) {
			TMappingRule otherRule = (TMappingRule)other;
			return (stripped.equals(otherRule.stripped) && conditions.equals(otherRule.conditions));
		}
		return false;
	}

	@Override 
	public String toString() {
		return stripped + " AND " + conditions;
	}
}
