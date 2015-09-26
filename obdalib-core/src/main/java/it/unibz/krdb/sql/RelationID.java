package it.unibz.krdb.sql;

public class RelationID {

	private final QuotedID schema, table;
	
	public RelationID(QuotedID schema, QuotedID table) {
		this.schema = (schema == null) ? new QuotedID(null, "") : schema;
		this.table = table;
	}
	
	public QuotedID getSchema() {
		return schema;
	}
	
	public QuotedID getTable() {
		return table;
	}
	
	public String getName() {
		String s = schema.getName();
		if (s == null)
			return table.getName();
		
		return s + "." + table.getName();
	}

	public String getSQLRendering() {
		String s = schema.getSQLRendering();
		if (s == null)
			return table.getSQLRendering();
		
		return s + "." + table.getSQLRendering();
	}
	
	@Override 
	public String toString() {
		return getSQLRendering();
	}
	
	@Override 
	public int hashCode() {
		return table.hashCode();
	}
	
	@Override
	public boolean equals(Object obj) {
		if (this == obj)
			return true;
		
		if (obj instanceof RelationID) {
			RelationID other = (RelationID)obj;
			return (this.schema.equals(other.schema) && this.table.equals(other.table));
		}
		
		return false;
	}
}
