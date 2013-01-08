package it.unibz.krdb.obda.model.impl;

import it.unibz.krdb.obda.model.AlgebraOperatorPredicate;

import java.net.URI;

public class AlgebraOperatorPredicateImpl extends PredicateImpl implements
		AlgebraOperatorPredicate {

	/**
	 * 
	 */
	private static final long serialVersionUID = 9152448029926695852L;

	/**
	 * Constructs a datatype predicate with one term. This is a usual construct
	 * where the type of the term represents the datatype itself.
	 * 
	 * @param name
	 *            The predicate name.
	 * @param type
	 *            The datatype that the term holds.
	 */
	public AlgebraOperatorPredicateImpl(URI name, COL_TYPE type) {
		super(name, 2, new COL_TYPE[] { type });
	}

	/**
	 * Construct a datatype predicate with two or more terms. The first term
	 * used to hold the value and the others are for any additional information.
	 * An example for using this constructor is the rdfs:Literal(value, lang).
	 * The predicate uses the second term to put the language tag.
	 * 
	 * @param name
	 *            The predicate name.
	 * @param types
	 *            The datatypes that each term holds.
	 */
	public AlgebraOperatorPredicateImpl(URI name, COL_TYPE[] types) {
		super(name, types.length, types);
	}

	@Override
	public AlgebraOperatorPredicateImpl clone() {
		return this;
	}

}
