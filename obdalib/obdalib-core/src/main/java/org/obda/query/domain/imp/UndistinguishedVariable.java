package org.obda.query.domain.imp;

import org.obda.query.domain.Variable;

import com.sun.msv.datatype.xsd.XSDatatype;

public class UndistinguishedVariable implements Variable {

	private String name= "#";
	private final int identifier = -4000;
	private final XSDatatype type = null;

	protected UndistinguishedVariable() {

	}

	@Override
	public boolean equals(Object obj){
		 if (obj == null || !(obj instanceof Variable)) {
			 return false;
		 }

		 VariableImpl var2 = (VariableImpl) obj;
		 return this.identifier == var2.hashCode();
	 }

	@Override
	public int hashCode(){
		return identifier;
	}

	@Override
	public String getName() {
		return name;
	}

	@Override
	public Variable copy() {
		return new UndistinguishedVariable();
	}

	public void setName(String n){
		name = n;
	}

	@Override
	public String toString() {
		return getName();
	}
}
