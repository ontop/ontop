package inf.unibz.it.ucq.domain;

import com.sun.msv.datatype.xsd.XSDatatype;

public class TypedConstantTerm extends QueryTerm {

	private XSDatatype datatype = null;
	
	public TypedConstantTerm(String name, XSDatatype type) {
		super(name);
		datatype = type;
		// TODO Auto-generated constructor stub
	}

	@Override
	public String toString() {
		return this.getName();
	}

	@Override
	public TypedConstantTerm clone() {
		return new TypedConstantTerm(new String(this.getName()), datatype);
	}
	
	public XSDatatype getDatatype(){
		return datatype;
	}
}
