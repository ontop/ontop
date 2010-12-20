package org.obda.query.domain.imp;

import org.obda.query.domain.FunctionSymbol;

public class FunctionSymbolImpl implements FunctionSymbol{

	private String name = null;
	private int identifier = Integer.MIN_VALUE;
	public FunctionSymbolImpl(String name, int identifier){
		this.name = name;
		this.identifier = identifier;
	}
	
	public boolean equals(Object obj){
		if(obj == null || !(obj instanceof FunctionSymbolImpl)){
			return false;
		}
		
		return this.hash() == obj.hashCode();
	}

	public long hash(){
	  return identifier;
	}

	public FunctionSymbol copy() {
		return new FunctionSymbolImpl(new String(name), identifier);
	}

	public String getName() {
		return name;
	}
}
