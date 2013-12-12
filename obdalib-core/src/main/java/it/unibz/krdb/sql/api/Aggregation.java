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

public class Aggregation implements Serializable{
	
	private static final long serialVersionUID = 5806057160397315905L;
	
	/**
	 * Collection of grouping columns. Uses {@link GroupingElement}
	 * as the grouping unit. Each grouping unit can contain
	 * one or several {@link ColumnReference}.
	 */
	private ArrayList<GroupingElement> groupingList;
	
	public Aggregation() {
		groupingList = new ArrayList<GroupingElement>();
	}
	
	/**
	 * Inserts a single column as the grouping element.
	 * 
	 * @param column
	 * 			The column object.
	 */
	public void add(ColumnReference column) {
		GroupingElement singleElement = new GroupingElement();
		singleElement.add(column);
		add(singleElement);
	}
	
	/**
	 * Inserts several columns as the grouping element.
	 * 
	 * @param columnList
	 * 			The column list.
	 */
	public void add(ArrayList<ColumnReference> columnList) {
		GroupingElement groupElement = new GroupingElement();
		for (ColumnReference col : columnList) {
			groupElement.add(col);
		}
		add(groupElement);
	}
	
	/**
	 * Inserts a grouping element to the list. Use this method
	 * if users can define this object already.
	 * 
	 * @param group
	 * 			The grouping element.
	 */
	public void add(GroupingElement group) {
		groupingList.add(group);
	}
	
	/**
	 * Appends several grouping elements to the list.
	 * 
	 * @param groups
	 * 			The list of grouping elements.
	 */
	public void addAll(ArrayList<GroupingElement> groups) {
		groupingList.addAll(groups);
	}
	
	/**
	 * Updates the column list in this aggregation. Any existing
	 * grouping elements are replaced by the new list.
	 * 
	 * @param columns
	 * 			The new grouping element list.
	 */
	public void update(ArrayList<GroupingElement> groups) {
		groupingList.clear();
		addAll(groups);
	}
	
	@Override
	public String toString() {
		String str = "group by";
		
		boolean bNeedComma = false;
		for (GroupingElement group : groupingList) {
			if (bNeedComma) {
				str += ",";
			}
			str += " ";
			str += group.toString();
			bNeedComma = true;
		}
		return str;
	}
}
