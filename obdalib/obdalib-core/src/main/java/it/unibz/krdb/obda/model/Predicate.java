package it.unibz.krdb.obda.model;

import java.io.Serializable;
import java.net.URI;

/**
 * A predicate is a property that the elements of the set have in common.
 * <p>
 * The notation {@code P(x)} is used to denote a sentence or statement {@code P}
 * concerning the variable object {@code x}. Also, the set defined by
 * {@code P(x)} written <code>{x|P(x)</code> is just a collection of all the
 * objects for which {@code P} is true.
 */
public interface Predicate extends Cloneable, Serializable {

	public enum COL_TYPE {
		OBJECT, BNODE, LITERAL, INTEGER, DECIMAL, DOUBLE, STRING, DATETIME, BOOLEAN, UNBOUND
	};

	/**
	 * Get the name of the predicate. In practice, the predicate name is
	 * constructed as a URI to indicate a unique resource.
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

	/***
	 * Returns the typing of the component given by component. Types can be
	 * "Object" or "Literal", defined by the inner enumerator PRED_TYPE
	 * 
	 * @param column
	 */
	public COL_TYPE getType(int column);

	/**
	 * Duplicate the object by performing a deep cloning.
	 * 
	 * @return the copy of the object.
	 */
	public Predicate clone();

	boolean isClass();

	boolean isObjectProperty();

	boolean isDataProperty();

}
