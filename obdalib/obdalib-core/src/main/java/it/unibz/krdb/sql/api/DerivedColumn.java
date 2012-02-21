package it.unibz.krdb.sql.api;

import java.io.Serializable;

/**
 * The DerivedColumn class stores the column expression
 * and the alternative name (its alias).
 */
public class DerivedColumn implements Serializable{
	
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
	 * @param alias
	 * 			The alternative name.
	 */
	public void setAlias(String alias) {
		this.alias = alias;
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
	 * Determines whether the column has an alternative name.
	 * 
	 * @return Returns true if the column has an alias name,
	 * or false otherwise.
	 */
	public boolean hasAlias() {
		return (alias != "")? true : false;
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
