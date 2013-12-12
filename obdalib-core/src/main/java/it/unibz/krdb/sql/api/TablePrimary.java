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

public class TablePrimary implements ITable {
	
	private static final long serialVersionUID = -205626125381808960L;
	
	private String schema;
	private String name;
	private String alias;
	
	/*
	 * This two fields are new to handle quoted names in multi schema.
	 * tablename is the cleaned name of the table. GivenName is the name with quotes, upper case,
	 * that is, exactly as given by the user. 
	 */
	private String tableName;
	private String givenName;

	public TablePrimary(String name) {
		this("", name);
	}
	
	public TablePrimary(String schema, String name) {
		this(schema, name, name);
	}

	/*
	 * This constructor is used when we take the table names from the mappings.
	 * 
	 */
	public TablePrimary(String schema, String tableName, String givenName) {
			setSchema(schema);
			setTableName(tableName);
			setGivenName(givenName);
			setName(givenName);
			setAlias("");
			
		}
	/**
	 * @param givenName The table name exactly as it appears in the source sql query of the mapping, possibly with prefix and quotes
	 */
	public void setGivenName(String givenName) {
		this.givenName = givenName;
	}
	
	public void setSchema(String schema) {
		this.schema = schema;
	}
	
	public String getSchema() {
		return schema;
	}
	/**
	 * The table name (without prefix and without quotation marks)
	* @param tableName 
	*/
	public void setTableName(String tableName) {
		this.tableName = tableName;
	}
	
	public void setName(String name) {
		this.name = name;
	}
	/**
	 * @return The table name exactly as it appears in the source sql query of the mapping, 
	 * possibly with prefix and quotes
	 */
	public String getGivenName() {
		return givenName;
	}

	public String getTableName() {
		return tableName;
	}
	
	public String getName() {
		return name;
	}
	
	public void setAlias(String alias) {
		if (alias == null) {
			return;
		}
		this.alias = alias;
	}

	public String getAlias() {
		return alias;
	}

	@Override
	public String toString() {
		/*
		String str = "";
		if (schema != "") {
			str += schema + ".";
		}			
		str += tableName;
		if (alias != "") {
			str += " as " + alias;
		}
		*/
		return givenName;
	}

	/**
	 * Called from the MappingParser:getTables. 
	 * Needed to remove duplicates from the list of tables
	 */
	@Override
	public boolean equals(Object t){
		if(t instanceof TablePrimary){
			TablePrimary tp = (TablePrimary) t;
			return this.givenName.equals(tp.getGivenName())
					&& ((this.alias == null && tp.getAlias() == null)
							|| this.alias.equals(tp.getAlias())
							);
		}
		return false;
	}
}
