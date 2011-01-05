package org.obda.query.domain;

import java.util.List;

/**
 * This class defines a type of {@link Term} in which it denotes a mapping of
 * one or more elements in a set (called the domain of the function) into a
 * unique element of another set (the range of the function).
 * <p>
 * A function expression is a function symbol followed by its arguments.
 * The arguments are elements from the domain of the function; the number of
 * arguments is equal to the {@code arity} of the function. The arguments are
 * enclosed in parentheses and separated by commas, e.g.,
 * <p>
 * <code>
 * f(X,Y) <br />
 * father(david) <br />
 * price(apple) <br />
 * </code>
 * <p>
 * are all well-formed function expressions.
 */
public interface Function extends Term {

	/**
	 * Get a list of terms (or arguments) that are contained in the function
	 * symbol.
	 *
	 * @return a list of terms.
	 */
	public List<Term> getTerms();

	/**
	 * Get the function symbol.
	 *
	 * @return the predicate object.
	 */
	public Predicate getFunctionSymbol();

	/**
	 * Get the number of terms (or arguments) in the function symbol.
	 *
	 * @return the arity.
	 */
	public int getArity();
}
