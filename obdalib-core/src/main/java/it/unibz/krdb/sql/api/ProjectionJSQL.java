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

import net.sf.jsqlparser.statement.select.AllColumns;
import net.sf.jsqlparser.statement.select.AllTableColumns;
import net.sf.jsqlparser.statement.select.SelectExpressionItem;
/**
 * Store the information about the Projection of the parsed query. (between SELECT... FROM)
 */
import java.util.ArrayList;
import java.util.List;

public class ProjectionJSQL implements Serializable {

	private static final long serialVersionUID = -1926279507915359040L;
	
	public static final int SELECT_DEFAULT = 0;
	public static final int SELECT_DISTINCT_ON = 1;
	public static final int SELECT_DISTINCT = 2;

	private int type;

	/**
	 * Collection of columns for this projection.
	 */
	private ArrayList<SelectExpressionItem> selectList;
	private ArrayList<SelectExpressionItem> selectDistinctList; //for the cases with DISTINCT ON
	private AllColumns allcolumns; //for the cases as SELECT *
	private AllTableColumns tablecolumns; //for the cases as SELECT table.*
	
	/** 
	 * A new Projection JSQL. It returns the select list or select distinct list. Recognize * sign.
	 */

	public ProjectionJSQL() {
		selectList = new ArrayList<SelectExpressionItem>();
		selectDistinctList = new ArrayList<SelectExpressionItem>();
		allcolumns=null;
		tablecolumns=null;
	}

	public void setType(int value) {
		type = value;
	}

	public String getType() {
		switch (type) {
		case SELECT_DEFAULT:
			return "select";
		case SELECT_DISTINCT_ON:
			return "select distinct on";
		case SELECT_DISTINCT:
			return "select distinct";
		}
		return "";
	}

	/**
	 * Inserts this column to the projection list.
	 * 
	 * @param column
	 *            The input column object.
	 */
	public void add(SelectExpressionItem column, boolean distinctOn) {
		
		if (distinctOn){
			
			this.setType(ProjectionJSQL.SELECT_DISTINCT_ON);
			selectDistinctList.add(column);
		}
		else{
		
		selectList.add(column);
		}
	}

	/**
	 * Inserts all columns in the case of SELECT * FROM.
	 * 
	 * @param column
	 *            The allcolumns object.
	 */
	public void add(AllColumns column) {
		allcolumns = column;
	}
	
	/**
	 * Inserts  columns for a specific table to the projection list.
	 * 
	 * @param column
	 *            The AllTableColumns object.
	 */
	public void add(AllTableColumns column) {
		tablecolumns =column;
	}
	
	/**
	 * Copies all the columns in the list and appends them to the existing list.
	 * 
	 * @param columns
	 *            The input column list.
	 */
	public void addAll(List<SelectExpressionItem> columns) {
		selectList.addAll(columns);
	}

	/**
	 * Updates the column list in this projection. Any existing columns are
	 * replaced by the new list.
	 * 
	 * @param columns
	 *            The new column list.
	 */
	public void update(List<SelectExpressionItem> columns) {
		selectList.clear();
		addAll(columns);
	}

	/**
	 * Retrieves all columns that are mentioned in the SELECT clause.
	 */
	public List<SelectExpressionItem> getColumnList() {
		return selectList;
	}
	
	/**
	 * Retrieves all column names that are mentioned in the SELECT clause.
	 */
	public ArrayList<String> getColumnNameList() {
		ArrayList<String> result = new ArrayList<String>();
		for (SelectExpressionItem column : getColumnList()) {
			result.add(column.getExpression().toString());
		}
		return result;
	}
	

	/**
	 * Retrieves the number of columns this projection has.
	 */
	public int size() {
		return selectList.size();
	}

	@Override
	public String toString() {
		String str = getType();

		boolean bNeedComma = false;
		boolean bParenthesis= true;
		
		for (SelectExpressionItem column : selectDistinctList) 
		{
			
			if (bNeedComma) {
				str += ",";
			}
			str += " ";
			
			if (bParenthesis){
				str += "(";
				bParenthesis = false;
			}
			
			str += column.toString();
			bNeedComma = true;
		}
		
		if(!selectDistinctList.isEmpty())
		{
			str += ")";
			bNeedComma = false;
		}
			

		for (SelectExpressionItem column : selectList) {

			if (bNeedComma) {
				str += ",";
			}
			str += " ";
			str += column.toString();
			bNeedComma = true;
		}
		
		
			if(allcolumns!=null)
				str+= " *";
			if( tablecolumns!=null)
				str+= " "+tablecolumns.getTable()+".*";
		
		return str + " " + "from";
	}
	
	/**
	 * Checks if the name can be found in the projection. The name can be a
	 * column name or an alias.
	 * 
	 * @param name
	 * 			A column name or an alias.
	 * @return Returns true if the name is in the projection or false, otherwise.
	 */
	public boolean contains(String name) {
		for (SelectExpressionItem column : getColumnList()) {
			if (column.getExpression().toString().equalsIgnoreCase(name)) {
				return true;
			}
			if (column.getAlias().getName().equalsIgnoreCase(name)) {
				return true;
			}
		}
		return false;
	}
}
