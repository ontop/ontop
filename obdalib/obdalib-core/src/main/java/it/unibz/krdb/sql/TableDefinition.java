package it.unibz.krdb.sql;

import it.unibz.krdb.sql.DBMetadata.DataDefinition;
import it.unibz.krdb.sql.api.Attribute;

import java.util.ArrayList;
import java.util.HashMap;

public class TableDefinition implements DataDefinition {

	private String name;
	private HashMap<Integer, Attribute> attributes = new HashMap<Integer, Attribute>();

	public void setName(String name) {
		this.name = name;
	}

	@Override
	public String getName() {
		return name;
	}
	
	public void setAttribute(int pos, Attribute value) {
		attributes.put(pos, value);
	}
	
	public ArrayList<Attribute> getAttributes() {
		ArrayList<Attribute> list = new ArrayList<Attribute>();
		for (Attribute value : attributes.values()) {
			list.add(value);
		}
		return list;
	}
	
	@Override
	public String getAttributeName(int pos) {
		Attribute attribute = attributes.get(pos);
		return attribute.name;
	}
	
	@Override
	public int countAttribute() {
		return attributes.size();
	}
	
	@Override
	public String toString() {
		StringBuffer bf = new StringBuffer();
		bf.append(name);
		bf.append("[");
		boolean comma = false;
		for (Integer i: attributes.keySet()) {
			if (comma) {
				bf.append(",");
			}
			bf.append(attributes.get(i));
			comma = true;
		}
		bf.append("]");
		return bf.toString();
	}		
}
