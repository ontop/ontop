package it.unibz.krdb.sql;

import it.unibz.krdb.sql.DBMetadata.DataDefinition;
import it.unibz.krdb.sql.api.Attribute;

import java.util.HashMap;

public class ViewDefinition implements DataDefinition {

	private String name;
	private HashMap<Integer, Attribute> attributes = new HashMap<Integer, Attribute>();

	private String statement;
	
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
	
	@Override
	public String getAttributeName(int pos) {
		Attribute attribute = attributes.get(pos);
		return attribute.name;
	}
	
	@Override
	public int countAttribute() {
		return attributes.size();
	}
	
	public void copy(String statement) {
		this.statement = statement;
	}
	
	@Override
	public String toString() {
		return String.format("(%s)", statement);
	}	
}