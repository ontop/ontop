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

import net.sf.jsqlparser.expression.Alias;
import net.sf.jsqlparser.schema.Table;

import java.io.Serializable;


public class TableJSQL implements Serializable{
	
	private static final long serialVersionUID = 7031993308873750327L;
	/**
	 * Class TableJSQL used to store the information about the tables. We distinguish between givenName and Name.
	 * Since with Name we don't want to consider columns.
	 */
	
	private String schema;
	private String tableName;
	private Alias alias;
	
	
	
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
			
		}
	
	public TableJSQL(Table table){
		setSchema(table.getSchemaName());
		setGivenSchema(table.getSchemaName());
		setTableName(table.getName());
		setGivenName(table.getFullyQualifiedName());
		setAlias(table.getAlias());
	}

	/**
	 * @param givenName The table name exactly as it appears in the source sql query of the mapping, possibly with prefix and quotes
	 */
	public void setGivenName(String givenName) {
		this.givenName = givenName;
	}
	
	public void setSchema(String schema) {
		if(schema!=null && ParsedSQLQuery.pQuotes.matcher(schema).matches()) {
            this.schema = schema.substring(1, schema.length() - 1);
            quotedSchema = true;
        }
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
		if(ParsedSQLQuery.pQuotes.matcher(tableName).matches())
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

	/**
	 * The alias given to the table
	 * See test  QuotedAliasTableTest
	 * @param alias
	 */
	public void setAlias(Alias alias) {
		if (alias == null) {
			return;
		}
		alias.setName(unquote(alias.getName()));
		this.alias = alias;
	}
	
	public Alias getAlias() {
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

	/**
	 * Idempotent method.
	 */
	public static String unquote(String name) {
		if(ParsedSQLQuery.pQuotes.matcher(name).matches()) {
			return name.substring(1, name.length()-1);
		}
		return name;
	}
	
}
