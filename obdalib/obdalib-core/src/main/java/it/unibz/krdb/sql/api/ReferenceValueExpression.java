package it.unibz.krdb.sql.api;

public class ReferenceValueExpression extends AbstractValueExpression {

	private static final long serialVersionUID = -3324178278393020165L;

	@Override
	public void putSpecification(Object obj) {
		// NO-OP
	}

	@Override
	public String toString() {
		ColumnReference column = factors.get(0); // always has one element.
		return column.toString();
	}	
}
