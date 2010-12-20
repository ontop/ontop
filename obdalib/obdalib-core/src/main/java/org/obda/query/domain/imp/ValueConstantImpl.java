package org.obda.query.domain.imp;

import org.obda.query.domain.ValueConstant;

import com.sun.msv.datatype.xsd.XSDatatype;

public class ValueConstantImpl implements ValueConstant{

	private String value = null;
	private int identifier = Integer.MIN_VALUE;
	private XSDatatype type = null;

	protected ValueConstantImpl(String value, int identifier, XSDatatype type){
		this.value = value;
		this.identifier = identifier;
		this.type = type;
	}

	 @Override
	public boolean equals(Object obj){

		 if(obj == null || !(obj instanceof ValueConstantImpl)){
			 return false;
		 }

		 return this.hash() == ((ValueConstantImpl)obj).hash();
	 }


	 public long hash(){
		return identifier;
	 }

	public ValueConstant copy() {
		return new ValueConstantImpl(new String(value),identifier,type);
	}

	public String getName() {
		return value;
	}

	public XSDatatype getType() {
		return type;
	}
}
