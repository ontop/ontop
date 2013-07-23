package it.unibz.krdb.sql.api;

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
