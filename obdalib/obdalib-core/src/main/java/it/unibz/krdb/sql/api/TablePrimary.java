package it.unibz.krdb.sql.api;

import java.util.ArrayList;

public class TablePrimary implements IRelation {
	
	private String schema;
	private String name;
	private String alias;
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
	
	public ArrayList<Attribute> getAttributeList() {
		return attributes;
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

	public boolean hasAttribute(String name) {
		for (Attribute entry : attributes) {
			if (entry.hasName(name)) {
				return true;
			}
		}
		return false;
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