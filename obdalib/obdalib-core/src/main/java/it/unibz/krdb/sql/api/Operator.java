package it.unibz.krdb.sql.api;

/**
 * A base class for operators in the relational algebra
 * expression.
 */
public abstract class Operator extends RelationalAlgebra {
	
	private static final long serialVersionUID = -707166944279903524L;
	
	protected String alias;

	public Operator() {
		// NO-OP
	}

	@Override
	public Operator clone() {
		return null;
	}
}