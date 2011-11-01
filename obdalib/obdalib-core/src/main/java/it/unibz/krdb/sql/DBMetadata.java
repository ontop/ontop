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
		
	/**
	 * Inserts a new data definition to this meta data object.
	 * 
	 * @param value
	 * 			The data definition. It can be a {@link TableDefinition} or a {@link ViewDefinition}
	 * 			object.
	 */
	public void add(DataDefinition value) {
		schema.put(value.getName(), value);
	}
	
	/**
	 * Inserts a list of data definition in batch.
	 * 
	 * @param list
	 * 			A list of data definition.
	 */
	public void add(List<DataDefinition> list) {
		for (DataDefinition value : list) {
			add(value);
		}
	}
	
	/**
	 * Retrieves the data definition object based on its name. The name can be either
	 * a table name or a view name.
	 * 
	 * @param name
	 * 			The string name.
	 */
	public DataDefinition getDefinition(String name) {
		return schema.get(name);
	}
	
	/**
	 * Returns the attribute name based on the table/view name and its position in the meta data.
	 * 
	 * @param name
	 * 			Can be a table name or a view name.
	 * @param pos
	 * 			The index position.
	 * @return
	 */
	public String getAttributeName(String name, int pos) {
		DataDefinition dd = getDefinition(name);
		return dd.getAttributeName(pos);
	}
	
	/**
	 * Returns the attribute full-qualified name based on the table/view name and its position in the meta data.
	 * 
	 * @param name
	 * 			Can be a table name or a view name.
	 * @param pos
	 * 			The index position.
	 * @return
	 */
	public String getFullQualifiedAttributeName(String name, int pos) {		
		String value = String.format("%s.%s", name, getAttributeName(name, pos));
		return value;
	}
}
