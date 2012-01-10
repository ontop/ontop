package it.unibz.krdb.sql;

public class ViewDefinition extends DataDefinition {

	private String statement;

	public void copy(String statement) {
		this.statement = statement;
	}

	public String getStatement() {
		return statement;
	}

	@Override
	public String toString() {
		StringBuffer bf = new StringBuffer();
		bf.append(name);
		bf.append("[");
		boolean comma = false;
		for (Integer i : attributes.keySet()) {
			if (comma) {
				bf.append(",");
			}
			bf.append(attributes.get(i));
			comma = true;
		}
		bf.append("]");

		bf.append(String.format("   (%s)", statement));
		return bf.toString();

	}
}