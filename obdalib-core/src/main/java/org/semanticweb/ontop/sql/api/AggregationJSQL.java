package org.semanticweb.ontop.sql.api;

/*
 * #%L
 * ontop-obdalib-core
 * %%
 * Copyright (C) 2009 - 2014 Free University of Bozen-Bolzano
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
import java.util.List;

import net.sf.jsqlparser.expression.Expression;
import net.sf.jsqlparser.schema.Column;

/**
 * Class for an Aggregation, it contains the GROUP BY statement.
 * 
 */
		

public class AggregationJSQL implements Serializable {

	
private static final long serialVersionUID = 5806057160397315905L;
	
	/**
	 * Collection of grouping columns in {@link Expression}. Each grouping unit can contain
	 * one or several {@link Column}.
	 */
	private List<Expression> groupingList;
	
	public AggregationJSQL() {
		groupingList = new ArrayList<Expression>();
	}
	
		
	/**
	 * Inserts a grouping element to the list. Use this method
	 * if users can define this object already.
	 * 
	 * @param group
	 * 			The grouping element.
	 */
	public void add(Column group) {
		groupingList.add(group);
	}
	
	/**
	 * Appends several grouping elements to the list.
	 * 
	 * @param list
	 * 			The list of grouping elements.
	 */
	public void addAll(List<Expression> list) {
		groupingList.addAll(list);
	}
	
	/**
	 * Updates the column list in this aggregation. Any existing
	 * grouping elements are replaced by the new list.
	 * 
	 * @param columns
	 * 			The new grouping element list.
	 */
	public void update(ArrayList<Expression> groups) {
		groupingList.clear();
		addAll(groups);
	}
	
	@Override
	public String toString() {
		String str = "";
		if(!groupingList.isEmpty())
		str = "group by";
		
		boolean bNeedComma = false;
		for (Expression group : groupingList) {
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
