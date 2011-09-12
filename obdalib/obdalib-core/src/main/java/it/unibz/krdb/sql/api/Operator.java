package it.unibz.krdb.sql.api;

/**
 * A base class for operators in the relational algebra
 * expression.
 */
public abstract class Operator extends RelationalAlgebra {
	
	protected String alias;

	public Operator() {
		// Does nothing
	}

	@Override
	public Operator clone() {
		return null;
	}
}