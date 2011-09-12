package it.unibz.krdb.sql.api;

/**
 * The DerivedColumn class stores the column expression
 * and the alternative name (its alias).
 */
public class DerivedColumn {
	
	private AbstractValueExpression value;
	private String alias = "";

	/**
	 * The default constructor.
	 */
	public DerivedColumn() {
		// Does nothing
	}
	
	/**
	 * Constructs the column object along with the value
	 * expression for defining this column.
	 * 
	 * @param value
	 * 			The column value expression.
	 */
	public DerivedColumn(AbstractValueExpression value) {
		setValueExpression(value);
	}
	
	/**
	 * Sets the value expression for defining this column.
	 * 
	 * @param value
	 * 			The column value expression.
	 */
	public void setValueExpression(AbstractValueExpression value) {
		this.value = value;
	}
	
	/**
	 * Gets the column value expression.
	 * 
	 * @return The column value expression.
	 */
	public AbstractValueExpression getValueExpression() {
		return value;
	}
	
	/**
	 * Sets the alternative name for this column.
	 * 
	 * @param name
	 * 			The alternative name.
	 */
	public void setAlias(String name) {
		this.alias = name;
	}
	
	/**
	 * Gets the alternative name for this column.
	 * 
	 * @return The alternative name.
	 */
	public String getAlias() {
		return alias;
	}
	
	/**
	 * Prints the string representation of this object.
	 */
	public String toString() {
		String str = value.toString();
		if (alias != "") {
			str += " as " + alias;
		}
		return str;
	}
}
