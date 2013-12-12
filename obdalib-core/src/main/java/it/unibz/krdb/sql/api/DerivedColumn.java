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

import java.io.Serializable;

/**
 * The DerivedColumn class stores the column expression
 * and the alternative name (its alias).
 */
public class DerivedColumn implements Serializable {
	
	private static final long serialVersionUID = 5426403301125901562L;
	
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
	 * Gets the column name.
	 */
	public String getName() {
		return value.toString();
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
