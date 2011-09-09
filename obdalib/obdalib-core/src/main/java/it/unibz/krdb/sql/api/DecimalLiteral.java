package it.unibz.krdb.sql.api;

public class DecimalLiteral extends NumericLiteral {

	/**
	 * Float value
	 */
	protected Float value;

	/**
	 * Constructor
	 * 
	 * @param value
	 *            Float value as String
	 */
	public DecimalLiteral(String value) {
		set(new Float(value));
	}

	/**
	 * Set Float value
	 * 
	 * @param value
	 *            Float value
	 */
	public void set(Float value) {
		this.value = value;
	}

	/**
	 * Get Float value
	 * 
	 * @return Float value
	 */
	public Float get() {
		return value;
	}

	@Override
	public String toString() {
		return get().toString();
	}
}
