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
import java.util.TreeSet;

public class Projection implements Serializable {

	private static final long serialVersionUID = -1926279507915359040L;
	
	public static final int SELECT_DEFAULT = 0;
	public static final int SELECT_ALL = 1;
	public static final int SELECT_DISTINCT = 2;

	private int type;

	/**
	 * Collection of columns for this projection.
	 */
	private ArrayList<DerivedColumn> selectList;

	public Projection() {
		selectList = new ArrayList<DerivedColumn>();
	}

	public void setType(int value) {
		type = value;
	}

	public String getType() {
		switch (type) {
		case SELECT_DEFAULT:
			return "select";
		case SELECT_ALL:
			return "select all";
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
	public void add(DerivedColumn column) {
		selectList.add(column);
	}

	/**
	 * Copies all the columns in the list and appends them to the existing list.
	 * 
	 * @param columns
	 *            The input column list.
	 */
	public void addAll(ArrayList<DerivedColumn> columns) {
		selectList.addAll(columns);
	}

	/**
	 * Updates the column list in this projection. Any existing columns are
	 * replaced by the new list.
	 * 
	 * @param columns
	 *            The new column list.
	 */
	public void update(ArrayList<DerivedColumn> columns) {
		selectList.clear();
		addAll(columns);
	}

	/**
	 * Retrieves all columns that are mentioned in the SELECT clause.
	 */
	public ArrayList<DerivedColumn> getColumnList() {
		return selectList;
	}
	
	/**
	 * Retrieves all column names that are mentioned in the SELECT clause.
	 */
	public ArrayList<String> getColumnNameList() {
		ArrayList<String> result = new ArrayList<String>();
		for (DerivedColumn column : getColumnList()) {
			result.add(column.getName());
		}
		return result;
	}
	
	/**
	 * Retrieves all column names that are particularly used in the query string
	 * for a specific table name.
	 * 
	 * @param table
	 *            The table name.
	 * @return Returns a list of table name.
	 */
	public String[] getColumns(String table) {
		TreeSet<String> list = new TreeSet<String>(); // use set to avoid duplication.
		for (DerivedColumn column : selectList) {
			ArrayList<ColumnReference> factors = column.getValueExpression().getAll();
			for (ColumnReference value : factors) {
				String columnOwner = value.getTable();
				if (columnOwner.equals(table)) {
					list.add(value.getColumn());
				}
			}
		}
		return list.toArray(new String[0]); // return the set as array.
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

		for (DerivedColumn column : selectList) {
			if (column == null) { // am asterisk was found
				str += "*";
				break;
			}

			if (bNeedComma) {
				str += ",";
			}
			str += " ";
			str += column.toString();
			bNeedComma = true;
		}
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
		for (DerivedColumn column : getColumnList()) {
			if (column.getName().equalsIgnoreCase(name)) {
				return true;
			}
			if (column.getAlias().equalsIgnoreCase(name)) {
				return true;
			}
		}
		return false;
	}
}
