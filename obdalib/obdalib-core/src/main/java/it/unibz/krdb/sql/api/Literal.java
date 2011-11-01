package it.unibz.krdb.sql.api;

/**
 * A shared abstract class to represent literal values.
 */
public abstract class Literal implements IValueExpression {
	
	public abstract Object get();
}
