package it.unibz.krdb.sql.api;

public class StringLiteral extends Literal {
	
	/**
	 * Value
	 */
	protected String value;

	/**
	 * @param value
	 *            Value associated
	 */
	public StringLiteral(String value) {
		set(value);
	}

	/**
	 * Set the Value
	 * 
	 * @param value
	 *            Value
	 */
	public void set(String value) {
		this.value = value;
	}

	/**
	 * Get the Value
	 * 
	 * @return Value
	 */
	public String get() {
		return value;
	}

	@Override
	public String toString() {
		return value;
	}
}
