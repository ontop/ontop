package it.unibz.krdb.sql;

import it.unibz.krdb.sql.api.Attribute;
import it.unibz.krdb.sql.api.TablePrimary;

import java.util.Hashtable;

public class DBMetadata {
	
	private static final String DEFAULT_SCHEMA = "public";
	
	private String database;
	
	private Hashtable<String, Hashtable<String, TablePrimary>> metadata = 
		new Hashtable<String, Hashtable<String, TablePrimary>>();
	
	/**
	 * Creates the database metadata. The metadata comprises of two levels. 
	 * The first level contains the schema list owned by the database and 
	 * the second level contains the table list within a schema. By default, 
	 * the metadata contains already a schema called "public".
	 * 
	 * @param database
	 * 			The database name.
	 */
	public DBMetadata(String database) {
		Hashtable<String, TablePrimary> tableList = new Hashtable<String, TablePrimary>();
		metadata.put(DEFAULT_SCHEMA, tableList);
		setDatabase(database);
	}
	
	/**
	 * Sets the database name for this metadata.
	 * 
	 * @param name
	 * 			The database name.
	 */
	public void setDatabase(String name) {
		database = name;
	}
	
	/**
	 * Returns the database name.
	 * 
	 * @return Returns the database name.
	 */
	public String getDatabase() {
		return database;
	}
	
	/**
	 * Inserts a new table with some basic information. By default, the
	 * method uses schema name "public" and set false for both primary
	 * key and nullable properties.
	 * 
	 * @param table
	 * 			The table name.
	 * @param column
	 * 			The table column name.
	 * @param type
	 * 			The column data type.
	 */
	public void add(String table, String column, String type) {
		add(DEFAULT_SCHEMA, table, column, type, false, 0);
	}
	
	/**
	 * Inserts a new table with all its attributes information in a given
	 * schema name.
	 * 
	 * @param schema
	 * 			The schema name.
	 * @param table
	 * 			The table name.
	 * @param column
	 * 			The table column name.
	 * @param type
	 * 			The column data type.
	 * @param primaryKey
	 * 			Is the column a primary key?
	 * @param canNull
	 * 			Can the column null?
	 */
	public void add(String schema, String table, String column, String type, boolean primaryKey, int canNull) {
		Attribute attr = new Attribute(column, type, primaryKey, canNull);
		
		Hashtable<String, TablePrimary> tableList = getTableList(schema);
		if (tableList == null) {  // creates a table list for a new schema name
			tableList = new Hashtable<String, TablePrimary>();
			metadata.put(schema, tableList);
		}
		
		if (tableList.containsKey(table)) {
			TablePrimary tableObj = tableList.get(table);
			tableObj.addAttribute(attr);
		}
		else {
			TablePrimary tableObj = (schema == DEFAULT_SCHEMA)?
		 	    new TablePrimary(table) :  // ignore the schema name if it is the default.
			    new TablePrimary(schema, table);
			
			tableObj.addAttribute(attr);
			tableList.put(table, tableObj);
			metadata.put(schema, tableList);
		}
	}
	
	/**
	 * Determines if the database has the schema.
	 * 
	 * @param schema
	 * 			The schema name.
	 * @return Returns true if the database has the schema, or false
	 * otherwise.
	 */
	public boolean hasSchema(String schema) {
		return (metadata.containsKey(schema));
	}
	
	/**
	 * Determines if the database has the table. The method uses the
	 * default schema name "public".
	 * 
	 * @param table
	 * 			The table name.
	 * @return Returns true if the database has the table, or false
	 * otherwise.
	 */
	public boolean hasTable(String table) {
		return hasTable(DEFAULT_SCHEMA, table);
	}
	
	/**
	 * Determines if the database has the table within the given schema.
	 * 
	 * @param schema
	 * 			The schema name.
	 * @param table
	 * 			The table name.
	 * @return Returns true if the database has the schema.table, or 
	 * false otherwise.
	 */
	public boolean hasTable(String schema, String table) {		
		if (hasSchema(schema)) {
			Hashtable<String, TablePrimary> tableList = getTableList(schema);
			return tableList.containsKey(table);
		}
		return false;
	}
	
	/**
	 * Returns the table list that is contained in a schema, or <tt>null</tt> 
	 * if the database doesn't contain the schema.
	 * 
	 * @param schema
	 * 			The schema name.
	 * @return The table list, or <tt>null</tt> if the database doesn't
	 * contain the schema.
	 */
	public Hashtable<String, TablePrimary> getTableList(String schema) {
		Hashtable<String, TablePrimary> tableList = null;
		if (hasSchema(schema)) {
			tableList = metadata.get(schema);			
		}
		return tableList;
	}

	/**
	 * Returns the table object to which the specified table name is
	 * mapped, or <tt>null</tt> if there is no mapping for the table
	 * name. The method uses the default schema name "public".
	 * 
	 * @param table
	 * 			The table name.
	 * @return Returns the {@link TablePrimary} object, or <tt>null</tt> if
	 * there is no mapping for the table name
	 */
	public TablePrimary getTable(String table) {
		return getTable(DEFAULT_SCHEMA, table);
	}
	
	/**
	 * Returns the table object to which the specified schema and table 
	 * name are mapped, or <tt>null</tt> if there is no mapping for both
	 * names.
	 * 
	 * @param schema
	 * 			The schema name.
	 * @param table
	 * 			The table name.
	 * @return Returns the {@link TablePrimary} object, or <tt>null</tt> if
	 * there is no mapping for the schema and table name
	 */
	public TablePrimary getTable(String schema, String table) {
		Hashtable<String, TablePrimary> tableList = getTableList(schema);
		if (tableList != null) {
			return tableList.get(table);
		}
		return null;
	}
}
