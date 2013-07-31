/*
 * Copyright (C) 2009-2013, Free University of Bozen Bolzano
 * This source code is available under the terms of the Affero General Public
 * License v3.
 * 
 * Please see LICENSE.txt for full license terms, including the availability of
 * proprietary exceptions.
 */
package it.unibz.krdb.sql.api;

import java.io.Serializable;
import java.util.ArrayList;

public class TableExpression implements Serializable{

	private static final long serialVersionUID = 8677327000060313472L;
	
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
