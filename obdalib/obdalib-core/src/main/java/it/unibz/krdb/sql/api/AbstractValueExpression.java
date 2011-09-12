package it.unibz.krdb.sql.api;

import java.util.ArrayList;

/**
 * An abstract base class that defines value expressions. The expression
 * always involves one or several columns using string, mathematical or
 * boolean operators.
 */
public abstract class AbstractValueExpression implements IValueExpression {
	
	/**
	 * Collection of listed columns in the SQL query.
	 */
	protected ArrayList<ColumnReference> factors = new ArrayList<ColumnReference>();
	
	/**
	 * A {@link ColumnReference} contains the schema name, the table name
	 * and the column name. If the schema name is blank, it considers to
	 * use the default name "public".
	 * 
	 * @param schema
	 * 			The schema name. The default name "public" is used if this
	 * 			parameter is blank.
	 * @param table
	 * 			The table name.
	 * @param column
	 * 			The column name.
	 */
	public void add(String schema, String table, String column) {
		add(new ColumnReference(schema, table, column));
	}
	
	/**
	 * Inserts a {@link ColumnReference} to the list.
	 * 
	 * @param column
	 * 			The column object.
	 */
	public void add(ColumnReference column) {
		factors.add(column);
	}
	
	/**
	 * Retrieves a column object at a specified position in the list.
	 * 
	 * @param index
	 * 			The index of the element to return.
	 * @return Returns the column object at the specified position.
	 */
	public ColumnReference get(int index) {
		return factors.get(index);
	}
	
	/**
	 * Retrieves the column list.
	 */
	public ArrayList<ColumnReference> getAll() {
		return factors;
	}
	
	/**
	 * There might be some additional information that are contained
	 * in the value expression. Implement this method to store this
	 * kind of information.
	 * 
	 * @param obj
	 * 			The information object.
	 */
	public abstract void putSpecification(Object obj);
	
	/**
	 * Returns the string representation of this object.
	 */
	public abstract String toString();
}
