package inf.unibz.it.obda.model;

import inf.unibz.it.obda.model.Constant;

import com.sun.msv.datatype.xsd.XSDatatype;

/**
 * Provides an interface for storing the value constant.
 */
public interface ValueConstant extends Constant {

	/**
	 * Get the data type of the constant.
	 *
	 * @return the data type.
	 */
	public XSDatatype getType();
}
