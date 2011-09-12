package it.unibz.krdb.sql.api;

public class CollectionValueExpression extends AbstractValueExpression {

	/**
	 * The name of the function operation.
	 */
	private String functionOp = "";
	
	@Override
	public void putSpecification(Object obj) {
		functionOp = (String)obj;
	}
	
	public String getSpecification() {
		return functionOp;
	}

	@Override
	public String toString() {
		return functionOp + "(" + factors.get(0) + ")";
	}
}
