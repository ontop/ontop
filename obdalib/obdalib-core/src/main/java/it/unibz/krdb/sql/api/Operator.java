package it.unibz.krdb.sql.api;

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