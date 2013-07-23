package it.unibz.krdb.obda.model.impl;

import it.unibz.krdb.obda.model.Atom;
import it.unibz.krdb.obda.model.Variable;

import java.util.HashMap;
import java.util.LinkedHashSet;
import java.util.Map;
import java.util.Set;

public class AnonymousVariable extends AbstractLiteral implements Variable {

	private static final long serialVersionUID = 6099056787768897902L;

	private static final String DEFAULT_NAME = "_";
	
	private static final int identifier = -4000;

	protected AnonymousVariable() {
		// NO-OP
	}

	@Override
	public boolean equals(Object obj) {
		if (obj == null || !(obj instanceof AnonymousVariable)) {
			return false;
		}
		return true;
	}

	@Override
	public int hashCode() {
		return identifier;
	}

	@Override
	public String getName() {
		return DEFAULT_NAME;
	}

	@Override
	public Variable clone() {
		return this;
	}

	@Override
	public String toString() {
		return TermUtil.toString(this);
	}

	@Override
	public Set<Variable> getReferencedVariables() {
		return new LinkedHashSet<Variable>();
	}
	
	@Override
	public Map<Variable, Integer> getVariableCount() {
		// TODO This is wrong but it shouldn't affect query containment
		Map<Variable,Integer> count =  new HashMap<Variable,Integer>();
		count.put(this, 1);
		return count;
	}

	@Override
	public Atom asAtom() {
		throw new RuntimeException("Impossible to cast as atom: " + this.getClass()); 
	}
}
