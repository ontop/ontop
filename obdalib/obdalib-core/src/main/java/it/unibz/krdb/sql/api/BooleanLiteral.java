package it.unibz.krdb.sql.api;

public class BooleanLiteral extends Literal {
	
	/**
	 * Boolean Value
	 */
	protected boolean value;

	/**
	 * Constructor
	 * 
	 * @param value
	 *            Boolean value
	 */
	public BooleanLiteral(boolean value) {
		this.set(value);
	}

	/**
	 * Set the boolean value
	 * 
	 * @param value
	 *            Boolean value
	 */
	public void set(boolean value) {
		this.value = value;
	}

	/**
	 * Get the Boolean value
	 * 
	 * @return Boolean value
	 */
	public boolean get() {
		return value;
	}

	@Override
	public String toString() {
		String result = get() ? "true" : "false";
		return result;
	}
}
