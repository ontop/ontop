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

import net.sf.jsqlparser.schema.Table;


public class TableJSQL implements Serializable{
	
	private final long serialVersionUID = 7031993308873750327L;
	/**
	 * Class TableJSQL used to store the information about the tables. We distinguish between givenName and Name.
	 * Since with Name we don't want to consider columns.
	 */
	
	private String schema;
	private String tableName;
	private String name;
	private String alias;
	
	
	
	/*
	 * These  fields are to handle quoted names in multischema.
	 * givenSchema and givenName are the names with quotes, upper case,
	 * that is, exactly as given by the user. 
	 * quotedTable and quotedSchema are boolean value to identify when quotes have been removed.
	 */
	
	private String givenSchema;
	private String givenName;
	private boolean quotedTable, quotedSchema;
	

	public TableJSQL(String name) {
		this("", name);
	}
	
	public TableJSQL(String schema, String name) {
		this(schema, name, name);
	}

	/*
	 * This constructor is used when we take the table names from the mappings.
	 * 
	 */
	public TableJSQL(String schema, String tableName, String givenName) {
			setSchema(schema);
			setGivenSchema(schema);
			setTableName(tableName);
			setGivenName(givenName);
			setName(givenName);
			
		}
	
	public TableJSQL(Table table){
		setSchema(table.getSchemaName());
		setGivenSchema(table.getSchemaName());
		setTableName(table.getName());
		setGivenName(table.getWholeTableName());
		setName(table.getName());
		setAlias(table.getAlias());
	}

	/**
	 * @param givenName The table name exactly as it appears in the source sql query of the mapping, possibly with prefix and quotes
	 */
	public void setGivenName(String givenName) {
		this.givenName = givenName;
	}
	
	public void setSchema(String schema) {
		if(schema!=null && (schema.startsWith("\"") || schema.startsWith("`")))
			this.schema = schema.substring(1, schema.length()-1);
		else
			this.schema = schema;
	}
	
	public String getSchema() {
		return schema;
	}
	
	/**
	 * @param givenSchema The schema name exactly as it appears in the source sql query of the mapping, possibly with prefix and quotes
	 */
	
	public void setGivenSchema(String givenSchema) {
		this.givenSchema = givenSchema;
		
	}
	
	public String getGivenSchema() {
		return givenSchema;
		
	}
	/**
	 * The table name (without prefix and without quotation marks)
	* @param tableName 
	*/
	public void setTableName(String tableName) {
		if(tableName.startsWith("\"") || tableName.startsWith("`")  || tableName.startsWith("["))
		{
			this.tableName = tableName.substring(1, tableName.length()-1);
			quotedTable = true;
		}
		else			
			this.tableName = tableName;
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
	

	public void setName(String name) {
		this.name = name;
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
	
	/**
	 * 
	 * @return true if the original name of the table is quoted
	 */
	public boolean isTableQuoted() {
		return quotedTable;
	}
	/**
	 * 
	 * @return true if the original name of the schema is quoted
	 */
	public boolean isSchemaQuoted() {
		return quotedSchema;
	}

	@Override
	public String toString() {

		return givenName;
	}

	/**
	 * Called from the MappingParser:getTables. 
	 * Needed to remove duplicates from the list of tables
	 */
	@Override
	public boolean equals(Object t){
		if(t instanceof TableJSQL){
			TableJSQL tp = (TableJSQL) t;
			return this.givenName.equals(tp.getGivenName())
					&& ((this.alias == null && tp.getAlias() == null)
							|| this.alias.equals(tp.getAlias())
							);
		}
		return false;
	}

	
}
