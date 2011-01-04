package org.obda.query.domain;

import java.net.URI;

/**
 * A predicate is a property that the elements of the set have in common.
 * <p>
 * The notation {@code P(x)} is used to denote a sentence or statement
 * {@code P} concerning the variable object {@code x}. Also, the set defined
 * by {@code P(x)} written <code>{x|P(x)</code> is just a collection of all
 * the objects for which {@code P} is true.
 */
public interface Predicate {

	/**
	 * Get the name of the predicate. In practice, the predicate name
	 * is constructed as a URI to indicate a unique resource.
	 *
	 * @return the resource identifier (URI).
	 */
	public URI getName();

	/**
	 * Get the number of elements of the predicate.
	 *
	 * @return an integer number.
	 */
	public int getArity();

	/**
	 * Duplicate the object by performing a deep cloning.
	 *
	 * @return the copy of the object.
	 */
	public Predicate copy();
}
