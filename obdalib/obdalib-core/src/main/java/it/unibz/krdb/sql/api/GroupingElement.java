package it.unibz.krdb.sql.api;

import java.util.ArrayList;

public class GroupingElement {
	
	private ArrayList<ColumnReference> columnList;
	
	private boolean bAsGroup = false; // a single element or a group?
	
	/**
	 * The default constructor.
	 */
	public GroupingElement() {
		columnList = new ArrayList<ColumnReference>();
	}
	
	/**
	 * Adds a column to this grouping element.
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
	 * Overrides the existing list with the new one.
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
