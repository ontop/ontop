package it.unibz.krdb.sql.api;

import java.util.ArrayList;

public class TablePrimary implements ITable {
	
	private String schema;
	private String name;
	private String alias;
	
	/**
	 * Collection of table attributes.
	 */
	private ArrayList<Attribute> attributes = new ArrayList<Attribute>();

	public TablePrimary(String name) {
		this("", name);
	}
	
	public TablePrimary(String schema, String name) {
		setSchema(schema);
		setName(name);
		setAlias("");
	}

	public void setSchema(String schema) {
		this.schema = schema;
	}
	
	public String getSchema() {
		return schema;
	}
	
	public void setName(String name) {
		this.name = name;
	}

	public String getName() {
		return name;
	}

	public void addAttribute(Attribute attr) {
		attributes.add(attr);
	}

	public Attribute getAttribute(int index) {
		return attributes.get(index);
	}
	
	public Attribute getAttribute(String name) {
		for (Attribute entry : attributes) {
			if (entry.hasName(name)) {
				return entry;
			}
		}
		return null;
	}

	public int getAttributeIndex(String name) {
		for (Attribute entry : attributes) {
			if (entry.hasName(name)) {
				return attributes.indexOf(entry);
			}
		}
		return -1;
	}
	
	public ArrayList<Attribute> getAttributes() {
		return attributes;
	}
	
	public ArrayList<String> getAttributeNames() {
		ArrayList<String> values = new ArrayList<String>();
		for (Attribute attr : getAttributes()) {
			values.add(attr.name);
		}
		return values;
	}

	public int getAttributeCount() {
		return attributes.size();
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
		String str = "";
		if (schema != "") {
			str += schema + ".";
		}			
		str += name;
		if (alias != "") {
			str += " as " + alias;
		}
		return str;
	}
}