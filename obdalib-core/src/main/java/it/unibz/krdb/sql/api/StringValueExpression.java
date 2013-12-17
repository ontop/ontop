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
import java.util.LinkedList;
import java.util.Queue;

public class StringValueExpression extends AbstractValueExpression {

	private static final long serialVersionUID = 4563093796759540610L;

	public static final String CONCAT_OP = "||";
	
	/**
	 * Collection of custom string expressions.
	 */
	private Queue<Object> cache = new LinkedList<Object>();
	
	@Override
	public void putSpecification(Object obj) {
		cache.add(obj);
		if (obj instanceof ColumnReference) {
			super.add((ColumnReference)obj);
		}
	}

	public Queue<Object> getSpecification() {
		return cache;
	}
	
	/**
	 * Retrieves all the {@link ColumnReference} objects that
	 * are used in this expression.
	 */
	public ArrayList<ColumnReference> getColumns() {
		ArrayList<ColumnReference> columns = new ArrayList<ColumnReference>();
		for (Object obj : cache) {
			if (obj instanceof ColumnReference) {
				columns.add((ColumnReference)obj);
			}
		}
		return columns;
	}

	@Override
	public String toString() {
		String str = "(";
		for (Object obj : cache) {
			if (obj instanceof String) {
				if (!((String)obj).equals(CONCAT_OP)) {
					str += "'" + obj.toString() + "'"; // to delimit the string
				}
			}
			str += obj.toString();
		}
		str += ")";
			
		return str;
	}
}
