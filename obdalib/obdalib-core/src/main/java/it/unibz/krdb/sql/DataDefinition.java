package it.unibz.krdb.sql;

import it.unibz.krdb.sql.api.Attribute;

import java.util.ArrayList;
import java.util.HashMap;

public abstract class DataDefinition {

	protected String name;

	protected HashMap<Integer, Attribute> attributes = new HashMap<Integer, Attribute>();

	public DataDefinition() {

	}

	public void setName(String name) {
		this.name = name;
	}

	public String getName() {
		return name;
	}

	public void setAttribute(int pos, Attribute value) {
		attributes.put(pos, value);
	}

	public String getAttributeName(int pos) {
		Attribute attribute = attributes.get(pos);
		return attribute.name;
	}

	public Attribute getAttribute(int pos) {
		Attribute attribute = attributes.get(pos);
		return attribute;
	}

	public ArrayList<Attribute> getAttributes() {
		ArrayList<Attribute> list = new ArrayList<Attribute>();
		for (Attribute value : attributes.values()) {
			list.add(value);
		}
		return list;
	}

	public int countAttribute() {
		return attributes.size();
	}

}