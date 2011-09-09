package it.unibz.krdb.sql.api;

public class ReferenceValueExpression extends AbstractValueExpression {

	@Override
	public void putSpecification(Object obj) {
		// Do nothing
	}

	@Override
	public String toString() {
		ColumnReference column = factors.get(0); // always has one element.
		return column.toString();
	}	
}
