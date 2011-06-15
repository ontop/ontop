package it.unibz.krdb.obda.model.impl;

import it.unibz.krdb.obda.model.ValueConstant;

import com.sun.msv.datatype.xsd.XSDatatype;

public class ValueConstantImpl implements ValueConstant {

	private String		value		= null;
	private XSDatatype	type		= null;
	private int			identifier	= -1;

	/**
	 * The default constructor.
	 * 
	 * @param value
	 *            the constant value.
	 * @param type
	 *            the constant type.
	 */
	protected ValueConstantImpl(String value, XSDatatype type) {
		this.value = value;
		this.type = type;
		this.identifier = value.hashCode();
	}

	@Override
	public boolean equals(Object obj) {

		if (obj == null || !(obj instanceof ValueConstantImpl))
			return false;

		ValueConstantImpl value2 = (ValueConstantImpl) obj;
		return this.identifier == value2.identifier;
	}

	@Override
	public int hashCode() {
		return identifier;
	}

	@Override
	public ValueConstant clone() {
		return new ValueConstantImpl(new String(value), this.type);
	}

	@Override
	public String getValue() {
		return value;
	}

	@Override
	public XSDatatype getType() {
		return type;
	}

	@Override
	public String toString() {
		// TODO add the data type support
		return "\"" + getValue().toString() + "\"";
	}
}
