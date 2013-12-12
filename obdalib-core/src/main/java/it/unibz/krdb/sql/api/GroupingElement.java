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
import java.util.ArrayList;

public class GroupingElement implements Serializable {
	
	private static final long serialVersionUID = -6857110873541531968L;

	/**
	 * Collection of columns that is used for grouping.
	 */
	private ArrayList<ColumnReference> columnList;
	
	private boolean bAsGroup = false; // a single element or a group?
	
	public GroupingElement() {
		columnList = new ArrayList<ColumnReference>();
	}
	
	/**
	 * Inserts a column to this grouping element.
	 * 
	 * @param column
	 * 			A single column.
	 */
	public void add(ColumnReference column) {
		columnList.add(column);
		if (columnList.size() > 1) {
			bAsGroup = true;
		}
	}
	
	/**
	 * Updates the column list in this grouping element. Any 
	 * existing columns are going to be replaced by the given
	 * new list.
	 * 
	 * @param columnList
	 * 			The new column list.
	 */
	public void update(ArrayList<ColumnReference> columnList) {
		columnList.clear(); // remove any existing columns.
		columnList.addAll(columnList);
	}
	
	@Override
	public String toString() {
		String str = "";
		
		boolean bNeedComma = false;
		for (ColumnReference col : columnList) {
			if (bNeedComma) {
				str += ", ";
			}
			str += col.toString();
			bNeedComma = true;
		}
		
		if (bAsGroup) {
			str = String.format("(%s)", str);
		}
		return str;
	}
}
