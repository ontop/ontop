package it.unibz.krdb.sql;

public class TableDefinition extends RelationDefinition {

	public TableDefinition(String catalogName, String schemaName, String tableName, String name) {
		super(catalogName, schemaName, tableName, name);
	}

	public TableDefinition(String name) {
		super(null, null, name, name);
	}

	@Override
	public String toString() {
		StringBuilder bf = new StringBuilder();
		bf.append(getName());
		bf.append("[");
		boolean comma = false;
		for (Attribute att : getAttributes()) {
			if (comma) 
				bf.append(",");
			
			bf.append(att);
			comma = true;
		}
		bf.append("]");

		return bf.toString();
	}

}
