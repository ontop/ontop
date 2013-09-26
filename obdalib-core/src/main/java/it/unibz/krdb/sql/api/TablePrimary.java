/*
 * Copyright (C) 2009-2013, Free University of Bozen Bolzano
 * This source code is available under the terms of the Affero General Public
 * License v3.
 * 
 * Please see LICENSE.txt for full license terms, including the availability of
 * proprietary exceptions.
 */
package it.unibz.krdb.sql.api;

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
		setSchema(schema);
		setName(name);
		setAlias("");
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
}
