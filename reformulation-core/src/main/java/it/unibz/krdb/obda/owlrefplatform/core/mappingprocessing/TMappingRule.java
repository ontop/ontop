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
	final CQContainmentCheckUnderLIDs cqc;   // TODO: make private
	
	public TMappingRule(Function head, List<Function> body, CQContainmentCheckUnderLIDs cqc) {
		conditions = new LinkedList<Function>();
		List<Function> newbody = new LinkedList<Function>();
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

	public TMappingRule(Function head, TMappingRule baseRule, CQContainmentCheckUnderLIDs cqc) {
		conditions = new ArrayList<Function>(baseRule.conditions.size());
		for (Function atom : baseRule.conditions) {
			Function clone = (Function)atom.clone();
			conditions.add(clone);	
		}
				
		List<Function> newbody = new ArrayList<Function>(baseRule.stripped.getBody().size());
		for (Function atom : baseRule.stripped.getBody()) {
			Function clone = (Function)atom.clone();
			newbody.add(clone);	
		}
		Function newhead = (Function)head.clone();
		stripped = fac.getCQIE(newhead, newbody);
		this.cqc = cqc;
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
	
	// ROMAN: avoid using it because it gives access to the internal presentation
	@Deprecated
	public CQIE getStripped() {
		return stripped;
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
	public Function getMergedConditions() {
		// one might prefer to cache merged conditions
		Iterator<Function> iter = conditions.iterator();
		Function mergedConditions = iter.next(); // IMPORTANT: assume that conditions is non-empty
		while (iter.hasNext()) {
			Function e = iter.next();
			mergedConditions = fac.getFunctionAND(e, mergedConditions);
		}
		return mergedConditions;
/*		
		if (conditions.size() == 1)
			return conditions.get(0);
		Function atom0 = conditions.remove(0);
		Function atom1 = conditions.remove(0);
		Term f0 = fac.getFunction(atom0.getPredicate(), atom0.getTerms());
		Term f1 = fac.getFunction(atom1.getPredicate(), atom1.getTerms());
		Function nestedAnd = fac.getFunctionAND(f0, f1);
		while (conditions.size() != 0) {
			Function condition = conditions.remove(0);
			Term term0 = nestedAnd.getTerm(1);
			Term term1 = fac.getFunction(condition.getPredicate(), condition.getTerms());
			Term newAND = fac.getFunctionAND(term0, term1);
			nestedAnd.setTerm(1, newAND);
		}
		return nestedAnd;
*/
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
