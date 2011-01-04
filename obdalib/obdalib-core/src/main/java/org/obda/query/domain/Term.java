package org.obda.query.domain;

public interface Term {

	/**
	 * Duplicate the object by performing a deep cloning.
	 *
	 * @return the copy of the object.
	 */
	public Term copy();

	/**
	 * Get the name of the term object.
	 *
	 * @return a string name.
	 */
	public String getName();

	/**
	 * Get the string representation of the term object.
	 *
	 * @return a string text.
	 */
	public String toString();
}
