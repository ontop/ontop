package it.unibz.krdb.obda.model;

/**
 * This class defines a type of {@link NewLiteral} in which it expresses a quantity
 * that during a calculation is assumed to vary or be capable of varying in
 * value.
 */
public interface Variable extends NewLiteral {

	public String getName();
}
