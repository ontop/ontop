package org.obda.query.domain.imp;

import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Vector;

import org.obda.query.domain.Atom;
import org.obda.query.domain.CQIE;
import org.obda.query.domain.DatalogProgram;
import org.obda.query.domain.Predicate;

public class DatalogProgramImpl implements DatalogProgram{

	private List<CQIE> rules = null;
	
	private Map<Predicate, List<CQIE>> predicateIndex = null;
	
	public DatalogProgramImpl(){
		rules = new Vector<CQIE>();
		predicateIndex = new HashMap<Predicate, List<CQIE>>();
	}
	
	public void appendRule(CQIE rule){
		rules.add(rule);
		
		Predicate predicate = rule.getHead().getPredicate();
		List<CQIE> indexedRules = this.getRules(predicate);
		indexedRules.add(rule);
	}
	
	public void appendRule(List<CQIE> rules){
		for (CQIE rule: rules) {
			appendRule(rule);
		}
		 
	}
	
	public void removeRule(CQIE rule){
		rules.remove(rule);
		
		Predicate predicate = rule.getHead().getPredicate();
		List<CQIE> indexedRules = this.getRules(predicate);
		indexedRules.remove(rule);
	}
	
	public void removeRules(List<CQIE> rules){
		for (CQIE rule: rules) {
			removeRule(rule);
		}
	}
	public boolean isUCQ(){
		
		if(rules.size() >1){
			boolean isucq = true;
			CQIE rule0 = rules.get(0);
			Atom head0 = rule0.getHead();
			for(int i=1;i<rules.size() && isucq;i++){
				
				CQIE ruleI = rules.get(i);
				Atom headI = ruleI.getHead();
				if(head0.getArity() != headI.getArity() || !(head0.getPredicate().equals(headI.getPredicate()))){
					isucq = false;
				}
			}
			return isucq;
		}else if(rules.size() ==1){
			return true; 
		}else{
			return false;
		}
//	  returns true if the head of all the rules has the same predicate and 
//	  same arity
	}
	
	public List<CQIE> getRules() {
		return rules;
	}
	
	public String toString() {
		StringBuffer bf = new StringBuffer();
		for (CQIE rule: this.rules) {
			bf.append(rule.toString());
			bf.append("\n");
		}
		return bf.toString();
	}

	@Override
	public List<CQIE> getRules(Predicate headPredicate) {
		List<CQIE> rules = this.predicateIndex.get(headPredicate);
		if (rules == null) 
		{
			rules = new LinkedList<CQIE>();
			predicateIndex.put(headPredicate, rules);
		}
		return rules;
	}

}
