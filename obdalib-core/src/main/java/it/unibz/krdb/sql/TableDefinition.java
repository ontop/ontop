package it.unibz.krdb.sql;

import it.unibz.krdb.sql.api.Attribute;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class TableDefinition extends DataDefinition {

	private static final long serialVersionUID = 1L;

	public TableDefinition(String name) {
		super(name);
	}
	
	public List<Attribute> getPrimaryKeys() {
		List<Attribute> primaryKeys = new ArrayList<Attribute>();
		for (Attribute attr : attributes.values()) {
			if (attr.isPrimaryKey()) {
				primaryKeys.add(attr);
			}
		}
		return primaryKeys;
	}
	
	public Map<String, List<Attribute>> getForeignKeys() {
		Map<String, List<Attribute>> foreignKeys = new HashMap<String, List<Attribute>>();
		for (Attribute attr : attributes.values()) {
			if (attr.isForeignKey()) {
				String fkName = attr.getReference().getReferenceName();
				List<Attribute> fkAttributes = foreignKeys.get(fkName);
				if (fkAttributes == null) {
					fkAttributes = new ArrayList<Attribute>();
					foreignKeys.put(fkName, fkAttributes);
				}
				fkAttributes.add(attr);
			}
		}
		return foreignKeys;
	}
	
	@Override
	public String toString() {
		StringBuilder bf = new StringBuilder();
		bf.append(name);
		bf.append("[");
		boolean comma = false;
		for (Integer i : attributes.keySet()) {
			if (comma) {
				bf.append(",");
			}
			Attribute at = attributes.get(i);
			bf.append(at);
			if (at.isPrimaryKey()) {
				bf.append(":PK");
			}
			if (at.isForeignKey()) {
				bf.append(":FK:");
				bf.append(String.format("%s(%s)", 
						at.getReference().getTableReference(), 
						at.getReference().getColumnReference()));
			}
			comma = true;
		}
		bf.append("]");
		return bf.toString();
	}
}
