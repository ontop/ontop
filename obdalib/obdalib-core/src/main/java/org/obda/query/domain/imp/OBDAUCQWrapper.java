package org.obda.query.domain.imp;

import inf.unibz.it.ucq.domain.ConjunctiveQuery;
import inf.unibz.it.ucq.domain.UnionOfConjunctiveQueries;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.Vector;

import org.obda.query.domain.Atom;
import org.obda.query.domain.CQIE;
import org.obda.query.domain.DatalogProgram;

public class OBDAUCQWrapper implements DatalogProgram {

	private List<CQIE> rules = null;
	private boolean isBoolean = false;
	
	public OBDAUCQWrapper(UnionOfConjunctiveQueries ucq, boolean isBoolean){
		
		rules = new Vector<CQIE>();
		this.isBoolean = isBoolean;
		ArrayList<ConjunctiveQuery> queries = ucq.getQueries();
		Iterator<ConjunctiveQuery> it = queries.iterator();
		while(it.hasNext()){
			
			CQIE rule = new OBDAConjunctiveQueryWrapper(it.next(),isBoolean);
			rules.add(rule);
		}
	}

	public List<CQIE> getRules() {
		return rules;
	}

	public void appendRule(CQIE rule) {
		rules.add(rule);		
	}

	public void appendRule(List<CQIE> rule) {
		rules.addAll(rule);		
	}

	public boolean isUCQ() {
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
	}

	public void removeRule(CQIE rule) {
		rules.remove(rule);		
	}

	public void removeRules(List<CQIE> rule) {
		rules.removeAll(rule);			
	}


	public boolean isBoolean() {
		return isBoolean;
	}

}
