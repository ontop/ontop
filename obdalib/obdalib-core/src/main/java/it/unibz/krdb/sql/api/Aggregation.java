package it.unibz.krdb.sql.api;

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