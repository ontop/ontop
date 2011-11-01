package it.unibz.krdb.sql.api;

/**
 * This class represents the literal of boolean value.
 */
public class BooleanLiteral extends Literal {
	
	/**
	 * The boolean value.
	 */
	protected boolean value;

	public BooleanLiteral(boolean value) {
		this.set(value);
	}

	public void set(boolean value) {
		this.value = value;
	}

	public Object get() {
		return value;
	}

	@Override
	public String toString() {
		return value ? "true" : "false";
	}
}
