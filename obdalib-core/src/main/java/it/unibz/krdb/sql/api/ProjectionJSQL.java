package it.unibz.krdb.sql.api;

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
	private final List<SelectExpressionItem> selectList = new ArrayList<SelectExpressionItem>();
	private final List<SelectExpressionItem> selectDistinctList = new ArrayList<SelectExpressionItem>(); //for the cases with DISTINCT ON
	private AllColumns allcolumns; //for the cases as SELECT *
	private AllTableColumns tablecolumns; //for the cases as SELECT table.*
	
	/** 
	 * A new Projection JSQL. It returns the select list or select distinct list. Recognize * sign.
	 */

	public ProjectionJSQL() {
		allcolumns = null;
		tablecolumns = null;
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
		if (distinctOn) {
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
		tablecolumns = column;
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
	 * Retrieves all columns that are mentioned in the SELECT clause.
	 */
	public List<SelectExpressionItem> getColumnList() {
		return selectList;
	}
	
	

	@Override
	public String toString() {
		StringBuilder str = new StringBuilder(getType());

		boolean bNeedComma = false;
		boolean bParenthesis = true;
		
		for (SelectExpressionItem column : selectDistinctList) {
			if (bNeedComma) {
				str.append(",");
			}
			str.append(" ");
			
			if (bParenthesis) {
				str.append("(");
				bParenthesis = false;
			}
			
			str.append(column.toString());
			bNeedComma = true;
		}
		
		if(!selectDistinctList.isEmpty()) {
			str.append(")");
			bNeedComma = false;
		}
			

		for (SelectExpressionItem column : selectList) {
			if (bNeedComma) {
				str.append(",");
			}
			str.append(" ");
			str.append(column.toString());
			bNeedComma = true;
		}
		
		if (allcolumns != null)
			str.append(" *");
		if (tablecolumns != null)
			str.append(" "+ tablecolumns.getTable() + ".*");
		
		return str.append(" from").toString();
	}	
}
