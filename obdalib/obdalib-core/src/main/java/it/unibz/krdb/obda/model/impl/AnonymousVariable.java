package it.unibz.krdb.obda.model.impl;

import it.unibz.krdb.obda.model.Variable;

import com.sun.msv.datatype.xsd.XSDatatype;

public class AnonymousVariable implements Variable {

	private String name= "_";
	private final int identifier = -4000;
	private final XSDatatype type = null;

	protected AnonymousVariable() {

	}

	@Override
	public boolean equals(Object obj){
		 if (obj == null || !(obj instanceof AnonymousVariable)) {
			 return false;
		 }

		 AnonymousVariable var2 = (AnonymousVariable) obj;
		 return this.identifier == var2.hashCode();
	 }

	@Override
	public int hashCode(){
		return identifier;
	}

//	@Override
//	public void setName(String name){
//		this.name = name;
//	}
	
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
