package inf.unibz.it.obda.model.impl;

import inf.unibz.it.obda.model.Variable;


import com.sun.msv.datatype.xsd.XSDatatype;



public class VariableImpl implements Variable {

	private String name = null;
	private XSDatatype type = null;
	private int identifier = -1;

	protected VariableImpl(String name, XSDatatype type) {
		this.name = name;
		this.type = type;
		this.identifier = name.hashCode();
	}

	@Override
	public boolean equals(Object obj) {

		if(obj == null || !(obj instanceof VariableImpl))
			 return false;

		VariableImpl name2 = (VariableImpl) obj;
		return this.identifier == name2.identifier;
	 }

	@Override
	public int hashCode() {
		 return identifier;
	}

	@Override
	public String getName() {
		return name;
	}

	@Override
	public String toString() {
		return name;
	}

	@Override
	public Variable copy() {
		return new VariableImpl(new String(name), this.type);
	}
}
