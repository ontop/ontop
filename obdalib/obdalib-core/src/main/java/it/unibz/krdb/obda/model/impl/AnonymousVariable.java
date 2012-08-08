package it.unibz.krdb.obda.model.impl;

import java.util.HashMap;
import java.util.LinkedHashSet;
import java.util.Map;
import java.util.Set;

import it.unibz.krdb.obda.model.Variable;

public class AnonymousVariable implements Variable {

	/**
	 * 
	 */
	private static final long serialVersionUID = 6099056787768897902L;
	private static final String name = "_";
	private static final int identifier = -4000;

	// private static final XSDatatype type = null;

	protected AnonymousVariable() {

	}

	@Override
	public boolean equals(Object obj) {
		if (obj == null || !(obj instanceof AnonymousVariable)) {
			return false;
		}

//		AnonymousVariable var2 = (AnonymousVariable) obj;
//		return identifier == var2.hashCode();
		return true;
	}

	@Override
	public int hashCode() {
		return identifier;
	}

	// @Override
	// public void setName(String name){
	// this.name = name;
	// }

	@Override
	public String getName() {
		return name;
	}

	@Override
	public Variable clone() {
		return this;
//		return new AnonymousVariable();
	}

	@Override
	public String toString() {
		return getName();
	}

	@Override
	public Set<Variable> getReferencedVariables() {
		return new LinkedHashSet<Variable>();
	}
	
	@Override
	public Map<Variable, Integer> getVariableCount() {
		/* This is wrong but it shouldn't affect query containment */
		Map<Variable,Integer> count =  new HashMap<Variable,Integer>();
		count.put(this, 1);
		return count;
	}
}
