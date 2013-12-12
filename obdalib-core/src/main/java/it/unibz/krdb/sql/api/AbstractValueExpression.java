package it.unibz.krdb.sql.api;

/*
 * #%L
 * ontop-obdalib-core
 * %%
 * Copyright (C) 2009 - 2013 Free University of Bozen-Bolzano
 * %%
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 * 
 *      http://www.apache.org/licenses/LICENSE-2.0
 * 
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 * #L%
 */

import java.util.ArrayList;

/**
 * An abstract base class that defines value expressions. The expression
 * always involves one or several columns using string, mathematical or
 * boolean operators.
 */
public abstract class AbstractValueExpression implements IValueExpression {
	
	private static final long serialVersionUID = -7761700127601635879L;
	
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
