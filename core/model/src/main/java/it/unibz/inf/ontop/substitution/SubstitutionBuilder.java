package it.unibz.inf.ontop.substitution;

import it.unibz.inf.ontop.model.term.Term;
import it.unibz.inf.ontop.model.term.TermFactory;
import it.unibz.inf.ontop.model.term.Variable;
import it.unibz.inf.ontop.substitution.impl.SubstitutionImpl;

import java.util.HashMap;
import java.util.Map;

public class SubstitutionBuilder implements Cloneable {

	private Map<Variable, Term> map;
	private final TermFactory termFactory;

	public SubstitutionBuilder(TermFactory termFactory) {
		this.termFactory = termFactory;
		this.map = new HashMap<>();
	}

	public boolean extend(Variable var, Term term) {
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
		return new SubstitutionImpl(map, termFactory);
	}
	
	@SuppressWarnings("unchecked")
	@Override
	public SubstitutionBuilder clone() {
		SubstitutionBuilder sb;
		try {
			sb = (SubstitutionBuilder) super.clone();
			sb.map = (Map<Variable, Term>) ((HashMap<Variable, Term>)map).clone();
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
