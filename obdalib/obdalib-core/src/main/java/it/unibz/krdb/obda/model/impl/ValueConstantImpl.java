package it.unibz.krdb.obda.model.impl;

import it.unibz.krdb.obda.model.ValueConstant;

import com.sun.msv.datatype.xsd.XSDatatype;

public class ValueConstantImpl implements ValueConstant {

	/**
	 * 
	 */
	private static final long serialVersionUID = 8031338451909170400L;

	private final String value;

	private final XSDatatype type;

	private final int identifier;

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
		// ValueConstantImpl clone = new ValueConstantImpl(value, this.type);
		// clone.identifier = identifier;
		// return clone;
		return this;
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
