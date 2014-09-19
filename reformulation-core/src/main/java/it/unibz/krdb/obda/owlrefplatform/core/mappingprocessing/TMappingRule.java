package it.unibz.krdb.obda.owlrefplatform.core.mappingprocessing;

import it.unibz.krdb.obda.model.BuiltinPredicate;
import it.unibz.krdb.obda.model.CQIE;
import it.unibz.krdb.obda.model.Function;
import it.unibz.krdb.obda.model.OBDADataFactory;
import it.unibz.krdb.obda.model.Term;
import it.unibz.krdb.obda.model.impl.OBDADataFactoryImpl;
import it.unibz.krdb.obda.ontology.Ontology;
import it.unibz.krdb.obda.owlrefplatform.core.basicoperations.CQCUtilities;

import java.util.LinkedList;
import java.util.List;

/***
 * Splits a given {@link mapping} into builtin predicates ({@link conditions})
 * and all other atoms ({@link stripped}), which are checked for containment 
 * by the main algorithm.
 */

public class TMappingRule {
	
	private static final OBDADataFactory fac = OBDADataFactoryImpl.getInstance();
	
	private final CQIE stripped;	
	private final List<Function> conditions;	
	private final CQCUtilities cqc;

	public TMappingRule(Function head, List<Function> body) {
		conditions = new LinkedList<Function>();
		List<Function> newbody = new LinkedList<Function>();
		for (Function atom : body) {
			Function clone = (Function)atom.clone();
			if (clone.getPredicate() instanceof BuiltinPredicate) 
				conditions.add(clone);
			else 
				newbody.add(clone);			
		}
		Function newhead = (Function)head.clone();
		stripped = fac.getCQIE(newhead, newbody);
		cqc = new CQCUtilities(stripped, (Ontology)null /*sigma*/);
	}

	public TMappingRule(Function head, TMappingRule baseRule) {
		conditions = new LinkedList<Function>();
		for (Function atom : baseRule.conditions) {
			Function clone = (Function)atom.clone();
			conditions.add(clone);	
		}
		
		List<Function> newbody = new LinkedList<Function>();
		for (Function atom : baseRule.stripped.getBody()) {
			Function clone = (Function)atom.clone();
			newbody.add(clone);	
		}
		Function newhead = (Function)head.clone();
		stripped = fac.getCQIE(newhead, newbody);
		cqc = new CQCUtilities(stripped, (Ontology)null /*sigma*/);
	}
	
	
	public boolean isConditionsEmpty() {
		return conditions.isEmpty();
	}
	
	public boolean isContainedIn(TMappingRule other) {
		return cqc.isContainedIn(other.stripped);
	}
	
	public CQIE asCQIE() {
		List<Function> combinedBody = new LinkedList<Function>(); 
		combinedBody.addAll(stripped.getBody());
		combinedBody.addAll(conditions);
		return fac.getCQIE(stripped.getHead(), combinedBody);				
	}
	
	public boolean isFact() {
		return stripped.getBody().isEmpty() && conditions.isEmpty();
	}
	
	public List<Term> getHeadTerms() {
		return stripped.getHead().getTerms();
	}
	
	// Roman: avoid using it because it gives access to the internal presentation
	@Deprecated
	public CQIE getStripped() {
		return stripped;
	}
	
	/***
	 * Takes a conjunctive boolean atoms and returns one single atom
	 * representing the conjunction (it might be a single atom if
	 * conditions.size() == 1.
	 * 
	 * @return
	 */
	public Function getMergedConditions() {
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

}
