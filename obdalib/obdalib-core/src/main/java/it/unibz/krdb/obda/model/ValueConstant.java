package it.unibz.krdb.obda.model;

/**
 * Provides an interface for storing the value constant.
 */
public interface ValueConstant extends Constant {

	public String getValue();
	
	public String getLanguage();
	
	/**
	 * Get the data type of the constant.
	 *
	 * @return the data type.
	 */
	public Predicate.COL_TYPE getType();
}
