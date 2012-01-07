package it.unibz.krdb.obda.model.impl;

import it.unibz.krdb.obda.model.Variable;

import com.sun.msv.datatype.xsd.XSDatatype;

public class VariableImpl implements Variable {

	/**
	 * 
	 */
	private static final long serialVersionUID = 5723075311798541659L;
	
	private final String name;
	
	private final XSDatatype type;
	
	private final int identifier;

	protected VariableImpl(String name, XSDatatype type) {
		this.name = name;
		this.type = type;
		this.identifier = name.hashCode();
	}

	@Override
	public boolean equals(Object obj) {

		if (obj == null || !(obj instanceof VariableImpl))
			return false;

		VariableImpl name2 = (VariableImpl) obj;
		return this.identifier == name2.identifier;
	}

	@Override
	public int hashCode() {
		return identifier;
	}

	// @Override
	// public void setName(String name) {
	// this.name = name;
	// }

	@Override
	public String getName() {
		return name;
	}

	// TODO this method seems to be tied to some semantics, if we modified it,
	// things become slow and maybe wrong we must make sure that this is not the
	// case
	@Override
	public String toString() {
		return name;
	}

	@Override
	public Variable clone() {
		 return this;
		// VariableImpl clone = new VariableImpl(new String(name), this.type);
		// clone.identifier = identifier;
		// return clone;
	}
}
