package it.unibz.krdb.sql;

public class ViewDefinition extends DataDefinition {

	private static final long serialVersionUID = 3312336193514797486L;

	private String statement;
	
	public ViewDefinition(String name) {
		super(name);
	}
	

	@Deprecated
	public void copy(String statement) {
		this.statement = statement;
	}
	
	public void setSQL(String statement) {
		this.statement = statement;
	}

	public String getStatement() {
		return statement;
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
			bf.append(attributes.get(i));
			comma = true;
		}
		bf.append("]");

		bf.append(String.format("   (%s)", statement));
		return bf.toString();
	}
}