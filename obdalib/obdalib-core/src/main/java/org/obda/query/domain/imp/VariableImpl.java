package org.obda.query.domain.imp;

import org.obda.query.domain.Variable;

import com.sun.msv.datatype.xsd.XSDatatype;

public class VariableImpl implements Variable{

	private String name= null;
	private int identifier = Integer.MIN_VALUE;
	private XSDatatype type = null;

	
	protected VariableImpl(String name, int identifier, XSDatatype type){
		this.name = name;
		this.identifier = identifier;
		this.type = type;
	}

	 public boolean equals(Object obj){
		 if(obj == null || !(obj instanceof Variable)){
			 return false;
		 }
		 
		 return this.hash() == ((VariableImpl)obj).hash();
	 }

	 public long hash(){
		 return identifier;
	 }

	
	public String getName() {
		return name;
	}

	public Variable copy() {
		return new VariableImpl(new String(name), identifier, type);
	}

}
