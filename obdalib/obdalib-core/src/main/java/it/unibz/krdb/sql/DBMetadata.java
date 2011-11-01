package it.unibz.krdb.sql;

import java.util.HashMap;
import java.util.List;

public class DBMetadata {
	
	public interface DataDefinition {
		public String getName();
		public String getAttributeName(int pos);
		public int countAttribute();
		public String toString();
	}
	
	private HashMap<String, DataDefinition> schema = new HashMap<String, DataDefinition>();
		
	public void add(DataDefinition value) {
		schema.put(value.getName(), value);
	}
	
	public void add(List<DataDefinition> list) {
		for (DataDefinition value : list) {
			add(value);
		}
	}
	
	public DataDefinition getDefinition(String name) {
		return schema.get(name);
	}
	
	public String getAttributeName(String tableName, int pos) {
		DataDefinition dd = schema.get(tableName);
		return dd.getAttributeName(pos);
	}
	
	public String getFullQualifiedAttributeName(String tableName, int pos) {		
		String value = String.format("%s.%s", tableName, getAttributeName(tableName, pos));
		return value;
	}
}
