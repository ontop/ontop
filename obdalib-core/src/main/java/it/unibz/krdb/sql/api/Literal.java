package it.unibz.krdb.sql.api;

/**
 * A shared abstract class to represent literal values.
 */
public abstract class Literal implements IValueExpression {
	
	private static final long serialVersionUID = 3027236534360355748L;

	public abstract Object get();
}
