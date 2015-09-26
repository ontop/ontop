package it.unibz.krdb.sql;

public class TableDefinition extends RelationDefinition {

	public TableDefinition(RelationID name) {
		super(name);
	}

	@Override
	public String toString() {
		StringBuilder bf = new StringBuilder();
		bf.append(getID());
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
