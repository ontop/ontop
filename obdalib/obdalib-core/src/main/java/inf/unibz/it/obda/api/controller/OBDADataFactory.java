package inf.unibz.it.obda.api.controller;

import java.net.URI;

import org.obda.query.domain.Predicate;

public interface OBDADataFactory {
	/**
	 * Construct a {@link Predicate} object.
	 *
	 * @param name the name of the predicate (defined as a URI).
	 * @param arity the number of elements inside the predicate.
	 * @return a predicate object.
	 */
	public Predicate createPredicate(URI name, int arity);
	
}
