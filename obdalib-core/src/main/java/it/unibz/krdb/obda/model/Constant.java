package it.unibz.krdb.obda.model;

/**
 * This class defines a type of {@link Term} in which it has a constant
 * value.
 */
public interface Constant extends Term {

	public Predicate.COL_TYPE getType();

	/***
	 * Returns the literal value of this constant.
	 * 
	 * @return
	 */
	public String getValue();

	/***
	 * Returns the language of this Literal constant, or null if its not a
	 * literal with language.
	 * 
	 * @return
	 */
	public String getLanguage();
}
