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
