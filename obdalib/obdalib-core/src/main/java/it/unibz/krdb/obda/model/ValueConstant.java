package it.unibz.krdb.obda.model;

import com.sun.msv.datatype.xsd.XSDatatype;

/**
 * Provides an interface for storing the value constant.
 */
public interface ValueConstant extends Constant {

	public String getValue();
	/**
	 * Get the data type of the constant.
	 *
	 * @return the data type.
	 */
	public XSDatatype getType();
}
