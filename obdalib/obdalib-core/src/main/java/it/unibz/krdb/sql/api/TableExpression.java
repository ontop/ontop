package it.unibz.krdb.sql.api;

import java.util.ArrayList;

public class TableExpression {

	private ArrayList<TablePrimary> tableList;
	private BooleanValueExpression booleanExp;
	private ArrayList<GroupingElement> groupingList;
	
	public TableExpression(ArrayList<TablePrimary> tableList) {
		setFromClause(tableList);
	}

	public void setFromClause(ArrayList<TablePrimary> tableList) {
		this.tableList = tableList;
	}
	
	public ArrayList<TablePrimary> getFromClause() {
		return tableList;
	}
	
	public void setWhereClause(BooleanValueExpression booleanExp) {
		this.booleanExp = booleanExp;
	}
	
	public BooleanValueExpression getWhereClause() {
		return booleanExp;
	}
	
	public void setGroupByClause(ArrayList<GroupingElement> groupingList) {
		this.groupingList = groupingList;
	}
	
	public ArrayList<GroupingElement> getGroupByClause() {
		return groupingList;
	}
}
