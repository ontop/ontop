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

/**
 * The ColumnReference class is the basic structure for 
 * representing a column in the SQL query.
 */
public class ColumnReference implements Serializable{

	private static final long serialVersionUID = -7614041850928541325L;
	
	private String schema;
	private String table;
	private String column;
	
	public ColumnReference(String column) {
		this("", "", column);
	}
	
	public ColumnReference(String table, String column) {
		this("", table, column);
	}
	
	public ColumnReference(String schema, String table, String column) {
		setSchema(schema);
		setTable(table);
		setColumn(column);
	}
	
	public void setSchema(String name) {
		schema = name;
	}
	
	public String getSchema() {
		return schema;
	}
	
	public void setTable(String name) {
		table = name;
	}
	
	public String getTable() {
		return table;
	}
	
	public void setColumn(String name) {
		column = name;
	}
	
	public String getColumn() {
		return column;
	}
	
	@Override
	public String toString() {
		String str = "";
		if (schema != "") {
			str += schema + ".";
		}
		if (table != "") {
			str += table + ".";
		}
		str += column;
		return str;
	}
}
