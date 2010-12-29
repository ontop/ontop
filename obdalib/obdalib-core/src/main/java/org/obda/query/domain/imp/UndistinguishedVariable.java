package org.obda.query.domain.imp;

import org.obda.query.domain.Variable;

import com.sun.msv.datatype.xsd.XSDatatype;

public class UndistinguishedVariable implements Variable{

	private String name= "#";
	private int identifier = -4000;
	private XSDatatype type = null;
	
	public UndistinguishedVariable(){

	}

	public boolean equals(Object obj){
		 if(obj == null || !(obj instanceof Variable)){ 
			 return false;
		 }
		 
		 return this.hash() == ((VariableImpl)obj).hashCode();
	 }

	 public long hash(){
		 return identifier;
	 }

	
	public String getName() {
		return name;
	}

	public Variable copy() {
		return new UndistinguishedVariable();
	}

	public void setName(String n){
		name = n;
	}
	
	public String toString() {
		return getName();
	}
}
