package it.unibz.krdb.sql.api;

import java.util.ArrayList;

public class Aggregation {
	
	private ArrayList<GroupingElement> groupingList;
	
	public Aggregation() {
		groupingList = new ArrayList<GroupingElement>();
	}
	
	public void add(ColumnReference column) {
		GroupingElement singleElement = new GroupingElement();
		singleElement.add(column);
		add(singleElement);
	}
	
	public void add(ArrayList<ColumnReference> columnList) {
		GroupingElement groupElement = new GroupingElement();
		for (ColumnReference col : columnList) {
			groupElement.add(col);
		}
		add(groupElement);
	}
	
	public void add(GroupingElement group) {
		groupingList.add(group);
	}
	
	public void addAll(ArrayList<GroupingElement> groups) {
		groupingList.addAll(groups);
	}
	
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