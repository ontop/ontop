package it.unibz.krdb.obda.model.impl;

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

		AnonymousVariable var2 = (AnonymousVariable) obj;
		return identifier == var2.hashCode();
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
		return new AnonymousVariable();
	}

	@Override
	public String toString() {
		return getName();
	}
}
