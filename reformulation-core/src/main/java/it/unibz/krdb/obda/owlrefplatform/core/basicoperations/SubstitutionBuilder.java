package it.unibz.krdb.obda.owlrefplatform.core.basicoperations;

import it.unibz.krdb.obda.model.Term;
import it.unibz.krdb.obda.model.impl.VariableImpl;

import java.util.HashMap;
import java.util.Map;

public class SubstitutionBuilder implements Cloneable {

	private Map<VariableImpl, Term> map = new HashMap<>();
	
	public boolean extend(VariableImpl var, Term term) {
		Term t = map.get(var);
		// add if there is no value yet
		if (t == null) {
			map.put(var, term);
			return true;
		}
		// ignore if the substitution already has the same value
		if (term.equals(t))
			return true;
		// otherwise
		return false;
	}
	
	
	
	public Substitution getSubstituition() {
		return new SubstitutionImpl(map);
	}
	
	@SuppressWarnings("unchecked")
	@Override
	public SubstitutionBuilder clone() {
		SubstitutionBuilder sb;
		try {
			sb = (SubstitutionBuilder) super.clone();
			sb.map = (Map<VariableImpl, Term>) ((HashMap<VariableImpl, Term>)map).clone();
			return sb;
		} 
		catch (CloneNotSupportedException e) {
            e.printStackTrace();
			throw new RuntimeException();
		}
	}	
	
	@Override
	public String toString() {
		return map.toString();
	}
}
